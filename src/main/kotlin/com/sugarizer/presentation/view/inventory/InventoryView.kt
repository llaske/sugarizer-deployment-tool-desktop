package view.inventory

import javafx.scene.layout.GridPane
import tornadofx.View


class InventoryView : View() {
    override val root: GridPane by fxml("/layout/view-inventory.fxml")

    init {

    }
}