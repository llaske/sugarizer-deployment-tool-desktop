package com.sugarizer.view.device.type

import com.google.gson.Gson
import com.jfoenix.controls.events.JFXDialogEvent
import com.sugarizer.Main
import com.sugarizer.listitem.ListItemDevice
import com.sugarizer.listitem.ListItemInstruction
import com.sugarizer.model.*
import com.sugarizer.utils.shared.JADB
import com.sugarizer.utils.shared.JADB_MembersInjector
import com.sugarizer.utils.shared.NotificationBus
import com.sugarizer.utils.shared.ZipOutUtils
import com.sugarizer.view.createinstruction.instructions.ClickInstruction
import com.sugarizer.view.device.DeviceContract
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.Alert
import se.vidstige.jadb.JadbDevice
import java.io.File
import javax.inject.Inject

class SPK(private val view: DeviceContract.View) {

    @Inject lateinit var jadb: JADB
    @Inject lateinit var notifBus: NotificationBus

    private val zipOut: ZipOutUtils = ZipOutUtils()
    private val listDevice: MutableList<ListItemDevice> = mutableListOf()

    init {
        Main.appComponent.inject(this)
    }

    fun init(file: File, list: List<ListItemDevice>){
        if (list.size > 0) {
            view.showProgressFlash("Preparing instructions...")
            listDevice.addAll(list)

            zipOut.loadZip(file.absolutePath)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(JavaFxScheduler.platform())
                    .subscribe({},{
                        println(it.message)
                        view.hideProgressFlash()
                    },{
                        var list = mutableListOf<String>()
                        println("Size: " + zipOut.instruction?.intructions?.size)

                        zipOut.instruction?.intructions?.forEach {
                            list.add(it.ordre as Int, it.type.toString())
                        }

                        view.showProgressFlash("Instructions ready")
                        view.showDialog(list, DeviceContract.Dialog.SPK)
                    })
        } else {
            var alert = Alert(Alert.AlertType.ERROR)

            alert.title = "Error"
            alert.headerText = null
            alert.contentText = "No device connected"

            alert.showAndWait()
        }
    }

    fun launchInstruction(): EventHandler<ActionEvent> {
        return EventHandler {
            view.showProgressFlash("Flash: 0 %")
            view.closeDialog(DeviceContract.Dialog.SPK)
            executeOnDevice()
                        .subscribeOn(Schedulers.computation())
                        .observeOn(JavaFxScheduler.platform())
                        .subscribe({}, {}, {
                            view.hideProgressFlash()
                            notifBus.send("Flash completed")
                        })
        }
    }

    fun cancelInstruction(): EventHandler<ActionEvent> {
        return EventHandler {
            zipOut.deleteTmp()
            view.closeDialog(DeviceContract.Dialog.SPK)
        }
    }

    fun onInstructionDialogClosed(): EventHandler<JFXDialogEvent> {
        return EventHandler {
            zipOut.deleteTmp()
            view.closeDialog(DeviceContract.Dialog.SPK)
        }
    }

    fun executeOnDevice(): Observable<Any> {
        return Observable.create { subscriber ->
            var numberEnded = 0

            zipOut.instruction?.intructions?.let {
                var maxIntstruc = (it.size * listDevice.size).toDouble()
                var numberInstruc = 0.0

                listDevice.forEach {
                    newExecute(it)
                            .subscribeOn(Schedulers.computation())
                            .observeOn(JavaFxScheduler.platform())
                            .subscribe({
                                ++numberInstruc
                                view.showProgressFlash("Flash: " + Math.round((numberInstruc / maxIntstruc) * 100) + " %")
                            }, { it.printStackTrace() }, {
                                ++numberEnded

                                if (numberEnded == listDevice.size) {
                                    subscriber.onComplete()
                                }
                            })
                }
            }
        }
    }

