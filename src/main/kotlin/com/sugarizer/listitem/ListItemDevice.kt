package com.sugarizer.listitem

import com.jfoenix.controls.JFXDialog
import com.jfoenix.controls.JFXRippler
import com.jfoenix.effects.JFXDepthManager
import com.sugarizer.model.DeviceModel
import com.sugarizer.utils.shared.JADB
import com.sugarizer.Main
import com.sugarizer.view.devicedetails.view.devicedetails.DeviceDetailsPresenter
import io.reactivex.Observable
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import javafx.animation.FadeTransition
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.JavaFXBuilderFactory
import javafx.scene.control.Label
import javafx.scene.layout.StackPane
import javafx.util.Duration
import tornadofx.onDoubleClick
import tornadofx.useMaxSize
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ListItemDevice(val device: DeviceModel) : JFXRippler() {
    @FXML lateinit var nameLabel: Label
    @FXML lateinit var modelLabel: Label
    @FXML lateinit var indicator: StackPane
    @FXML lateinit var state: Label

    @Inject lateinit var jadb: JADB

    enum class State {
        IDLE,
        WORKING,
        FINISH,
        UNAUTHORIZED
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

            onDoubleClick {
                var dialog = DeviceDetailsPresenter(device)

                dialog.show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
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

    fun changeState(state: State){
        if (!currentState.equals(state)) {
            currentState = state
            Platform.runLater { this.state.text = state.toString() }

            if (state == State.FINISH) {
                Observable.timer(10, TimeUnit.SECONDS)
                        .observeOn(JavaFxScheduler.platform())
                        .subscribe {
                            currentState = State.IDLE
                            Platform.runLater { this.state.text = State.IDLE.toString() }
                        }
            }
        }

        when (state) {
            State.IDLE -> isWorking = false
            State.WORKING -> isWorking = true
            State.FINISH -> isWorking = false
        }
    }
}