package com.sugarizer.presentation.view.loadinstruction

import com.google.gson.Gson
import com.sugarizer.domain.model.*
import com.sugarizer.domain.shared.JADB
import com.sugarizer.domain.shared.ZipOutUtils
import com.sugarizer.main.Main
import com.sugarizer.presentation.custom.ListItemLoadInstruction
import com.sugarizer.presentation.view.createinstruction.instructions.ClickInstruction
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.Label
import javafx.stage.FileChooser
import se.vidstige.jadb.JadbDevice
import tornadofx.c
import java.io.File
import java.security.Key
import javax.inject.Inject

class LoadInstructionPresenter(val view: LoadInstructionContract.View) : LoadInstructionContract.Presenter {
    val zipOut: ZipOutUtils = ZipOutUtils()

    @Inject lateinit var jadb: JADB

    init {
        Main.appComponent.inject(this)
    }

    override fun onClickLoad(): EventHandler<ActionEvent> {
        return EventHandler {
            var directory = FileChooser()
            directory.title = "Choose instruction zip"
            var file = directory.showOpenDialog(view.primaryStage())

            file?.let {
                view.reset()
                view.setNameZip(file.name)

                zipOut.loadZip(file.absolutePath)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(JavaFxScheduler.platform())
                        .doOnComplete {
                            println("Size: " + zipOut.instruction?.intructions?.size)
                            zipOut.instruction?.intructions?.forEach {
                                view.addInstruction(it.ordre as Int, ListItemLoadInstruction(it))
                            }

                            view.canStart(true)
                        }
                        .subscribe {
                            when (it) {
                                ZipOutUtils.STATUS.NOT_COMPLETE -> { view.showProgress(false) }
                                ZipOutUtils.STATUS.IN_PROGRESS -> { view.showProgress(true) }
                                ZipOutUtils.STATUS.COMPLETE -> { view.showProgress(false) }
                            }
                        }
            }
        }
    }

    override fun onClickStart(): EventHandler<ActionEvent> {
        return EventHandler {
            view.setInWork(true)

            try {
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

    fun executeInstructions(index: Int): Observable<String> {
        return Observable.create { subscriber -> run {
                view.canStart(false)

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

                                it.ordre?.let { view.setProgressOnInstruction(it, false) }
                            }
                            .doOnError { restart() }
                            .subscribe()
                }
            }
        }
    }

    fun executeInstruction(instruction: Instruction): Observable<String> {
        return Observable.create { subscriber -> run {
            instruction.ordre?.let { view.setProgressOnInstruction(it, true) }

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

        install.apks?.forEachIndexed { index, apk ->
            run {
                jadb.listJadb.forEach {
                    jadb.installAPK(it, File("tmp\\" + apk), true)
                            .subscribeOn(Schedulers.computation())
                            .observeOn(JavaFxScheduler.platform())
                            .doOnComplete {
                                if (index.equals(install.apks?.size?.minus(1))) {
                                    view.setProgressOnInstruction(instruction.ordre as Int, false)
                                }

                                subscriber.onComplete()
                            }
                            .subscribe()
                }
            }
        }
    }

    fun doInput(subscriber: ObservableEmitter<String>, instruction: Instruction, type: ClickInstruction.Type){
        val nb = jadb.listJadb.size

        jadb.listJadb.forEachIndexed { index, device -> run {
                Observable.create<Any> {
                    when (type) {
                        ClickInstruction.Type.CLICK -> doClick(instruction, device)
                        ClickInstruction.Type.LONG_CLICK -> doLongClick(instruction, device)
                        ClickInstruction.Type.SWIPE -> doSwipe(instruction, device)
                        ClickInstruction.Type.KEY -> doKey(instruction, device)
                        ClickInstruction.Type.TEXT -> doText(instruction, device)
                        ClickInstruction.Type.SLEEP -> doSleep(instruction, device)
                    }

                    it.onComplete()
                }
                        .subscribeOn(Schedulers.computation())
                        .observeOn(JavaFxScheduler.platform())
                        .doOnComplete {
                            if (index.equals(nb - 1)){
                                view.setProgressOnInstruction(instruction.ordre as Int, false)

                                subscriber.onComplete()
                            }
                        }
                        .subscribe()
        }
        }
    }

    fun doClick(instruction: Instruction, device: JadbDevice){
        val click = Gson().fromJson(instruction.data, ClickModel::class.java)

        device.executeShell("input tap " + click.x + " " + click.y, "")
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
    }

    fun doText(instruction: Instruction, device: JadbDevice){
        val click = Gson().fromJson(instruction.data, TextModel::class.java)

        device.executeShell("input text " + click.text, "")
    }

    fun doSleep(instruction: Instruction, device: JadbDevice){
        val click = Gson().fromJson(instruction.data, SleepModel::class.java)

        Thread.sleep(click.duration)
    }

    fun restart(){
        view.canStart(true)
        view.setInWork(false)
    }
}