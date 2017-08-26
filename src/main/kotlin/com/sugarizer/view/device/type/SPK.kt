package com.sugarizer.view.device.type

import com.google.gson.Gson
import com.jfoenix.controls.events.JFXDialogEvent
import com.sugarizer.Main
import com.sugarizer.listitem.ListItemDevice
import com.sugarizer.listitem.ListItemInstruction
import com.sugarizer.model.*
import com.sugarizer.utils.shared.*
import com.sugarizer.view.createinstruction.CreateInstructionView
import com.sugarizer.view.createinstruction.instructions.ClickInstruction
import com.sugarizer.view.device.DeviceContract
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.rxkotlin.toSingle
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
    @Inject lateinit var fileUtils: FileUtils

    private val zipOut: ZipOutUtils
    private val listDevice: MutableList<ListItemDevice> = mutableListOf()
    private var map: MutableMap<JadbDevice, MutableList<Instruction>> = mutableMapOf()

    init {
        Main.appComponent.inject(this)
        zipOut = ZipOutUtils(fileUtils)
    }

    fun init(file: File, list: List<ListItemDevice>){
        listDevice.clear()
        if (list.isNotEmpty()) {
            view.showProgressFlash("Preparing instructions...")
            listDevice.addAll(list)

            zipOut.loadZip(file.absolutePath)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(JavaFxScheduler.platform())
                    .subscribe({},{
                        it.printStackTrace()
                        println("Erroe Load:" + it.message)
                        view.hideProgressFlash()
                    },{
                        var listInstructions = mutableListOf<String>()
                        println("Size: " + zipOut.instruction?.intructions?.size)

                        zipOut.instruction?.let {
                            for (tmp in listDevice) {
                                map.put(tmp.device, it.intructions)
                            }
                        }
                        zipOut.instruction?.intructions?.forEach {
                            listInstructions.add(it.ordre as Int, it.type.toString())
                        }

                        view.showProgressFlash("Instructions ready")
                        view.showDialog(listInstructions, DeviceContract.Dialog.SPK)
                    })
        } else {
            var alert = Alert(Alert.AlertType.ERROR)

            alert.title = "Error"
            alert.headerText = null
            alert.contentText = "No device connected"

            alert.showAndWait()
        }
    }

    fun changeState(device: JadbDevice, state: ListItemDevice.State){
        for (tmp in listDevice) {
            if (tmp.device.serial == device.serial) {
                Platform.runLater {
                    tmp.changeState(state)
                }
            }
        }
    }

    fun launchInstruction(): EventHandler<ActionEvent> {
        return EventHandler {
            view.showProgressFlash("Flash: 0 %")
            view.closeDialog(DeviceContract.Dialog.SPK)

            var numberEnded = 0
            var maxIntstruc = getNumberInstruction().toDouble()
            var numberInstruc = 0.0

            Observable.create<Any> { mainSub ->
                val iterate = map.iterator()
                while (iterate.hasNext()) {
                    val tmp = iterate.next()
                    changeState(tmp.key, ListItemDevice.State.WORKING)
                    Observable.create<Any> { deviceSubscriber ->
                        var list = mutableListOf<Instruction>()
                        do {
                            list.clear()
                            val max = tmp.value.size.toDouble()
                            var i = 0.0
                            val tmpInstruction = tmp.value.iterator()

                            while (tmpInstruction.hasNext()) {
                                var instruction = tmpInstruction.next()
                                Observable.create<Any> {
                                    executeOneInstruction(instruction, tmp.key)
                                    mainSub.onNext("")
                                    updateDevice(tmp.key, i / max)
                                }.subscribe({}, {
                                    it.printStackTrace()
                                    list.add(instruction)
                                }, {
                                    iterate.remove()
                                })
                            }

                            tmp.value.addAll(list)
                        } while (list.isNotEmpty())

                        deviceSubscriber.onComplete()
                    }
                            .subscribeOn(Schedulers.newThread())
                            .subscribe({},{it.printStackTrace()},{
                                numberEnded++
                                changeState(tmp.key, ListItemDevice.State.FINISH)
                                removeFromMap(tmp.key)
                            })
//                    Observable.fromIterable(tmp.value)
//                            .subscribeOn(Schedulers.newThread())
//                            .subscribe({
//                                println("SPK - 4")
//                                executeOneInstruction(it, tmp.key)
//                                mainSub.onNext("")
//                            },{},{
//                                println("SPK - 5")
//                                numberEnded++
//                                changeState(tmp.key, ListItemDevice.State.FINISH)
//                                removeFromMap(tmp.key)
//                            })
                }

                while (map.isNotEmpty()) { Thread.sleep(1000) }

                mainSub.onComplete()
            }
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(JavaFxScheduler.platform())
                    .subscribe({
                        view.showProgressFlash("Flash: " + Math.round((++numberInstruc / maxIntstruc) * 100) + " %")
                    },{it.printStackTrace()},{
                        view.hideProgressFlash()
                        notifBus.send("Flash completed")
                    })
//            executeOnDevice()
//                        .subscribeOn(Schedulers.newThread())
//                        .observeOn(JavaFxScheduler.platform())
//                        .subscribe({}, { it.printStackTrace() }, {
//                        })
        }
    }

    fun removeFromMap(device: JadbDevice){
        val iterate = map.iterator()
        while (iterate.hasNext()) {
            val tmp = iterate.next()
            if (tmp.key.serial == device.serial) {
                iterate.remove()
            }
        }
    }

    fun updateDevice(device: JadbDevice, double: Double){
        for (tmp in listDevice) {
            if (tmp.device.serial == device.serial) {
                tmp.progress.progress = double
            }
        }
    }

    fun getNumberInstruction(): Int {
        var nb = 0

        map.forEach {
            nb += it.value.size
        }

        return nb
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
                            .subscribeOn(Schedulers.newThread())
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

//            zipOut.instruction?.let {
//                Observable.fromIterable(it.intructions).subscribe({
//                    executeOneInstruction(it, device)
//                    deviceSubscriber.onNext("")
//                },{},{
//                    deviceSubscriber.onComplete()
//                    device.changeState(ListItemDevice.State.FINISH)
//                })
//            }
        }
    }

    fun executeOneInstruction(instruction: Instruction, device: JadbDevice) {
        when (instruction.type) {
            CreateInstructionView.Type.APK -> doInstallApk(instruction, device)
            CreateInstructionView.Type.PUSH -> TODO()
            CreateInstructionView.Type.DELETE -> doDelete(instruction, device)
            CreateInstructionView.Type.KEY -> doKey(instruction, device)
            CreateInstructionView.Type.CLICK -> doClick(instruction, device)
            CreateInstructionView.Type.LONGCLICK -> doLongClick(instruction, device)
            CreateInstructionView.Type.SWIPE -> doSwipe(instruction, device)
            CreateInstructionView.Type.TEXT -> doText(instruction, device)
            CreateInstructionView.Type.SLEEP -> doSleep(instruction, device)
            CreateInstructionView.Type.OPENAPP -> doOpenApp(instruction, device)
        }
    }

    fun doInstallApk(instruction: Instruction, device: JadbDevice){
        val install = Gson().fromJson(instruction.data, InstallApkModel::class.java)

        install.apks?.forEach { apk ->
            jadb.installAPK(device, File("tmp" + fileUtils.separator + apk), false)
                    //.subscribeOn(Schedulers.newThread())
                    //.observeOn(JavaFxScheduler.platform())
                    .subscribe({},{
                        it.printStackTrace()
                    },{})
        }
    }

    fun doClick(instruction: Instruction, device: JadbDevice){
        val click = Gson().fromJson(instruction.data, ClickModel::class.java)

        device.executeShell("input tap " + click.x + " " + click.y, "")

        Thread.sleep(1000)
    }

    fun doLongClick(instruction: Instruction, device: JadbDevice){
        val click = Gson().fromJson(instruction.data, LongClickModel::class.java)

        device.executeShell("input swipe " + click.x + " " + click.y + " " + click.x + " " + click.y + " " + click.duration, "")

        Thread.sleep(click.duration.toLong())
    }

    fun doSwipe(instruction: Instruction, device: JadbDevice){
        val click = Gson().fromJson(instruction.data, SwipeModel::class.java)

        device.executeShell("input swipe " + click.x1 + " " + click.y1 + " " + click.x2 + " " + click.y2 + " " + click.duration, "")

        Thread.sleep(click.duration.toLong())
    }

    fun doKey(instruction: Instruction, device: JadbDevice){
        val click = Gson().fromJson(instruction.data, KeyModel::class.java)

        device.executeShell("input keyevent " + click.idKey, "")

        Thread.sleep(1000)
    }

    fun doText(instruction: Instruction, device: JadbDevice){
        val click = Gson().fromJson(instruction.data, TextModel::class.java)

        Thread.sleep(1000)

        device.executeShell("input text " + click.text, "")

        Thread.sleep(1000)
    }

    fun doSleep(instruction: Instruction, device: JadbDevice){
        val click = Gson().fromJson(instruction.data, SleepModel::class.java)

        Thread.sleep(click.duration)
    }

    fun doOpenApp(instruction: Instruction, device: JadbDevice){
        val click = Gson().fromJson(instruction.data, OpenAppModel::class.java)

        jadb.convertStreamToString(device.executeShell("monkey -p " + click.package_name + " -c android.intent.category.LAUNCHER 1", ""))

        Thread.sleep(1000)
    }

    fun doDelete(instruction: Instruction, device: JadbDevice){
        val click = Gson().fromJson(instruction.data, DeleteFileModel::class.java)

        jadb.convertStreamToString(device.executeShell("rm " + click.file_name, ""))

        Thread.sleep(1000)
    }
}