package view.main

import com.sugarizer.BuildConfig
import javafx.scene.layout.BorderPane
import javafx.scene.layout.GridPane
import com.sugarizer.domain.shared.JADB
import com.sugarizer.main.Main
import com.sugarizer.presentation.custom.ListMenuItem
import com.sugarizer.presentation.view.appmanager.AppManagerView
import com.sugarizer.presentation.view.createinstruction.CreateInstructionView
import com.sugarizer.presentation.view.device.DevicesView
import com.sugarizer.presentation.view.home.HomeView
import com.sugarizer.presentation.view.loadinstruction.LoadInstructionView
import com.sugarizer.presentation.view.synchronisation.SynchronisationView
import com.sun.javafx.css.converters.BooleanConverter
import io.reactivex.rxkotlin.toObservable
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleIntegerProperty
import javafx.scene.layout.StackPane
import javafx.util.converter.NumberStringConverter
import tornadofx.View
import tornadofx.booleanBinding
import tornadofx.find
import tornadofx.integerBinding
import view.inventory.InventoryView
import java.io.IOException
import javax.inject.Inject
import kotlin.reflect.KClass

class MainView : View() {
    override val root : StackPane by fxml("/layout/main.fxml")

    @Inject lateinit var jadb: JADB

    val container : BorderPane by fxid(propName = "container")

    val inventory : ListMenuItem by fxid(propName = "inventory")
    val devices : ListMenuItem by fxid(propName = "devices")
    val application : ListMenuItem by fxid(propName = "appManager")
    val createInstruction : ListMenuItem by fxid(propName = "createInstruction")
    val loadInstruction: ListMenuItem by fxid(propName = "loadInstruction")
    val synchronisation: ListMenuItem by fxid(propName = "synchronisation")
    val home: ListMenuItem by fxid(propName = "home")

    var lastItem: ListMenuItem? = null

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
//            views.put(Views.DEVICES, DevicesView::class as KClass<View>)
//            views.put(Views.INVENTORY, InventoryView::class as KClass<View>)
//            views.put(Views.APP_MANAGER, AppManagerView::class as KClass<View>)
//            views.put(Views.CREATE_INSTRUCTION, CreateInstructionView::class as KClass<View>)
//            views.put(Views.LOAD_INSTRUCTION, LoadInstructionView::class as KClass<View>)
//            views.put(Views.SYNCHRONISATION, SynchronisationView::class as KClass<View>)
//            views.put(Views.HOME, HomeView::class as KClass<View>)
//
//            devices.setOnMouseClicked { load(Views.DEVICES, devices) }
//            inventory.setOnMouseClicked { load(Views.INVENTORY, inventory) }
//            application.setOnMouseClicked { load(Views.APP_MANAGER, application) }
//            createInstruction.setOnMouseClicked { load(Views.CREATE_INSTRUCTION, createInstruction) }
//            loadInstruction.setOnMouseClicked { load(Views.LOAD_INSTRUCTION, loadInstruction) }
//            synchronisation.setOnMouseClicked { load(Views.SYNCHRONISATION, synchronisation) }
//            home.setOnMouseClicked { load(Views.HOME, home) }
//
//            devices.numberLabel.textProperty().bind(Bindings.size((find((views[Views.DEVICES]) as KClass<View>) as DevicesView).tableDevice.items).asString())
//            application.numberLabel.textProperty().bind(Bindings.size((find((views[Views.APP_MANAGER]) as KClass<View>) as AppManagerView).listView.items).asString())
//            createInstruction.progress.visibleProperty().bind((find((views[Views.CREATE_INSTRUCTION]) as KClass<View>) as CreateInstructionView).inWork)
//            loadInstruction.progress.visibleProperty().bind((find((views[Views.LOAD_INSTRUCTION]) as KClass<View>) as LoadInstructionView).inWork)
//
//            load(Views.HOME, home)
//
//            home.setSelected(true)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun load(view: Views, item: ListMenuItem) {
        if (views.containsKey(view)) {
            with(container) {
                center = find((views[view]) as KClass<View>).root

                if (view.equals(Views.DEVICES)) {
                    var tmp = (find((views[view]) as KClass<View>) as DevicesView)
                    devices.numberLabel.textProperty().bind(Bindings.size(tmp.tableDevice.items).asString())
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