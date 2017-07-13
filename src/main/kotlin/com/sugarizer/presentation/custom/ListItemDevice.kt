package com.sugarizer.presentation.custom

import com.jfoenix.controls.JFXDialog
import com.jfoenix.controls.JFXRippler
import com.jfoenix.effects.JFXDepthManager
import com.sugarizer.domain.model.DeviceModel
import com.sugarizer.main.Main
import com.sugarizer.presentation.view.devicedetails.view.devicedetails.DeviceDetailsPresenter
import com.sugarizer.presentation.view.devicedetails.view.devicedetails.DeviceDetailsView
import javafx.application.Platform
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.ContextMenu
import javafx.scene.control.Label
import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
import javafx.scene.layout.BackgroundImage
import javafx.scene.layout.StackPane
import tornadofx.show
import tornadofx.useMaxSize
import java.io.IOException

class ListItemDevice(val device: DeviceModel) : JFXRippler() {
    @FXML lateinit var nameLabel: Label
    @FXML lateinit var serialLabel: Label
    @FXML lateinit var modelLabel: Label
    @FXML lateinit var versionLabel: Label

    init {
        val loader = FXMLLoader(javaClass.getResource("/layout/list-item/list-item-device.fxml"))

        loader.setRoot(this)
        loader.setController(this)

        try {
            loader.load<JFXRippler>()

            JFXDepthManager.setDepth(this, 8)

            nameLabel.text = device.name.get()
            serialLabel.text = device.serial.get()
            modelLabel.text = device.model.get()
            //versionLabel.text = device.version.get()

            val contextMenu = ContextMenu()
            contextMenu.style = "-fx-background-color: #ffffff; -fx-text-fill: #000000;"

            val install = MenuItem("Install APK")
            val details = MenuItem("Details")

            install.style = "-fx-text-fill: #000000; -fx-background-color: #FFFFFF;"
            details.style = "-fx-text-fill: #000000; -fx-background-color: #FFFFFF;"

            details.onAction = onClickDetails()

            contextMenu.items.addAll(install, details)

            setOnMousePressed { event -> run {
                    if (event.isSecondaryButtonDown) {
                        contextMenu.show(this, event.screenX, event.screenY)
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun onClickDetails(): EventHandler<ActionEvent> {
        return EventHandler {
            println("Show - 1")
            var dialog = JFXDialog(null, DeviceDetailsPresenter(device), JFXDialog.DialogTransition.TOP)
            dialog.useMaxSize = false

            dialog.show()
            println("Show - 2")
        }
    }
}