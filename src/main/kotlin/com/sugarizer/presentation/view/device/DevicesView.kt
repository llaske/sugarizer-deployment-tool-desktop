package com.sugarizer.presentation.view.device

import com.sugarizer.main.Main
import io.reactivex.schedulers.Schedulers
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.GridPane
import javafx.stage.Stage
import com.sugarizer.domain.model.DeviceEventModel
import com.sugarizer.domain.model.DeviceModel
import com.sugarizer.domain.shared.JADB
import com.sugarizer.domain.shared.RxBus
import com.sugarizer.domain.shared.StringUtils
import com.sugarizer.presentation.custom.ListItemDevice
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.TableCell
import javafx.scene.layout.StackPane
import javafx.util.Callback
import org.controlsfx.control.GridView
import tornadofx.*
import view.main.ListItemDeviceCellFactory
import view.main.MainView
import java.net.URL
import java.util.*
import javax.inject.Inject

class DevicesView : Initializable, DeviceContract.View {
    @Inject lateinit var jadb: JADB
    @Inject lateinit var bus: RxBus
    @Inject lateinit var stringUtils: StringUtils

    @FXML lateinit var devices: GridView<ListItemDevice>

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        devices.cellHeight = 200.0
        devices.cellWidth = 150.0
        devices.cellFactory =  ListItemDeviceCellFactory()
    }

    override fun onDeviceAdded(deviceEventModel: DeviceEventModel) {
        Platform.runLater {
            devices.items.add(ListItemDevice(deviceEventModel.device))
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
                .forEach {
                    Platform.runLater {
                        devices.items.remove(it)
                        devices.items.size
                    }
                }
    }

    override fun test() {

    }

    //override val root: StackPane by fxml("/layout/view-devices.fxml")

//    val columnName: TableColumn<DeviceModel, String> by fxid(propName = "columnName")
//    val columnStatus: TableColumn<DeviceModel, String> by fxid(propName = "columnStatus")
//    val columnAction: TableColumn<DeviceModel, String> by fxid(propName = "columnAction")
//    val columnPing: TableColumn<DeviceModel, String> by fxid(propName = "columnPing")

//    val tableDevice: TableView<DeviceModel> by fxid(propName = "deviceTable")
//    val devices: GridView<ListItemDevice> by fxid("devices")

    val presenter: DevicePresenter

    init {
        Main.appComponent.inject(this)

        presenter = DevicePresenter(this, jadb, bus, stringUtils)

        presenter.start()
    }

    override fun get(): Stage {
        return Stage()
    }

    override fun getParent(): StackPane {
        return StackPane()
    }
}