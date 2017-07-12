package com.sugarizer.presentation.custom

import com.jfoenix.controls.JFXRippler
import com.jfoenix.effects.JFXDepthManager
import com.sugarizer.domain.model.DeviceModel
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Label
import javafx.scene.layout.BackgroundImage
import javafx.scene.layout.StackPane
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
            versionLabel.text = device.version.get()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}