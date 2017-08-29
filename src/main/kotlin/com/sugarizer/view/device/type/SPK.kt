package com.sugarizer.view.device.type

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.jfoenix.controls.events.JFXDialogEvent
import com.sugarizer.BuildConfig
import com.sugarizer.Main
import com.sugarizer.listitem.ListItemDevice
import com.sugarizer.model.*
import com.sugarizer.utils.shared.*
import com.sugarizer.view.createinstruction.CreateInstructionView
import com.sugarizer.view.device.DeviceContract
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.Alert
import net.dongliu.apk.parser.ApkFile
import se.vidstige.jadb.JadbDevice
import se.vidstige.jadb.JadbException
import se.vidstige.jadb.managers.PackageManager
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
                        var allInstructions = mutableListOf<Instruction>()

                        zipOut.instruction?.let {
                            var ordre = 0
                            var iterator = it.intructions.iterator()
                            while (iterator.hasNext()) {
                                var tmp = iterator.next()
                                if (tmp.type == CreateInstructionView.Type.APK) {
                                    Gson().fromJson(tmp.data, InstallApkModel::class.java).apks?.let {
                                        it.forEach {
                                            var singleApk = SingleApk(it)
                                            var instruction = Instruction()
                                            instruction.type = CreateInstructionView.Type.SINGLE_APK
                                            instruction.ordre = ordre
                                            instruction.data = Gson().toJson(singleApk, SingleApk::class.java)
                                            allInstructions.add(ordre, instruction)
                                        }
                                    }
                                } else {
                                    tmp.ordre = ordre
                                    allInstructions.add(ordre, tmp)
                                }
                                ++ordre
                            }
                        }

                        println("Size all: " + allInstructions.size)

                        zipOut.instruction?.let {
                            for (tmp in listDevice) {
                                map.put(tmp.device, allInstructions)
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



    private fun changeState(device: JadbDevice, state: ListItemDevice.State){
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

            println("MaxInstruc: " + maxIntstruc)

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
                                    executeOneInstruction(instruction, tmp.key, it)
                                    mainSub.onNext("")
                                    updateDevice(tmp.key, ++i / max)
                                }.subscribe({}, {
                                    it.printStackTrace()
                                    list.add(instruction)
                                }, {})
                            }

                            println("List Size: " + tmp.value.size)
                            println("List : " + list.size)

                            map.put(tmp.key, list)
                        } while (list.isNotEmpty())

                        deviceSubscriber.onComplete()
                    }
                            .subscribeOn(Schedulers.newThread())
                            .subscribe({},{it.printStackTrace()},{
                                numberEnded++
                                changeState(tmp.key, ListItemDevice.State.FINISH)
                                removeFromMap(tmp.key)
                            })
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
                        deleteTmp()
                    })
        }
    }

    private fun removeFromMap(device: JadbDevice){
        val iterate = map.iterator()
        while (iterate.hasNext()) {
            val tmp = iterate.next()
            if (tmp.key.serial == device.serial) {
                iterate.remove()
            }
        }
    }

    private fun updateDevice(device: JadbDevice, double: Double){
        for (tmp in listDevice) {
            if (tmp.device.serial == device.serial) {
                tmp.progress.progress = double
            }
        }
    }

    private fun getNumberInstruction(): Int {
        var nb = 0

        map.forEach {
            it.value.filter { it.type == CreateInstructionView.Type.APK }
                    .forEach { Gson().fromJson(it.data, InstallApkModel::class.java).apks?.let {
                            nb += it.size + 1
                        } }
            nb += it.value.size
        }

        return nb
    }

    fun cancelInstruction(): EventHandler<ActionEvent> {
        return EventHandler {
            view.closeDialog(DeviceContract.Dialog.SPK)
            view.hideProgressFlash()
            deleteTmp()
        }
    }

    fun onInstructionDialogClosed(): EventHandler<JFXDialogEvent> {
        return EventHandler {
            view.closeDialog(DeviceContract.Dialog.SPK)
        }
    }

    private fun deleteTmp(){
        Observable.create<Any> {
            org.apache.commons.io.FileUtils.deleteDirectory(File(BuildConfig.TMP_DIRECTORY))
            it.onComplete()
        }
                .subscribeOn(Schedulers.newThread())
                .subscribe({},{it.printStackTrace()},{})
    }

    private fun executeOneInstruction(instruction: Instruction, device: JadbDevice, it: ObservableEmitter<Any>) {
        when (instruction.type) {
            CreateInstructionView.Type.SINGLE_APK -> doSingleApk(instruction, device)
            CreateInstructionView.Type.APK -> doInstallApk(instruction, device, it)
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

    private fun doSingleApk(instruction: Instruction, device: JadbDevice) {
        val install = Gson().fromJson(instruction.data, SingleApk::class.java)
        val apk = ApkFile(File(BuildConfig.TMP_DIRECTORY + fileUtils.separator + install.apk))

        try {
            device.executeShell(BuildConfig.CMD_LOG + " --es extra_log \"" + "Installing: " + apk.apkMeta.name + "\"")
            PackageManager(device).install(File(BuildConfig.TMP_DIRECTORY + fileUtils.separator + install.apk))
            device.executeShell(BuildConfig.CMD_LOG + " --es extra_log \"" + apk.apkMeta.name + " installed" + "\"")
        } catch (e: JadbException) {
            device.executeShell(BuildConfig.CMD_LOG + " --es extra_log \"" + apk.apkMeta.name + " already installed" + "\"")
        }
    }

    private fun doInstallApk(instruction: Instruction, device: JadbDevice, it: ObservableEmitter<Any>){
        val install = Gson().fromJson(instruction.data, InstallApkModel::class.java)

        install.apks?.forEach { apk ->
            jadb.installAPK(device, File(BuildConfig.TMP_DIRECTORY + fileUtils.separator + apk), false)
                    .subscribe({},{
                        it.printStackTrace()
                    },{

                    })
        }
    }

    private fun doClick(instruction: Instruction, device: JadbDevice){
        val click = Gson().fromJson(instruction.data, ClickModel::class.java)

        device.executeShell("input tap " + click.x + " " + click.y, "")

        Thread.sleep(1000)
    }

    private fun doLongClick(instruction: Instruction, device: JadbDevice){
        val click = Gson().fromJson(instruction.data, LongClickModel::class.java)

        device.executeShell("input swipe " + click.x + " " + click.y + " " + click.x + " " + click.y + " " + click.duration, "")

        Thread.sleep(click.duration.toLong())
    }

    private fun doSwipe(instruction: Instruction, device: JadbDevice){
        val click = Gson().fromJson(instruction.data, SwipeModel::class.java)

        device.executeShell("input swipe " + click.x1 + " " + click.y1 + " " + click.x2 + " " + click.y2 + " " + click.duration, "")

        Thread.sleep(click.duration.toLong())
    }

    private fun doKey(instruction: Instruction, device: JadbDevice){
        val click = Gson().fromJson(instruction.data, KeyModel::class.java)

        device.executeShell("input keyevent " + click.idKey, "")

        Thread.sleep(1000)
    }

    private fun doText(instruction: Instruction, device: JadbDevice){
        val click = Gson().fromJson(instruction.data, TextModel::class.java)

        Thread.sleep(1000)

        device.executeShell("input text " + click.text, "")

        Thread.sleep(1000)
    }

    private fun doSleep(instruction: Instruction, device: JadbDevice){
        val click = Gson().fromJson(instruction.data, SleepModel::class.java)

        Thread.sleep(click.duration)
    }

    private fun doOpenApp(instruction: Instruction, device: JadbDevice){
        val click = Gson().fromJson(instruction.data, OpenAppModel::class.java)

        jadb.convertStreamToString(device.executeShell("monkey -p " + click.package_name + " -c android.intent.category.LAUNCHER 1", ""))

        Thread.sleep(1000)
    }

    private fun doDelete(instruction: Instruction, device: JadbDevice){
        val click = Gson().fromJson(instruction.data, DeleteFileModel::class.java)

        jadb.convertStreamToString(device.executeShell("rm " + click.file_name, ""))

        Thread.sleep(1000)
    }

    private class SingleApk(apk: String) {

        @SerializedName("apk")
        var apk: String = apk
    }
}