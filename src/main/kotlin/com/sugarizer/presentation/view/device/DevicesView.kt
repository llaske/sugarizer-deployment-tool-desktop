package com.sugarizer.presentation.view.device

import com.jfoenix.controls.*
import com.sugarizer.main.Main
import io.reactivex.schedulers.Schedulers
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.GridPane
import javafx.stage.Stage
import com.sugarizer.domain.model.DeviceEventModel
import com.sugarizer.domain.model.DeviceModel
import com.sugarizer.domain.shared.JADB
import com.sugarizer.domain.shared.RxBus
import com.sugarizer.domain.shared.StringUtils
import com.sugarizer.presentation.custom.ListItemDevice
import com.sugarizer.presentation.view.devicedetails.view.devicedetails.DeviceDetailsPresenter
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.layout.StackPane
import javafx.util.Callback
import org.controlsfx.control.GridView
import tornadofx.*
import view.main.ListItemDeviceCellFactory
import view.main.MainView
import java.net.URL
import java.util.*
import javax.inject.Inject
import javafx.scene.input.TransferMode
import javafx.scene.input.Dragboard
import java.io.File

class DevicesView : Initializable, DeviceContract.View {
    @Inject lateinit var jadb: JADB
    @Inject lateinit var bus: RxBus
    @Inject lateinit var stringUtils: StringUtils

    @FXML lateinit var devices: GridView<ListItemDevice>
    @FXML lateinit var root: StackPane
    @FXML lateinit var titleBurgerContainer: StackPane
    @FXML lateinit var titleBurger: JFXHamburger
    @FXML lateinit var optionsBurger: StackPane
    @FXML lateinit var drawer: JFXDrawer
    @FXML lateinit var dropZone: Label
    @FXML lateinit var instructionDialog: JFXDialog
    @FXML lateinit var listInstruction: JFXListView<String>
    @FXML lateinit var launchInstruction: JFXButton
    @FXML lateinit var cancelInstruction: JFXButton
    @FXML lateinit var inWork: StackPane

    val presenter: DevicePresenter

    init {
        Main.appComponent.inject(this)

        presenter = DevicePresenter(this, jadb, bus, stringUtils)
    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        devices.cellHeight = 150.0
        devices.cellWidth = 150.0
        devices.cellFactory =  ListItemDeviceCellFactory()

        drawer.setOnDrawerOpening { e ->
            val animation = titleBurger.animation
            animation.rate = 1.0
            animation.play()
        }

        drawer.setOnDrawerClosing { e ->
            val animation = titleBurger.animation
            animation.rate = -1.0
            animation.play()
        }

        titleBurgerContainer.setOnMouseClicked { e ->
            if (drawer.isHidden || drawer.isHidding) {
                drawer.open()
            } else {
                drawer.close()
            }
        }

        drawer.setSidePane(DeviceSideMenu(this))

        dropZone.onDragOver = presenter.onDragOver()
        dropZone.onDragDropped = presenter.onDropped()
        launchInstruction.onAction = presenter.onLaunchInstruction()
        cancelInstruction.onAction = presenter.onCancelInstruction()
    }

    override fun onDeviceAdded(deviceEventModel: DeviceEventModel) {
        Platform.runLater {
            devices.items.add(ListItemDevice(deviceEventModel.device).onItemAdded())
        }
    }

    override fun onDeviceChanged(deviceEventModel: DeviceEventModel) {
//        tableDevice.items.filter {
//            it.jadbDevice.serial.equals(deviceEventModel.device.jadbDevice.serial) }
//                .forEach {
//                    it.setAction(deviceEventModel.device.action.get())
//                    it.setName(deviceEventModel.device.name.get())
//                    it.setStatus(deviceEventModel.device.status.get())
//                    it.setDevice(deviceEventModel.device.jadbDevice)
//                }
    }

    override fun onDeviceRemoved(deviceEventModel: DeviceEventModel) {
        devices.items.filter { it.device.serial.get().equals(deviceEventModel.device.jadbDevice.serial) }
                .forEach { device -> run {
                        Platform.runLater {
                            var tmp = device.onItemRemoved()

                            tmp.setOnFinished {
                                devices.items.remove(device)
                                devices.items.size
                            }

                            tmp.play()
                        }
                    }
                }
    }

    override fun showDialog(list: List<String>) {
        println("Size: " + list.size)
        list.forEach { listInstruction.items.add(it) }

        instructionDialog.show(root)
    }

    override fun closeDialog() {
        instructionDialog.close()
    }

    override fun setInWork(boolean: Boolean) {
        inWork.isVisible = boolean
    }

    override fun getDevices(): List<ListItemDevice> {
        return devices.items
    }

    override fun get(): Stage {
        return Stage()
    }

    override fun getParent(): StackPane {
        return root
    }

    override fun closeDrawer(){
        drawer.close()
    }
}