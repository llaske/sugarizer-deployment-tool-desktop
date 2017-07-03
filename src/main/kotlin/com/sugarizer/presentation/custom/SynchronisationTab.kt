package com.sugarizer.presentation.custom

import com.sugarizer.presentation.view.synchronisation.SynchronisationView
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.ListView
import javafx.scene.layout.GridPane
import javafx.stage.DirectoryChooser
import tornadofx.FX
import tornadofx.selectedItem
import java.io.IOException

class SynchronisationTab : GridPane() {
    @FXML lateinit var list: ListView<String>
    @FXML lateinit var addButton: Button
    @FXML lateinit var removeButton: Button

    var type: SynchronisationView.Type? = null

    init {
        val loader = FXMLLoader(javaClass.getResource("/layout/custom-synchronisation-tab.fxml"))

        loader.setRoot(this)
        loader.setController(this)

        try {
            loader.load<GridPane>()

            addButton.onAction = EventHandler {
                var directoryChooser = DirectoryChooser()
                directoryChooser.title = "Choose a repository for " + type.toString()
                var directorySelected = directoryChooser.showDialog(scene.window)

                list.items.add(directorySelected.absolutePath)
            }

            removeButton.onAction = EventHandler {
                if (list.selectedItem != null) {
                    list.items.remove(list.selectedItem)
                } else {
                    var alert = Alert(Alert.AlertType.ERROR)

                    alert.title = "Error"
                    alert.headerText = null
                    alert.contentText = "Select a directory"

                    alert.showAndWait()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}