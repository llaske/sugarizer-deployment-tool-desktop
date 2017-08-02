package com.sugarizer.view.device.type

import com.google.gson.Gson
import com.jfoenix.controls.events.JFXDialogEvent
import com.sugarizer.Main
import com.sugarizer.listitem.ListItemDevice
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
            println("Launch Instruction - S")
            view.showProgressFlash("Flash: 0.0 %")
            view.closeDialog(DeviceContract.Dialog.SPK)
            executeOnDevice()
                        .subscribeOn(Schedulers.computation())
                        .observeOn(JavaFxScheduler.platform())
                        .subscribe({}, {}, {
                            println("Launch Instruction - onComplete")
                            view.hideProgressFlash()
                            notifBus.send("Flash completed")
                        })
            println("Launch Instruction - E")
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
            println("Execute On Device - S")
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
                                println("NumberInstruct: " + numberInstruc)
                                println("MaxInstruc: " + maxIntstruc)
                                println("on Next instruct: " + (numberInstruc / maxIntstruc) * 100 )
                                view.showProgressFlash("Flash: " + (numberInstruc / maxIntstruc) * 100 + " %")
                            }, { it.printStackTrace() }, {
                                println("onNextDevice Ended")
                                ++numberEnded

                                println("Number: " + numberEnded)
                                println("Size: " + listDevice.size)

                                if (numberEnded == listDevice.size) {
                                    subscriber.onComplete()
                                }
                            })
                }
            }
            println("Execute On Device - E")
        }
    }

    fun newExecute(device: ListItemDevice): Observable<String> {
        return Observable.create { deviceSubscriber ->
            println("NewExecute - S")
            device.changeState(ListItemDevice.State.WORKING)

            zipOut.instruction?.let { executeInstruction(it.intructions as List<Instruction>, 0, device, deviceSubscriber) }
            println("NewExecute - E")
        }
    }

    fun executeInstruction(list: List<Instruction>, index: Int, device: ListItemDevice, deviceSubscriber: ObservableEmitter<String>) {
        println("Execute Instruction ! - S - S")
        Observable.create<String> { subscriber -> run {
            println("Execute Instruction ! - S")
            var instruction = list[index]
            println("Execute Instruction: " + instruction.type)

            when (instruction.type) {
                InstructionsModel.Type.INTALL_APK -> doInstallApk(subscriber, instruction, device)
                InstructionsModel.Type.PUSH_FILE -> TODO()
                InstructionsModel.Type.DELETE_FILE -> TODO()
                InstructionsModel.Type.INSTRUCTION_KEY -> doInput(subscriber, instruction, ClickInstruction.Type.KEY, device)
                InstructionsModel.Type.INSTRUCTION_CLICK -> doInput(subscriber, instruction, ClickInstruction.Type.CLICK, device)
                InstructionsModel.Type.INSTRUCTION_LONG_CLICK -> doInput(subscriber, instruction, ClickInstruction.Type.LONG_CLICK, device)
                InstructionsModel.Type.INSTRUCTION_SWIPE -> doInput(subscriber, instruction, ClickInstruction.Type.SWIPE, device)
                InstructionsModel.Type.INSTRUCTION_TEXT -> doInput(subscriber, instruction, ClickInstruction.Type.TEXT, device)
                InstructionsModel.Type.SLEEP -> {
                    println("SLEEP EXECUTE ? - S")
                    doInput(subscriber, instruction, ClickInstruction.Type.SLEEP, device)
                    println("SLEEP EXECUTE ? - E")}
                else -> {
                    println("type not found")
                    subscriber.onComplete()
                }
            }

            println("Execute Instruction ! - E")
        }
        }
                .subscribeOn(Schedulers.computation())
                .subscribe({}, { it.printStackTrace() }, {
                    println("executeInstruction - onComplete")
                    println("spk on complete")
                    deviceSubscriber.onNext("")
                    if (index < list.size - 1) {
                        println("spk on complete - continue")
                        executeInstruction(list, index + 1, device, deviceSubscriber)
                    } else {
                        println("spk on complete - finish")
                        deviceSubscriber.onComplete()
                        device.changeState(ListItemDevice.State.FINISH)
                    }
                })
        println("Execute Instruction ! - E - E")
    }

    fun doInstallApk(subscriber: ObservableEmitter<String>, instruction: Instruction, device: ListItemDevice){
        val install = Gson().fromJson(instruction.data, InstallApkModel::class.java)

        install.apks?.forEach { apk ->
            println("Install - S")
            jadb.installAPK(device.device.jadbDevice, File("tmp\\" + apk), false)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(JavaFxScheduler.platform())
                    .subscribe({},{},{ subscriber.onComplete() })
            println("Install - E")
        }
    }

    fun doInput(subscriber: ObservableEmitter<String>, instruction: Instruction, type: ClickInstruction.Type, device: ListItemDevice){
        println("doInput - S")
        when (type) {
            ClickInstruction.Type.CLICK -> doClick(instruction, device, subscriber)
            ClickInstruction.Type.LONG_CLICK -> doLongClick(instruction, device, subscriber)
            ClickInstruction.Type.SWIPE -> doSwipe(instruction, device, subscriber)
            ClickInstruction.Type.KEY -> doKey(instruction, device, subscriber)
            ClickInstruction.Type.TEXT -> doText(instruction, device, subscriber)
            ClickInstruction.Type.SLEEP -> doSleep(instruction, device, subscriber)
            else -> subscriber.onComplete()
        }
        println("doInput - E")
    }

    fun doClick(instruction: Instruction, device: ListItemDevice, subscriber: ObservableEmitter<String>){
        val click = Gson().fromJson(instruction.data, ClickModel::class.java)

        device.device.jadbDevice.executeShell("input tap " + click.x + " " + click.y, "")

        Thread.sleep(1000)

        subscriber.onComplete()
    }

    fun doLongClick(instruction: Instruction, device: ListItemDevice, subscriber: ObservableEmitter<String>){
        val click = Gson().fromJson(instruction.data, LongClickModel::class.java)

        device.device.jadbDevice.executeShell("input swipe " + click.x + " " + click.y + " " + click.x + " " + click.y + " " + click.duration, "")

        Thread.sleep(click.duration.toLong())

        subscriber.onComplete()
    }

    fun doSwipe(instruction: Instruction, device: ListItemDevice, subscriber: ObservableEmitter<String>){
        val click = Gson().fromJson(instruction.data, SwipeModel::class.java)

        device.device.jadbDevice.executeShell("input swipe " + click.x1 + " " + click.y1 + " " + click.x2 + " " + click.y2 + " " + click.duration, "")

        Thread.sleep(click.duration.toLong())

        subscriber.onComplete()
    }

    fun doKey(instruction: Instruction, device: ListItemDevice, subscriber: ObservableEmitter<String>){
        val click = Gson().fromJson(instruction.data, KeyModel::class.java)

        device.device.jadbDevice.executeShell("input keyevent " + click.idKey, "")

        Thread.sleep(1000)

        subscriber.onComplete()
    }

    fun doText(instruction: Instruction, device: ListItemDevice, subscriber: ObservableEmitter<String>){
        val click = Gson().fromJson(instruction.data, TextModel::class.java)

        device.device.jadbDevice.executeShell("input text " + click.text, "")

        Thread.sleep(1000)

        subscriber.onComplete()
    }

    fun doSleep(instruction: Instruction, device: ListItemDevice, subscriber: ObservableEmitter<String>){
        val click = Gson().fromJson(instruction.data, SleepModel::class.java)

        println("Sleep - S")
        Thread.sleep(click.duration)
        println("Sleep - E")

        subscriber.onComplete()
        println("Sleep - Complete")
    }
}