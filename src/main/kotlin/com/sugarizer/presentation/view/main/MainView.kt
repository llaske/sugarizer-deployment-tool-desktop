package view.main

import com.sugarizer.BuildConfig
import com.sugarizer.domain.model.MusicModel
import javafx.scene.layout.BorderPane
import com.sugarizer.domain.shared.JADB
import com.sugarizer.domain.shared.database.DBUtil
import com.sugarizer.domain.shared.database.FileSynchroniser
import com.sugarizer.domain.shared.database.MusicDAO
import com.sugarizer.main.Main
import com.sugarizer.presentation.custom.ListItemDevice
import com.sugarizer.presentation.custom.ListItemMenu
import io.reactivex.Observable
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import javafx.animation.FadeTransition
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.layout.StackPane
import javafx.util.Callback
import javafx.util.Duration
import org.controlsfx.control.GridCell
import org.controlsfx.control.GridView
import se.vidstige.jadb.JadbDevice
import se.vidstige.jadb.RemoteFile
import tornadofx.*
import java.io.File
import java.io.IOException
import javax.inject.Inject
import kotlin.reflect.KClass

class ListItemDeviceCellFactory : Callback<GridView<ListItemDevice>, GridCell<ListItemDevice>> {
    override fun call(param: GridView<ListItemDevice>?): GridCell<ListItemDevice> {
        return ListItemDeviceCell()
    }
}

class ListItemDeviceCell : GridCell<ListItemDevice>() {
    override fun updateItem(item: ListItemDevice?, empty: Boolean) {
        super.updateItem(item, empty)
        if (empty || item == null) {
            graphic = null
        } else {
            graphic = item
        }
    }
}

class MainView : View() {
    override val root : StackPane by fxml("/layout/main.fxml")

    @Inject lateinit var jadb: JADB
    @Inject lateinit var fileSync: FileSynchroniser
    @Inject lateinit var dbUtils: DBUtil
    @Inject lateinit var musicDAO: MusicDAO

    val container : BorderPane by fxid("container")

    val devicesView: Node by fxid("devicesView")
    val settingsView: Node by fxid("settingsView")
    val createView: Node by fxid("createView")
    val synchronisationView: Node by fxid("synchronisationView")

    val deviceItem: ListItemMenu by fxid("deviceItem")
    val settingsItem: ListItemMenu by fxid("settingsItem")
    val createItem: ListItemMenu by fxid("createItem")
    val synchronisationItem: ListItemMenu by fxid("synchronisationItem")

    var lastView: Node = createView
    var lastItem: ListItemMenu = createItem

    init {
        title = "Sugarizer Deloyment Tool - " + BuildConfig.VERSION

        Main.appComponent.inject(this)

        dbUtils.dbConnect()
        fileSync.startSearching()
                .observeOn(Schedulers.computation())
                .doOnComplete {
                    var list = musicDAO.searchMusic()

                    pushOnDevice(jadb.listJadb, 0, list, 0)
                }
                .subscribe()

        try {
            deviceItem.onMouseClicked = EventHandler { load(devicesView, deviceItem) }
            synchronisationItem.onMouseClicked = EventHandler { load(synchronisationView, synchronisationItem) }
            createItem.onMouseClicked = EventHandler { load(createView, createItem) }
            settingsItem.onMouseClicked = EventHandler { load(settingsView, settingsItem) }

            load(devicesView, deviceItem)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun pushingFile(list: List<MusicModel>, index: Int, device: JadbDevice)
    //        : Observable<Any>
    {
        Observable.create<Any> { subscriber ->
            Observable.create<Any> {
                var music = list[index]
                device.push(File(music.musicPath), RemoteFile("/sdcard/music-sugar/" + music.musicName))
                it.onComplete()
            }
                    .subscribeOn(Schedulers.computation())
                    .observeOn(JavaFxScheduler.platform())
                    .doOnComplete {
                        if (index < list.size - 1) {
                            pushingFile(list, index + 1, device)
                        } else {
                            subscriber.onComplete()
                        }
                    }
                    .subscribe()
        }
                .subscribe()
    }

    fun pushOnDevice(listDevice: List<JadbDevice>, indexDevice: Int, listFile: List<MusicModel>, indexMusic: Int) {
        Observable.create<Any> {
            pushingFile(listFile, 0, listDevice[indexDevice])
        }
                .subscribeOn(Schedulers.computation())
                .observeOn(JavaFxScheduler.platform())
                .doOnComplete {
                    if (indexDevice < listDevice.size - 1) {
                        pushOnDevice(listDevice, indexDevice + 1, listFile, 0)
                    }
                }
                .subscribe()
    }

    private fun load(viewIn: Node, button: ListItemMenu) {
        if (!viewIn.equals(lastView)) {
            viewIn.isVisible = true
            var fadeIn = FadeTransition(Duration.millis(250.0), viewIn)
            fadeIn.fromValue = 0.0
            fadeIn.toValue = 1.0

            var fadeOut = FadeTransition(Duration.millis(250.0), lastView)
            fadeOut.fromValue = 1.0
            fadeOut.toValue = 0.0

            fadeIn.setOnFinished {
                lastView.isVisible = false
                lastView = viewIn

                lastItem.setSelected(false)
                button.setSelected(true)
                lastItem = button
            }

            fadeIn.play()
            fadeOut.play()
        }
    }
}