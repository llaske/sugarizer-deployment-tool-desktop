package com.sugarizer.presentation.view.device

import com.google.gson.Gson
import com.jfoenix.controls.events.JFXDialogEvent
import com.sugarizer.BuildConfig
import com.sugarizer.domain.model.*
import com.sugarizer.domain.shared.*
import com.sugarizer.main.Main
import com.sugarizer.presentation.custom.ListItemDevice
import com.sugarizer.presentation.view.createinstruction.instructions.ClickInstruction
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.input.DragEvent
import javafx.scene.input.TransferMode
import java.io.File
import java.nio.file.Files
import javax.inject.Inject

class DevicePresenter(val view: DeviceContract.View, val jadb: JADB, val rxBus: RxBus, val stringUtils: StringUtils) : DeviceContract.Presenter {
    @Inject lateinit var notifBus: NotificationBus

    val zipOut: ZipOutUtils = ZipOutUtils()

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
                if (listAPK.size > 0) { view.showDialog(listAPK) }
                println("Size APK: " + listAPK.size)
                println("Size SPK: " + listSPK.size)
            }

//                success = true
//                zipOut.loadZip(db.files[0].absolutePath)
//                        .subscribeOn(Schedulers.computation())
//                        .observeOn(JavaFxScheduler.platform())
//                        .doOnComplete {
//                            var list = mutableListOf<String>()
//                            println("Size: " + zipOut.instruction?.intructions?.size)
//
//                            zipOut.instruction?.intructions?.forEach {
//                                list.add(it.ordre as Int, it.type.toString())
//                            }
//
//                            view.showDialog(list)
//                        }
//                        .subscribe {
//                            when (it) {
//                                ZipOutUtils.STATUS.NOT_COMPLETE -> { view.setInWork(false) }
//                                ZipOutUtils.STATUS.IN_PROGRESS -> { view.setInWork(true) }
//                                ZipOutUtils.STATUS.COMPLETE -> { view.setInWork(false) }
//                            }
//                        }
//            } else if (db.hasFiles() && db.files.size == 1) {
//                var alert = Alert(Alert.AlertType.ERROR)
//
//                alert.title = "Error"
//                alert.headerText = null
//                alert.contentText = "Only file can be dropped"
//
//                alert.showAndWait()
//            }
//
            it.isDropCompleted = success
            it.consume()
        }
    }

    private fun copySPK(list: List<File>, index: Int) {
        Observable.create<Any> {
            println("Start - Copy")
            Files.copy(list[index].toPath(), File(BuildConfig.SPK_LOCATION + list[index].name).toPath())

            println("End - Copy")
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

    override fun onLaunchInstruction(): EventHandler<ActionEvent> {
        return EventHandler {
            try {
                view.closeDialog()
                executeInstructions(0)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(JavaFxScheduler.platform())
                        .doOnComplete { restart() }
                        .subscribe { }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCancelInstruction(): EventHandler<ActionEvent> {
        return EventHandler {
            zipOut.deleteTmp()
            view.closeDialog()
        }
    }

    override fun onDialogClosed(): EventHandler<JFXDialogEvent> {
        return EventHandler {
            zipOut.deleteTmp()
            view.closeDialog()
        }
    }

    fun executeInstructions(index: Int): Observable<String> {
        return Observable.create { subscriber -> run {
            zipOut.instruction?.intructions?.get(index)?.let {
                executeInstruction(instruction = it)
                        .subscribeOn(Schedulers.computation())
                        .doOnComplete {
                            zipOut.instruction?.intructions?.size?.let {
                                println("CompareTo: " + index.compareTo(it))
                                if ((index + 1).compareTo(it) < 0) {
                                    executeInstructions(index + 1)
                                            .subscribeOn(Schedulers.computation())
                                            .observeOn(JavaFxScheduler.platform())
                                            .doOnComplete { restart() }
                                            .subscribe()
                                } else {
                                    subscriber.onComplete()
                                }
                            }
                        }
                        .doOnError { restart() }
                        .subscribe()
            }
        }
        }
    }

    fun executeInstruction(instruction: Instruction): Observable<String> {
        return Observable.create { subscriber -> run {
            when (instruction.type) {
                InstructionsModel.Type.INTALL_APK -> doInstallApk(subscriber, instruction)
                InstructionsModel.Type.PUSH_FILE -> TODO()
                InstructionsModel.Type.DELETE_FILE -> TODO()
                InstructionsModel.Type.INSTRUCTION_KEY -> doInput(subscriber, instruction, ClickInstruction.Type.KEY)
                InstructionsModel.Type.INSTRUCTION_CLICK -> doInput(subscriber, instruction, ClickInstruction.Type.CLICK)
                InstructionsModel.Type.INSTRUCTION_LONG_CLICK -> doInput(subscriber, instruction, ClickInstruction.Type.LONG_CLICK)
                InstructionsModel.Type.INSTRUCTION_SWIPE -> doInput(subscriber, instruction, ClickInstruction.Type.SWIPE)
                InstructionsModel.Type.INSTRUCTION_TEXT -> doInput(subscriber, instruction, ClickInstruction.Type.TEXT)
                InstructionsModel.Type.SLEEP -> doInput(subscriber, instruction, ClickInstruction.Type.SLEEP)
                null -> TODO()
            }
        }
        }
    }

    fun doInstallApk(subscriber: ObservableEmitter<String>, instruction: Instruction){
        val install = Gson().fromJson(instruction.data, InstallApkModel::class.java)

        install.apks?.forEach { apk -> run {
            view.getDevices().forEach { device -> run {
                device.onItemStartWorking()
                jadb.installAPK(device.device.jadbDevice, File("tmp\\" + apk), true)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(JavaFxScheduler.platform())
                        .doOnComplete {
                            subscriber.onComplete()
                            device.onItemStopWorking()
                        }
                        .subscribe()
            }
            }
        }
        }
    }

    fun doInput(subscriber: ObservableEmitter<String>, instruction: Instruction, type: ClickInstruction.Type){
        val nb = jadb.listJadb.size

        view.getDevices().forEachIndexed { index, listItemDevice ->
            Observable.create<Any> {
                when (type) {
                    ClickInstruction.Type.CLICK -> doClick(instruction, listItemDevice)
                    ClickInstruction.Type.LONG_CLICK -> doLongClick(instruction, listItemDevice)
                    ClickInstruction.Type.SWIPE -> doSwipe(instruction, listItemDevice)
                    ClickInstruction.Type.KEY -> doKey(instruction, listItemDevice)
                    ClickInstruction.Type.TEXT -> doText(instruction, listItemDevice)
                    ClickInstruction.Type.SLEEP -> doSleep(instruction, listItemDevice)
                }

                it.onComplete()
            }
                    .subscribeOn(Schedulers.computation())
                    .observeOn(JavaFxScheduler.platform())
                    .doOnComplete {
                        if (index.equals(nb - 1)){
                            subscriber.onComplete()
                        }
                    }
                    .subscribe()
        }
    }

    fun doClick(instruction: Instruction, device: ListItemDevice){
        val click = Gson().fromJson(instruction.data, ClickModel::class.java)

        device.onItemStartWorking()
        device.device.jadbDevice.executeShell("input tap " + click.x + " " + click.y, "")

        Thread.sleep(1000)
        device.onItemStopWorking()
    }

    fun doLongClick(instruction: Instruction, device: ListItemDevice){
        val click = Gson().fromJson(instruction.data, LongClickModel::class.java)

        device.onItemStartWorking()
        device.device.jadbDevice.executeShell("input swipe " + click.x + " " + click.y + " " + click.x + " " + click.y + " " + click.duration, "")

        Thread.sleep(click.duration.toLong())
        device.onItemStopWorking()
    }

    fun doSwipe(instruction: Instruction, device: ListItemDevice){
        val click = Gson().fromJson(instruction.data, SwipeModel::class.java)

        device.onItemStartWorking()
        device.device.jadbDevice.executeShell("input swipe " + click.x1 + " " + click.y1 + " " + click.x2 + " " + click.y2 + " " + click.duration, "")

        Thread.sleep(click.duration.toLong())
        device.onItemStopWorking()
    }

    fun doKey(instruction: Instruction, device: ListItemDevice){
        val click = Gson().fromJson(instruction.data, KeyModel::class.java)

        device.onItemStartWorking()
        device.device.jadbDevice.executeShell("input keyevent " + click.idKey, "")

        Thread.sleep(1000)
        device.onItemStopWorking()
    }

    fun doText(instruction: Instruction, device: ListItemDevice){
        val click = Gson().fromJson(instruction.data, TextModel::class.java)

        device.onItemStartWorking()
        device.device.jadbDevice.executeShell("input text " + click.text, "")

        Thread.sleep(1000)
        device.onItemStopWorking()
    }

    fun doSleep(instruction: Instruction, device: ListItemDevice){
        val click = Gson().fromJson(instruction.data, SleepModel::class.java)

        device.onItemStartWorking()
        Thread.sleep(click.duration)
        device.onItemStopWorking()
    }

    fun restart(){
        view.setInWork(false)
    }
}