package view.main

import com.sugarizer.BuildConfig
import com.sugarizer.model.MusicModel
import com.sugarizer.utils.shared.JADB
import com.sugarizer.utils.shared.NotificationBus
import com.sugarizer.Main
import com.sugarizer.listitem.ListItemMenu
import com.sugarizer.listitem.ListItemNotification
import com.sugarizer.utils.shared.SpkManager
import io.reactivex.Observable
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import javafx.animation.FadeTransition
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.control.ListView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.StackPane
import javafx.util.Duration
import se.vidstige.jadb.JadbDevice
import se.vidstige.jadb.RemoteFile
import tornadofx.View
import java.io.File
import java.io.IOException
import javax.inject.Inject

class MainView : View() {
    override val root : StackPane by fxml("/layout/main.fxml")

    @Inject lateinit var jadb: JADB
    @Inject lateinit var notifBus: NotificationBus
    @Inject lateinit var spkManager: SpkManager

    val container : BorderPane by fxid("container")

    val devicesView: Node by fxid("devicesView")
    val createView: Node by fxid("createView")

    val deviceItem: ListItemMenu by fxid("deviceItem")
    val createItem: ListItemMenu by fxid("createItem")

    val notification: ListView<ListItemNotification> by fxid("notification")

    var lastView: Node = createView
    var lastItem: ListItemMenu = createItem

    companion object {
        lateinit var root: StackPane
    }

    init {
        title = "Sugarizer Deployment Tool - " + BuildConfig.VERSION

        Main.appComponent.inject(this)

        try {
            deviceItem.onMouseClicked = EventHandler { load(devicesView, deviceItem) }
            createItem.onMouseClicked = EventHandler { load(createView, createItem) }

            load(devicesView, deviceItem)

            notifBus.toObservable()
                    .observeOn(JavaFxScheduler.platform())
                    .subscribe {
                        var item = ListItemNotification(it)
                        var fadeIn = item.onItemAdded()
                        var fadeOut = item.onItemRemoved()

                        fadeOut.setOnFinished { Platform.runLater { notification.items.remove(item) } }
                        fadeIn.setOnFinished { fadeOut.play() }

                        notification.items.add(item)
                        fadeIn.play()
                    }

            MainView.root = root
        } catch (e: IOException) {
            e.printStackTrace()
        }

        spkManager.startWatching()
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