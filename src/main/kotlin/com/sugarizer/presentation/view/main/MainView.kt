package view.main

import com.sugarizer.BuildConfig
import javafx.scene.layout.BorderPane
import com.sugarizer.domain.shared.JADB
import com.sugarizer.domain.shared.database.DBUtil
import com.sugarizer.domain.shared.database.FileSynchroniser
import com.sugarizer.main.Main
import com.sugarizer.presentation.custom.ListItemDevice
import com.sugarizer.presentation.custom.ListItemMenu
import io.reactivex.schedulers.Schedulers
import javafx.animation.FadeTransition
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.layout.StackPane
import javafx.util.Callback
import javafx.util.Duration
import org.controlsfx.control.GridCell
import org.controlsfx.control.GridView
import tornadofx.*
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

    val container : BorderPane by fxid("container")

    val devicesView: Node by fxid("devicesView")
    val loadView: Node by fxid("loadView")
    val createView: Node by fxid("createView")
    val synchronisationView: Node by fxid("synchronisationView")

    val deviceItem: ListItemMenu by fxid("deviceItem")
    val loadItem: ListItemMenu by fxid("loadItem")
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
                .subscribe()

        try {
            deviceItem.onMouseClicked = EventHandler { load(devicesView, deviceItem) }
            synchronisationItem.onMouseClicked = EventHandler { load(synchronisationView, synchronisationItem) }
            createItem.onMouseClicked = EventHandler { load(createView, createItem) }

            load(devicesView, deviceItem)
        } catch (e: IOException) {
            e.printStackTrace()
        }
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