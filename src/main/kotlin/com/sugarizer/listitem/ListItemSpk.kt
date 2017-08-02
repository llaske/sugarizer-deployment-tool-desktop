package com.sugarizer.presentation.custom

import com.jfoenix.controls.JFXRippler
import com.jfoenix.effects.JFXDepthManager
import javafx.animation.FadeTransition
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Label
import javafx.scene.layout.StackPane
import javafx.util.Duration
import java.io.File
import java.io.IOException

class ListItemSpk(val file: File) : StackPane() {
    @FXML lateinit var nameLabel: Label
    @FXML lateinit var flash: JFXRippler

    init {
        val loader = FXMLLoader(javaClass.getResource("/layout/list-item/list-item-spk.fxml"))

        loader.setRoot(this)
        loader.setController(this)

        try {
            loader.load<StackPane>()

            isVisible = false

            JFXDepthManager.setDepth(this, 4)

            nameLabel.text = file.nameWithoutExtension

            flash.onMouseClicked = EventHandler { println("Clicked !") }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun onItemAdded(): ListItemSpk {
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