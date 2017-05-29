package views

import javafx.scene.Parent
import model.Device
import tornadofx.View

class DeviceDetailsView(device: Device) : View() {
    override val root: Parent by fxml("/layout/device-details.fxml")

    init {
        title = "Details of " + device.name.get()
    }
}