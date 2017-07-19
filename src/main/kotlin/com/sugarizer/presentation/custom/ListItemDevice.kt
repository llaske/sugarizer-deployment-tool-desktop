package com.sugarizer.presentation.custom

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXDialog
import com.jfoenix.controls.JFXRippler
import com.jfoenix.effects.JFXDepthManager
import com.sugarizer.domain.model.DeviceModel
import com.sugarizer.domain.shared.JADB
import com.sugarizer.main.Main
import com.sugarizer.presentation.view.devicedetails.view.devicedetails.DeviceDetailsPresenter
import com.sugarizer.presentation.view.devicedetails.view.devicedetails.DeviceDetailsView
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import javafx.animation.FadeTransition
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
import javafx.scene.input.MouseEvent
import javafx.scene.layout.BackgroundImage
import javafx.scene.layout.GridPane
import javafx.scene.layout.StackPane
import javafx.util.Duration
import tornadofx.observable
import tornadofx.onDoubleClick
import tornadofx.show
import tornadofx.useMaxSize
import java.io.IOException
import javax.inject.Inject

class ListItemDevice(val device: DeviceModel) : JFXRippler() {
    @FXML lateinit var nameLabel: Label
    @FXML lateinit var serialLabel: Label
    @FXML lateinit var modelLabel: Label
    @FXML lateinit var versionLabel: Label
    @FXML lateinit var indicator: StackPane
    @FXML lateinit var deviceItem: GridPane
    @FXML lateinit var ping: StackPane
    @FXML lateinit var pingOk: JFXButton

    @Inject lateinit var jadb: JADB

    var isWorking = false
    var numberTask = 0

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
            serialLabel.text = device.serial.get()
            modelLabel.text = device.model.get()
            //versionLabel.text = device.version.get()

            val contextMenu = ContextMenu()
            contextMenu.style = "-fx-background-color: #ffffff; -fx-text-fill: #000000;"

            val install = MenuItem("Install APK")
            val details = MenuItem("Details")
            val pingItem = MenuItem("Ping")

            install.style = "-fx-text-fill: #000000; -fx-background-color: #FFFFFF;"
            details.style = "-fx-text-fill: #000000; -fx-background-color: #FFFFFF;"
            pingItem.style = "-fx-text-fill: #000000; -fx-background-color: #FFFFFF;"

            details.onAction = onClickDetails()
            pingItem.onAction = onPing()

            contextMenu.items.addAll(pingItem, install, details)

            pingOk.onAction = onClickOkPing()

            setOnMousePressed { event -> run {
                    if (event.isSecondaryButtonDown) {
                        contextMenu.show(this, event.screenX, event.screenY)
                    }
                }
            }

            onDoubleClick { onPing().handle(ActionEvent()) }

            numberTask
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

    fun onPing(): EventHandler<ActionEvent> {
        return EventHandler {
            if (!isWorking) {
                onItemStartWorking()

                jadb.hasPackage(device.jadbDevice, "com.sugarizer.sugarizerdeploymenttoolapp")
                        .subscribeOn(Schedulers.computation())
                        .doOnComplete {
                            jadb.ping(device.jadbDevice)
                                    .subscribeOn(Schedulers.computation())
                                    .observeOn(JavaFxScheduler.platform())
                                    .doOnComplete { onItemPinged() }
                                    .subscribe()
                        }
                        .subscribe()
            }
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

    fun onItemPinged() {
        ping.isVisible = true
        var fadeIn = FadeTransition(Duration.millis(250.0), ping)
        fadeIn.fromValue = 0.0
        fadeIn.toValue = 1.0

        var fadeOut = FadeTransition(Duration.millis(250.0), indicator)
        fadeOut.fromValue = 1.0
        fadeOut.toValue = 0.0

        fadeIn.setOnFinished {
            indicator.isVisible = false
            isWorking = false
        }

        fadeIn.play()
        fadeOut.play()
    }

    fun onClickOkPing(): EventHandler<ActionEvent> {
        return EventHandler {
            var fadeOut = FadeTransition(Duration.millis(250.0), ping)

            fadeOut.fromValue = 1.0
            fadeOut.toValue = 0.0

            fadeOut.play()
        }
    }
}