    fun newExecute(device: ListItemDevice): Observable<String> {
        return Observable.create { deviceSubscriber ->
            device.changeState(ListItemDevice.State.WORKING)

            zipOut.instruction?.let {
                Observable.fromIterable(it.intructions).subscribe({
                    executeOneInstruction(it, device)
                    deviceSubscriber.onNext("")
                },{},{
                    deviceSubscriber.onComplete()
                    device.changeState(ListItemDevice.State.FINISH)
                })
            }
        }
    }

    fun executeOneInstruction(instruction: Instruction, device: ListItemDevice) {
        when (instruction.type) {
            ListItemInstruction.Type.APK -> doInstallApk(instruction, device)
            ListItemInstruction.Type.PUSH -> TODO()
            ListItemInstruction.Type.DELETE -> TODO()
            ListItemInstruction.Type.KEY -> doInput(instruction, ListItemInstruction.Type.KEY, device)
            ListItemInstruction.Type.CLICK -> doInput(instruction, ListItemInstruction.Type.CLICK, device)
            ListItemInstruction.Type.LONGCLICK -> doInput(instruction,ListItemInstruction.Type.LONGCLICK, device)
            ListItemInstruction.Type.SWIPE -> doInput(instruction, ListItemInstruction.Type.SWIPE, device)
            ListItemInstruction.Type.TEXT -> doInput(instruction, ListItemInstruction.Type.TEXT, device)
            ListItemInstruction.Type.SLEEP -> doInput(instruction, ListItemInstruction.Type.SLEEP, device)
        }
    }

    fun doInstallApk(instruction: Instruction, device: ListItemDevice){
        val install = Gson().fromJson(instruction.data, InstallApkModel::class.java)

        install.apks?.forEach { apk ->
            jadb.installAPK(device.device.jadbDevice, File("tmp\\" + apk), false)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(JavaFxScheduler.platform())
                    .subscribe({},{},{})
        }
    }

    fun doInput(instruction: Instruction, type: ListItemInstruction.Type, device: ListItemDevice){
        when (type) {
            ListItemInstruction.Type.CLICK -> doClick(instruction, device)
            ListItemInstruction.Type.LONGCLICK -> doLongClick(instruction, device)
            ListItemInstruction.Type.SWIPE -> doSwipe(instruction, device)
            ListItemInstruction.Type.KEY -> doKey(instruction, device)
            ListItemInstruction.Type.TEXT -> doText(instruction, device)
            ListItemInstruction.Type.SLEEP -> doSleep(instruction, device)
        }
    }

    fun doClick(instruction: Instruction, device: ListItemDevice){
        val click = Gson().fromJson(instruction.data, ClickModel::class.java)

        device.device.jadbDevice.executeShell("input tap " + click.x + " " + click.y, "")

        Thread.sleep(1000)
    }

    fun doLongClick(instruction: Instruction, device: ListItemDevice){
        val click = Gson().fromJson(instruction.data, LongClickModel::class.java)

        device.device.jadbDevice.executeShell("input swipe " + click.x + " " + click.y + " " + click.x + " " + click.y + " " + click.duration, "")

        Thread.sleep(click.duration.toLong())
    }

    fun doSwipe(instruction: Instruction, device: ListItemDevice){
        val click = Gson().fromJson(instruction.data, SwipeModel::class.java)

        device.device.jadbDevice.executeShell("input swipe " + click.x1 + " " + click.y1 + " " + click.x2 + " " + click.y2 + " " + click.duration, "")

        Thread.sleep(click.duration.toLong())
    }

    fun doKey(instruction: Instruction, device: ListItemDevice){
        val click = Gson().fromJson(instruction.data, KeyModel::class.java)

        device.device.jadbDevice.executeShell("input keyevent " + click.idKey, "")

        Thread.sleep(1000)
    }

    fun doText(instruction: Instruction, device: ListItemDevice){
        val click = Gson().fromJson(instruction.data, TextModel::class.java)

        device.device.jadbDevice.executeShell("input text " + click.text, "")

        Thread.sleep(1000)
    }

    fun doSleep(instruction: Instruction, device: ListItemDevice){
        val click = Gson().fromJson(instruction.data, SleepModel::class.java)

        Thread.sleep(click.duration)
    }
}