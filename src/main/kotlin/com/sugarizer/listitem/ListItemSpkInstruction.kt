package com.sugarizer.listitem

import com.jfoenix.controls.JFXRippler
import com.jfoenix.effects.JFXDepthManager
import com.sugarizer.view.createinstruction.CreateInstructionContract
import com.sugarizer.view.device.DeviceContract
import javafx.animation.FadeTransition
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Label
import javafx.scene.layout.GridPane
import javafx.scene.layout.StackPane
import javafx.util.Duration
import java.io.File
import java.io.IOException

class ListItemSpkInstruction(val file: File, val presenter: CreateInstructionContract.Presenter, isFake: Boolean) : StackPane() {
    @FXML lateinit var nameLabel: Label
    @FXML lateinit var flash: JFXRippler
    @FXML lateinit var addNewSpk: StackPane
    @FXML lateinit var deviceItem: GridPane

    init {
        val loader = FXMLLoader(javaClass.getResource("/layout/list-item/list-item-spk-instruction.fxml"))

        loader.setRoot(this)
        loader.setController(this)

        try {
            loader.load<StackPane>()

            if (!isFake) {
                isVisible = false

                JFXDepthManager.setDepth(this, 4)

                nameLabel.text = file.nameWithoutExtension
            } else {
                addNewSpk.isVisible = true
                deviceItem.isVisible = false
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun onItemAdded(): ListItemSpkInstruction {
        isVisible = true

        var fadeIn = FadeTransition(Duration.millis(250.0), this)

        fadeIn.fromValue = 0.0
        fadeIn.toValue = 1.0

        fadeIn.play()

        return this
    }

    fun onItemRemoved(): FadeTransition {
        var fadeOut = FadeTransition(Duration.millis(250.0), this)

        fadeOut.fromValue = 1.0
        fadeOut.toValue = 0.0

        return fadeOut
    }
}