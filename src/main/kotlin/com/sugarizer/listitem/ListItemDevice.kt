package com.sugarizer.presentation.custom

import com.jfoenix.controls.JFXDialog
import com.jfoenix.controls.JFXRippler
import com.jfoenix.effects.JFXDepthManager
import com.sugarizer.domain.model.DeviceModel
import com.sugarizer.domain.shared.JADB
import com.sugarizer.main.Main
import com.sugarizer.presentation.view.devicedetails.view.devicedetails.DeviceDetailsPresenter
import javafx.animation.FadeTransition
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Label
import javafx.scene.layout.StackPane
import javafx.util.Duration
import tornadofx.useMaxSize
import java.io.IOException
import javax.inject.Inject

class ListItemDevice(val device: DeviceModel) : JFXRippler() {
    @FXML lateinit var nameLabel: Label
    @FXML lateinit var modelLabel: Label
    @FXML lateinit var indicator: StackPane
    @FXML lateinit var state: Label

    @Inject lateinit var jadb: JADB

    enum class State {
        IDLE,
        WORKING
    }

    var isWorking = false
    var numberTask = 0
    var currentState = State.IDLE

    init {
        Main.appComponent.inject(this)

        val loader = FXMLLoader(javaClass.getResource("/layout/list-item/list-item-device.fxml"))

        loader.setRoot(this)
        loader.setController(this)

        try {
            loader.load<JFXRippler>()

            isVisible = false

            JFXDepthManager.setDepth(this, 8)

            nameLabel.text = device.name.get()
            modelLabel.text = device.model.get()

            state.text = currentState.toString()
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

    fun onItemAdded(): ListItemDevice {
        isVisible = true

        var fadeIn = FadeTransition(Duration.millis(250.0), this)

        fadeIn.fromValue = 0.0
        fadeIn.toValue = 1.0

        fadeIn.play()

        return this
    }

    fun onItemStartWorking() {
        numberTask++

        if (numberTask == 1) {
            Platform.runLater {
                indicator.isVisible = true

                var fadeIn = FadeTransition(Duration.millis(250.0), indicator)

                fadeIn.fromValue = 0.0
                fadeIn.toValue = 1.0

                fadeIn.play()

                isWorking = true
            }
        }
    }

    fun onItemStopWorking() {
        numberTask--

        if (numberTask == 0) {
            Platform.runLater {
                var fadeOut = FadeTransition(Duration.millis(250.0), indicator)

                fadeOut.fromValue = 1.0
                fadeOut.toValue = 0.0

                fadeOut.play()

                isWorking = false
            }
        }
    }

    fun onItemRemoved(): FadeTransition {
        var fadeOut = FadeTransition(Duration.millis(250.0), this)

        fadeOut.fromValue = 1.0
        fadeOut.toValue = 0.0

        return fadeOut
    }
}