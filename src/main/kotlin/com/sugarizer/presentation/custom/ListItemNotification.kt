package com.sugarizer.presentation.custom

import com.jfoenix.controls.JFXRippler
import com.jfoenix.effects.JFXDepthManager
import javafx.animation.FadeTransition
import javafx.animation.ParallelTransition
import javafx.animation.TranslateTransition
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Label
import javafx.scene.layout.StackPane
import javafx.util.Duration
import tornadofx.fade
import tornadofx.getDefaultConverter
import java.io.File
import java.io.IOException

class ListItemNotification(message: String) : StackPane() {
    @FXML lateinit var message: Label

    init {
        val loader = FXMLLoader(javaClass.getResource("/layout/list-item/list-item-notification.fxml"))

        loader.setRoot(this)
        loader.setController(this)

        try {
            loader.load<StackPane>()

            isVisible = false

            JFXDepthManager.setDepth(this, 4)

            this.message.text = message

            onItemAdded()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun onItemAdded(): FadeTransition {
        isVisible = true

        var fadeIn = FadeTransition(Duration.millis(250.0), this)

        fadeIn.fromValue = 0.0
        fadeIn.toValue = 1.0

        return fadeIn
    }

    fun onItemRemoved(): ParallelTransition {
        var parallel = ParallelTransition()

        var fadeOut = FadeTransition(Duration.millis(250.0), this)
        var translate = TranslateTransition(Duration.millis(250.0), this)

        fadeOut.fromValue = 1.0
        fadeOut.toValue = 0.0

        println("" + height)

        translate.toY = -25.0

        parallel.delay = Duration.millis(3000.0)
        //parallel.children.addAll(fadeOut, translate)
        parallel.children.add(fadeOut)

       return parallel
    }
}