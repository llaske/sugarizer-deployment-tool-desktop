package com.sugarizer.presentation.custom

import com.jfoenix.controls.JFXListView
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
    @FXML lateinit var list: JFXListView<String>

    var type: SynchronisationView.Type? = null

    init {
        val loader = FXMLLoader(javaClass.getResource("/layout/custom-synchronisation-tab.fxml"))

        loader.setRoot(this)
        loader.setController(this)

        try {
            loader.load<GridPane>()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}