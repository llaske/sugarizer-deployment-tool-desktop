package com.sugarizer.view.device

import com.jfoenix.controls.events.JFXDialogEvent
import com.sugarizer.BuildConfig
import com.sugarizer.model.*
import com.sugarizer.utils.shared.*
import com.sugarizer.Main
import com.sugarizer.view.device.type.APK
import com.sugarizer.view.device.type.SPK
import io.reactivex.Observable
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.input.DragEvent
import javafx.scene.input.TransferMode
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import javax.inject.Inject

class DevicePresenter(val view: DeviceContract.View, val jadb: JADB, val rxBus: RxBus, val stringUtils: StringUtils) : DeviceContract.Presenter {
    @Inject lateinit var notifBus: NotificationBus

    val spk = SPK(view)
    val apk = APK()

    init {
        Main.appComponent.inject(this)

        jadb.watcher()
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.io())
                .subscribe { deviceEvent ->
                    run {
                        when (deviceEvent.status) {
                            DeviceEventModel.Status.ADDED -> { view.onDeviceAdded(deviceEvent) }
                            DeviceEventModel.Status.REMOVED -> { view.onDeviceRemoved(deviceEvent) }
                            else -> { }
                        }
                    }
                }

        rxBus.toObservable().subscribe { deviceEvent -> run { view.onDeviceChanged(deviceEvent) } }
    }

    override fun onPingClick(device: DeviceModel): EventHandler<ActionEvent> {
        return EventHandler {
            jadb.hasPackage(device.jadbDevice, "com.sugarizer.sugarizerdeploymenttoolapp")
                    .subscribeOn(Schedulers.computation())
                    .doOnComplete { jadb.ping(device.jadbDevice) }
                    .subscribe { println(it) }
        }
    }

    override fun onDragOver(): EventHandler<DragEvent> {
        return EventHandler {
            val db = it.dragboard
            if (db.hasFiles()) {
                for (tmp in db.files) {
                    if (!tmp.extension.equals("spk") && !tmp.extension.equals("apk")) {
                        it.consume()
                        return@EventHandler
                    }
                }
                it.acceptTransferModes(TransferMode.COPY)
            } else {
                it.consume()
            }
        }
    }

    override fun onDropped(): EventHandler<DragEvent> {
        return EventHandler {
            val db = it.dragboard
            var success = false

            if (db.hasFiles()) {
                var listAPK = mutableListOf<File>()
                var listSPK = mutableListOf<File>()

                db.files.forEach {
                    when (it.extension) {
                        "spk" -> listSPK.add(it)
                        "apk" -> listAPK.add(it)
                    }
                }

                if (listSPK.size > 0) { copySPK(listSPK, 0) }
                if (listAPK.size > 0) { view.showDialog(listAPK, DeviceContract.Dialog.APK) }
                println("Size APK: " + listAPK.size)
                println("Size SPK: " + listSPK.size)
            }

            it.isDropCompleted = success
            it.consume()
        }
    }

    private fun copySPK(list: List<File>, index: Int) {
        Observable.create<Any> {
            var path = Paths.get(BuildConfig.SPK_LOCATION, list[index].name)
            //File(BuildConfig.SPK_LOCATION + list[index].name).toPath().toAbsolutePath()
            println("CopyPath:" + path.toString())
            Files.copy(list[index].toPath(), path)
            it.onComplete()
        }
                .subscribeOn(Schedulers.io())
                .observeOn(JavaFxScheduler.platform())
                .subscribe({}, { println(it.message) }, {
                    notifBus.send(list[index].nameWithoutExtension + " Copied !")

                    if (index < list.size - 1) {
                        copySPK(list, index + 1)
                    } else {
                        println("Copy finish")
                    }
                })
    }

    override fun onLaunchApk(): EventHandler<ActionEvent> {
        return EventHandler {
            try {

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCancelApk(): EventHandler<ActionEvent> {
        return EventHandler {
            view.closeDialog(DeviceContract.Dialog.APK)
        }
    }

    override fun onApkDialogClosed(): EventHandler<JFXDialogEvent> {
        return EventHandler {
            view.closeDialog(DeviceContract.Dialog.APK)
        }
    }

    override fun onSpkFlashClicked(file: File) {
        spk.init(file, view.getDevices())
    }

    override fun onLaunchInstruction(): EventHandler<ActionEvent> {
        return spk.launchInstruction()
    }

    override fun onCancelInstruction(): EventHandler<ActionEvent> {
        return spk.cancelInstruction()
    }

    override fun onInstructionDialogClosed(): EventHandler<JFXDialogEvent> {
        return spk.onInstructionDialogClosed()
    }
}