package com.sugarizer.presentation.view.appmanager

import javafx.scene.Parent
import javafx.scene.control.Button
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import javafx.stage.DirectoryChooser
import tornadofx.View
import java.io.File

class AppManagerView : View() {
    override val root: GridPane by fxml("/layout/view-app-manager.fxml")

    val chooser: Button by fxid("chooseRepository")
    val repositoryArea: TextField by fxid("repository")

    init {
        chooser.setOnAction {
            var directory = DirectoryChooser()
            directory.title = "Choose the apk directory"
            var choosedDirectory: File = directory.showDialog(primaryStage)
            repositoryArea.text = choosedDirectory.absolutePath
        }
    }
}