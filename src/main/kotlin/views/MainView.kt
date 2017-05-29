package views

import javafx.scene.control.Button
import javafx.scene.layout.BorderPane
import javafx.scene.layout.GridPane
import tornadofx.View
import tornadofx.action
import utils.JADB
import java.io.IOException
import javax.inject.Inject
import kotlin.reflect.KClass

class MainView : View() {
    override val root : GridPane by fxml("/layout/main.fxml")

    @Inject val jadb = JADB()

    val container : BorderPane by fxid(propName = "container")

    val inventory : Button by fxid(propName = "inventory")
    val devices : Button by fxid(propName = "devices")
    val application : Button by fxid(propName = "appManager")

    private val views = mutableMapOf<Views, KClass<View>>()

    private enum class Views {
        DEVICES,
        INVENTORY;

        override fun toString(): String {
            when (this) {
                DEVICES -> return "Devices"
                INVENTORY -> return "Inventory"
            }
            return super.toString()
        }
    }

    init {
        title = "Sugarizer Deloyement Tool"

        try {
            views.put(Views.DEVICES, DevicesView::class as KClass<View>)
            views.put(Views.INVENTORY, InventoryView::class as KClass<View>)

            with(inventory) { action { load(Views.INVENTORY) } }
            with(devices) { action { load(Views.DEVICES) } }
            with(application) { action {
                if (jadb.numberDevice() > 0) {
                    println("Changing state")

                    jadb.changeAction(0, "Doing something")
                }
            }}

            load(Views.DEVICES)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun load(view: Views) {
        if (views!!.containsKey(view)) {
            with(container) {
                center = find((views!![view]) as KClass<View>).root
            }
        } else {
            println("Error: key not found in map")
        }
    }
}