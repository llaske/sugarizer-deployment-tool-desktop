package com.sugarizer.presentation.view.device

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXCheckBox
import com.jfoenix.controls.JFXDialog
import com.jfoenix.controls.JFXListView
import com.sugarizer.domain.shared.JADB
import com.sugarizer.main.Main
import com.sugarizer.presentation.custom.ListItemDevice
import io.reactivex.Observable
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Alert
import javafx.scene.control.Label
import javafx.scene.input.MouseEvent
import javafx.scene.layout.StackPane
import javafx.stage.FileChooser
import tornadofx.selectedItem
import java.io.File
import java.io.IOException
import javax.inject.Inject

class DeviceSideMenu(val view: DeviceContract.View) : StackPane() {
    @FXML lateinit var sideList: JFXListView<Label>
    @FXML lateinit var installAPKDialog: JFXDialog
    @FXML lateinit var launchInstallAPK: JFXButton
    @FXML lateinit var closeDrawer: JFXButton
    @FXML lateinit var selectFiles: JFXButton
    @FXML lateinit var forceInstall: JFXCheckBox

    @Inject lateinit var jadb: JADB

    val listAPKs: MutableList<File> = mutableListOf()

    init {
        Main.appComponent.inject(this)

        val loader = FXMLLoader(javaClass.getResource("/layout/menu-device.fxml"))

        loader.setRoot(this)
        loader.setController(this)

        try {
            loader.load<StackPane>()

            sideList.onMouseClicked = onMenuItemClicked()
            closeDrawer.setOnAction { view.closeDrawer() }
            selectFiles.onAction = onInstallSelectFile()
            launchInstallAPK.onAction = onInstallLaunch()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun onMenuItemClicked(): EventHandler<MouseEvent> {
        return EventHandler {
            when (sideList.selectedItem?.id) {
                "installAPK" -> {
                    installAPKDialog.transitionType = JFXDialog.DialogTransition.CENTER
                    installAPKDialog.show(view.getParent())
                    view.closeDrawer()
                }
            }
        }
    }

    fun onInstallSelectFile(): EventHandler<ActionEvent> {
        return EventHandler {
            var fileChooser = FileChooser()
            var listFiles = fileChooser.showOpenMultipleDialog(Main.primaryStage)

            if (listFiles != null) {
                selectFiles.text = listFiles.size.toString() + " apks"

                Observable.create<File> { subscriber -> run {
                        listFiles?.let {
                            for (i in listFiles.indices) {
                                if (listFiles[i].isFile) {
                                    if (listFiles[i].extension.equals("apk")) {
                                        listAPKs.add(listFiles[i])
                                    }
                                } else if (listFiles[i].isDirectory) {
                                    println("Directory " + listFiles[i].name)
                                }
                            }
                        }

                        subscriber.onComplete()
                    }
                }
                        .subscribeOn(Schedulers.computation())
                        .observeOn(JavaFxScheduler.platform())
                        .subscribe()
            } else {
                val alert = Alert(Alert.AlertType.ERROR)

                alert.title = "Error"
                alert.headerText = null
                alert.contentText = "No files selected"

                alert.showAndWait()
            }
        }
    }

    fun onInstallLaunch(): EventHandler<ActionEvent> {
        return EventHandler {
            println("ListAPK :" + listAPKs.size)
            installAPKDialog.close()

            Observable.create<Any> {
                view.getDevices().forEach {
                    it.onItemStartWorking()
                    installAPK(it, 0)
                }
            }
                    .subscribeOn(Schedulers.computation())
                    .observeOn(JavaFxScheduler.platform())
                    .doOnComplete {
                        selectFiles.text = "Select Files"
                        forceInstall.isSelected = false
                        listAPKs.clear()
                    }
                    .subscribe()
        }
    }

    fun installAPK(device: ListItemDevice, index: Int){
        jadb.installAPK(device.device.jadbDevice, listAPKs[index], forceInstall.isSelected)
                .subscribeOn(Schedulers.computation())
                .observeOn(JavaFxScheduler.platform())
                .doOnError { device.onItemStopWorking() }
                .doOnComplete {
                    if (index.equals(listAPKs.size - 1)) {
                        device.onItemStopWorking()
                    } else {
                        installAPK(device, index + 1)
                    }
                }
                .subscribe()
    }
}