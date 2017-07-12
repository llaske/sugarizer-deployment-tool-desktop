package view.main

import com.jfoenix.controls.JFXRippler
import com.sugarizer.BuildConfig
import javafx.scene.layout.BorderPane
import com.sugarizer.domain.shared.JADB
import com.sugarizer.main.Main
import com.sugarizer.presentation.custom.ListItemDevice
import com.sugarizer.presentation.custom.ListMenuItem
import com.sugarizer.presentation.view.device.DevicesView
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

    val container : BorderPane by fxid(propName = "container")

    val inventory : ListMenuItem by fxid(propName = "inventorytets")
    //val devices : ListMenuItem by fxid(propName = "devices")
    val application : ListMenuItem by fxid(propName = "appManager")
    val createInstruction : ListMenuItem by fxid(propName = "createInstruction")
    val loadInstruction: ListMenuItem by fxid(propName = "loadInstruction")
    val synchronisation: ListMenuItem by fxid(propName = "synchronisation")
    val home: ListMenuItem by fxid(propName = "home")
    val devices: GridView<ListItemDevice> by fxid("devices")

    val deviceButton: JFXRippler by fxid("deviceButton")
    val inventoryButton: JFXRippler by fxid("inventoryButton")
    val loadButton: JFXRippler by fxid("loadButton")
    val createButton: JFXRippler by fxid("createButton")
    val applicationButton: JFXRippler by fxid("applicationButton")

    val devicesView: Node by fxid("devicesView")
    val inventoryView: Node by fxid("inventoryView")
    val loadView: Node by fxid("loadView")
    val createView: Node by fxid("createView")
    val applicationView: Node by fxid("applicationView")

    val deviceBackground: Node by fxid("deviceBackground")
    val inventoryBackground: Node by fxid("inventoryBackground")
    val loadBackground: Node by fxid("loadBackground")
    val createBackground: Node by fxid("createBackground")
    val applicationBackground: Node by fxid("applicationBackground")

    var lastItem: ListMenuItem? = null
    var lastView: Node = devicesView
    var lastBackground: Node = deviceBackground

    private val views = mutableMapOf<Views, KClass<View>>()

    private enum class Views {
        DEVICES,
        INVENTORY,
        APP_MANAGER,
        CREATE_INSTRUCTION,
        LOAD_INSTRUCTION,
        SYNCHRONISATION,
        HOME;

        override fun toString(): String {
            when (this) {
                DEVICES -> return "Devices"
                INVENTORY -> return "Inventory"
            }
            return super.toString()
        }
    }

    init {
        title = "Sugarizer Deloyment Tool - " + BuildConfig.VERSION

        Main.appComponent.inject(this)

        try {
            deviceButton.onMouseClicked = EventHandler { load(devicesView, deviceBackground) }
            inventoryButton.onMouseClicked = EventHandler { load(inventoryView, inventoryBackground) }
            applicationButton.onMouseClicked = EventHandler { load(applicationView, applicationBackground) }
            loadButton.onMouseClicked = EventHandler { load(loadView, loadBackground) }
            createButton.onMouseClicked = EventHandler { load(createView, createBackground) }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun load(viewIn: Node, button: Node) {
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

            lastBackground.style = "-fx-background-color: #FFFFFF;"
            lastBackground = button
            button.style = "-fx-background-color: #808080;"
        }

        fadeIn.play()
        fadeOut.play()
    }

    private fun load(view: Views, item: ListMenuItem) {
        if (views.containsKey(view)) {
            with(container) {
                center = find((views[view]) as KClass<View>).root

                if (view.equals(Views.DEVICES)) {
                    var tmp = (find((views[view]) as KClass<View>) as DevicesView)
                    //devices.numberLabel.textProperty().bind(Bindings.size(tmp.tableDevice.items).asString())
                }
            }
        } else {
            println("Error: key not found in map")
        }

        lastItem?.let {
            it.setSelected(false)
        }

        item.setSelected(true)
        lastItem = item
    }
}