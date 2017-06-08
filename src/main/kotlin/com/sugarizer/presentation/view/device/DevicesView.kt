package view.device

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
import com.sugarizer.presentation.view.device.DeviceContract
import com.sugarizer.presentation.view.device.DevicePresenter
import javafx.scene.control.Button
import javafx.scene.control.TableCell
import javafx.util.Callback
import tornadofx.*
import javax.inject.Inject

class DevicesView : View(), DeviceContract.View {
    override fun onDeviceAdded(deviceEventModel: DeviceEventModel) {
        tableDevice.items.add(deviceEventModel.device)
    }

    override fun onDeviceChanged(deviceEventModel: DeviceEventModel) {
        tableDevice.items.filter {
            it.jadbDevice.serial.equals(deviceEventModel.device.jadbDevice.serial) }
                .forEach {
                    it.setAction(deviceEventModel.device.action.get())
                    it.setName(deviceEventModel.device.name.get())
                    it.setStatus(deviceEventModel.device.status.get())
                    it.setDevice(deviceEventModel.device.jadbDevice)
                }
    }

    override fun onDeviceRemoved(deviceEventModel: DeviceEventModel) {
        tableDevice.items.filter {
            it.name.get().equals(deviceEventModel.device.name.get()) }
                .forEach {
                    tableDevice.items.remove(it)
                }
    }

    override fun test() {

    }

    override val root: GridPane by fxml("/layout/view-devices.fxml")

    @Inject lateinit var jadb: JADB
    @Inject lateinit var bus: RxBus
    @Inject lateinit var stringUtils: StringUtils

    val columnName: TableColumn<DeviceModel, String> by fxid(propName = "columnName")
    val columnStatus: TableColumn<DeviceModel, String> by fxid(propName = "columnStatus")
    val columnAction: TableColumn<DeviceModel, String> by fxid(propName = "columnAction")
    val columnPing: TableColumn<DeviceModel, String> by fxid(propName = "columnPing")

    val tableDevice: TableView<DeviceModel> by fxid(propName = "deviceTable")

    val presenter: DevicePresenter

    init {
        Main.appComponent.inject(this)

        presenter = DevicePresenter(this, jadb, bus, stringUtils)

        columnName.cellValueFactory = PropertyValueFactory<DeviceModel, String>("name")
        columnStatus.cellValueFactory = PropertyValueFactory<DeviceModel, String>("status")
        columnAction.cellValueFactory = PropertyValueFactory<DeviceModel, String>("action")
        columnPing.cellValueFactory = PropertyValueFactory<DeviceModel, String>("ping")

        presenter.start()

        columnPing.cellFactory = Callback {
            val button = Button()

            button.maxWidth = Double.MAX_VALUE
            button.maxHeight = Double.MAX_VALUE

            object : TableCell<DeviceModel, String>() {
                override fun updateItem(item: String?, empty: Boolean) {
                    super.updateItem(item, empty)

                    if (item == null) {
                        graphic = null
                    } else {
                        button.text = "Ping"
                        button.onAction = presenter.onPingClick(rowItem)

                        graphic = button
                    }
                }
            }
        }

        with(tableDevice) {
            onDoubleClick {
                selectedItem?.also {
                    modalStage?.let {
                        stage -> presenter.onTableRowDoubleClick(it, stage)
                    }
                }
            }
        }
    }

}