package com.sugarizer.presentation.view.appmanager

import com.sugarizer.domain.shared.JADB
import com.sugarizer.presentation.view.device.DevicesView
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.stage.DirectoryChooser
import javafx.stage.Stage
import se.vidstige.jadb.JadbDevice
import java.io.File

class AppManagerPresenter(val view: AppManagerContract.View, val jadb: JADB) : AppManagerContract.Presenter {
    val list: MutableList<File> = mutableListOf()

    override fun onChooseRepositoryClick(primaryStage: Stage): EventHandler<ActionEvent> {
        return EventHandler {
            var directory = DirectoryChooser()
            directory.title = "Choose the apk directory"
            var choosedDirectory: File = directory.showDialog(primaryStage)
            view.setRepository(choosedDirectory.absolutePath)

            Observable.create<String> {
                val folder = File(choosedDirectory.absolutePath)
                val listOfFiles = folder.listFiles()

                for (i in listOfFiles!!.indices) {
                    if (listOfFiles[i].isFile) {
                        println("File " + listOfFiles[i].name)
                        println("File " + listOfFiles[i].extension)
                        if (listOfFiles[i].extension.equals("apk")) {
                            if (!list.contains(listOfFiles[i])) {
                                view.onFileAdded(listOfFiles[i].name)
                                list.add(listOfFiles[i])
                            }
                        }
                    } else if (listOfFiles[i].isDirectory) {
                        println("Directory " + listOfFiles[i].name)
                    }
                }
            }
                    .subscribeOn(Schedulers.computation())
                    .observeOn(Schedulers.io())
                    .subscribe()
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
        jadb.installAPK(jadbDevice, list[i])
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.io())
                .doOnComplete {
                    if (i + 1 < list.size) {
                        installOnDevice(jadbDevice, list, i + 1)
                    }
                }
                .subscribe()
    }

    override fun onInstallClick(): EventHandler<ActionEvent> {
        return EventHandler {
            view.setInstallDisable(true)

            installOnDevices(list)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(Schedulers.io())
                    .doOnComplete { view.setInstallDisable(false) }
                    .subscribe()
        }
    }
}