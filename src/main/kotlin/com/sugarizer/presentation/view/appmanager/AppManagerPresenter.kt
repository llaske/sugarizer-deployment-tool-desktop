package com.sugarizer.presentation.view.appmanager

import com.sugarizer.domain.shared.JADB
import com.sugarizer.presentation.custom.ListItemApplication
import io.reactivex.Observable
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.Alert
import javafx.scene.input.MouseEvent
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import javafx.stage.Stage
import net.dongliu.apk.parser.ApkFile
import se.vidstige.jadb.JadbDevice
import java.io.File

class AppManagerPresenter(val view: AppManagerContract.View, val jadb: JADB) : AppManagerContract.Presenter {
    val list: MutableList<File> = mutableListOf()

    override fun onChooseRepositoryClick(primaryStage: Stage): EventHandler<MouseEvent> {
        return EventHandler {
            var fileChooser = FileChooser()
            var listFiles = fileChooser.showOpenMultipleDialog(primaryStage)

            if (listFiles != null) {
                view.setRepository(listFiles.size.toString() + " apks")

                Observable.create<ListItemApplication> { subscriber ->
                    run {
                        listFiles?.let {
                            for (i in listFiles.indices) {
                                if (listFiles[i].isFile) {
                                    if (listFiles[i].extension.equals("apk")) {
                                        if (!list.contains(listFiles[i])) {
                                            var apk: ApkFile = ApkFile(listFiles[i])
                                            subscriber.onNext(ListItemApplication(apk, listFiles[i]))

                                            list.add(listFiles[i])
                                        }
                                    }
                                } else if (listFiles[i].isDirectory) {
                                    println("Directory " + listFiles[i].name)
                                }
                            }
                        }
                    }
                }
                        .subscribeOn(Schedulers.computation())
                        .observeOn(JavaFxScheduler.platform())
                        .subscribe {
                            view.onFileAdded(it)
                        }
            } else {
                val alert = Alert(Alert.AlertType.ERROR)
                alert.title = "Error"
                alert.headerText = null
                alert.contentText = "No files selected"

                alert.showAndWait()
            }
        }
    }

    fun installOnDevices(list: List<File>): Observable<String> {
        return Observable.create<String> { subscriber ->
            run {
                jadb.listDevice.forEachIndexed { index, deviceModel ->
                    println("On device: " + deviceModel.name)

                    if (index < jadb.listDevice.size) {
                        installOnDevice(deviceModel.jadbDevice, list, 0)
                    } else {
                        installOnDevice(deviceModel.jadbDevice, list, 0)
                    }
                }.let {
                    subscriber.onComplete()
                }
            }
        }
    }

    fun installOnDevice(jadbDevice: JadbDevice, list: List<File>, i: Int) {
        jadb.installAPK(jadbDevice, list[i], view.isForceInstall())
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.io())
                .doOnComplete {
                    if (i + 1 < list.size) {
                        installOnDevice(jadbDevice, list, i + 1)
                    }
                }
                .subscribe()
    }

    override fun onInstallClick(): EventHandler<MouseEvent> {
        return EventHandler {
            if (jadb.listJadb.size > 0) {
                view.setInstallDisable(true)

                view.start()
            } else {
                val alert = Alert(Alert.AlertType.ERROR)

                alert.title = "Error"
                alert.headerText = null
                alert.contentText = "No devices found"

                alert.showAndWait()
            }
//            installOnDevices(list)
//                    .subscribeOn(Schedulers.computation())
//                    .observeOn(Schedulers.io())
//                    .doOnComplete { view.setInstallDisable(false) }
//                    .subscribe()
        }
    }
}