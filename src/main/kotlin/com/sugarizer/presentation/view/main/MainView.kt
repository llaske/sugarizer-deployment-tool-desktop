package view.main

import com.sugarizer.BuildConfig
import javafx.scene.layout.BorderPane
import com.sugarizer.domain.shared.JADB
import com.sugarizer.main.Main
import com.sugarizer.presentation.custom.ListItemDevice
import com.sugarizer.presentation.custom.ListItemMenu
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
    override val root : BorderPane by fxml("/layout/main.fxml")

    @Inject lateinit var jadb: JADB

    val container : BorderPane by fxid("container")

    val devicesView: Node by fxid("devicesView")
    val inventoryView: Node by fxid("inventoryView")
    val loadView: Node by fxid("loadView")
    val createView: Node by fxid("createView")
    val applicationView: Node by fxid("applicationView")

    val deviceItem: ListItemMenu by fxid("deviceItem")
    val applicationItem: ListItemMenu by fxid("applicationItem")
    val loadItem: ListItemMenu by fxid("loadItem")
    val createItem: ListItemMenu by fxid("createItem")
    val inventoryItem: ListItemMenu by fxid("inventoryItem")

    var lastView: Node = applicationView
    var lastItem: ListItemMenu = applicationItem

    init {
        title = "Sugarizer Deloyment Tool - " + BuildConfig.VERSION

        Main.appComponent.inject(this)

        try {
            deviceItem.onMouseClicked = EventHandler { load(devicesView, deviceItem) }
            inventoryItem.onMouseClicked = EventHandler { load(inventoryView, inventoryItem) }
            applicationItem.onMouseClicked = EventHandler { load(applicationView, applicationItem) }
            loadItem.onMouseClicked = EventHandler { load(loadView, loadItem) }
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