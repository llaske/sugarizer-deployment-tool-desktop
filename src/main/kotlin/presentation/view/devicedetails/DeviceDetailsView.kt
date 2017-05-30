package view.devicedetails

import javafx.scene.Parent
import domain.model.DeviceModel
import tornadofx.View

class DeviceDetailsView(device: DeviceModel) : View() {
    override val root: Parent by fxml("/layout/device-details.fxml")

    init {
        title = "Details of " + device.name.get()
    }
}