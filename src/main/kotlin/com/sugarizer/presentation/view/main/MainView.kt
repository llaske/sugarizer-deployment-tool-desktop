package view.main

import com.sugarizer.BuildConfig
import javafx.scene.control.Button
import javafx.scene.layout.BorderPane
import javafx.scene.layout.GridPane
import com.sugarizer.domain.shared.JADB
import com.sugarizer.main.Main
import com.sugarizer.presentation.view.appmanager.AppManagerView
import tornadofx.View
import tornadofx.action
import view.device.DevicesView
import view.inventory.InventoryView
import java.io.IOException
import javax.inject.Inject
import kotlin.reflect.KClass

class MainView : View() {
    override val root : GridPane by fxml("/layout/main.fxml")

    @Inject lateinit var jadb: JADB

    val container : BorderPane by fxid(propName = "container")

    val inventory : Button by fxid(propName = "inventory")
    val devices : Button by fxid(propName = "devices")
    val application : Button by fxid(propName = "appManager")

    private val views = mutableMapOf<Views, KClass<View>>()

    private enum class Views {
        DEVICES,
        INVENTORY,
        APP_MANAGER;

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
            views.put(Views.DEVICES, DevicesView::class as KClass<View>)
            views.put(Views.INVENTORY, InventoryView::class as KClass<View>)
            views.put(Views.APP_MANAGER, AppManagerView::class as KClass<View>)

            with(inventory) { action { load(Views.INVENTORY) } }
            with(devices) { action { load(Views.DEVICES) } }
            with(application) { action { load(Views.APP_MANAGER) }}

            load(Views.DEVICES)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun load(view: Views) {
        if (views.containsKey(view)) {
            with(container) {
                center = find((views[view]) as KClass<View>).root
            }
        } else {
            println("Error: key not found in map")
        }
    }
}