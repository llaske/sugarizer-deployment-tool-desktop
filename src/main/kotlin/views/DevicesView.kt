package views

import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.GridPane
import model.Device
import tornadofx.View
import utils.JADB
import javax.inject.Inject

class DevicesView : View() {
    override val root: GridPane by fxml("/layout/view-devices.fxml")

    @Inject val jadb = JADB()

    val columnName: TableColumn<Device, String> by fxid(propName = "columnName")
    val columnStatus: TableColumn<Device, String> by fxid(propName = "columnStatus")
    val columnAction: TableColumn<Device, String> by fxid(propName = "columnAction")

    val tableDevice: TableView<Device> by fxid(propName = "deviceTable")

    init {
        println("Device View initialize")

        println("Number Devices connected: " + jadb.numberDevice())

        columnName.cellValueFactory = PropertyValueFactory<Device, String>("name")
        columnStatus.cellValueFactory = PropertyValueFactory<Device, String>("status")
        columnAction.cellValueFactory = PropertyValueFactory<Device, String>("action")

        tableDevice.items = jadb.getDevices()
    }
}