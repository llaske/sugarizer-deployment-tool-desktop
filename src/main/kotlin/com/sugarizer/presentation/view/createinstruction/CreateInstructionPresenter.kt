package com.sugarizer.presentation.view.createinstruction

import com.google.gson.Gson
import com.sugarizer.BuildConfig
import com.sugarizer.domain.model.ClickModel
import com.sugarizer.domain.model.InstallApkModel
import com.sugarizer.domain.model.Instruction
import com.sugarizer.domain.model.InstructionsModel
import com.sugarizer.domain.shared.JADB
import com.sugarizer.domain.shared.ZipInUtils
import com.sugarizer.main.Main
import com.sugarizer.presentation.view.createinstruction.instructions.ClickInstruction
import io.reactivex.Observable
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.image.Image
import javafx.scene.input.*
import javafx.scene.input.TransferMode
import javafx.scene.layout.Pane
import javafx.event.ActionEvent
import javafx.scene.control.Alert
import javafx.stage.DirectoryChooser
import javafx.stage.Stage
import se.vidstige.jadb.managers.Package
import java.io.*
import javax.inject.Inject

class CreateInstructionPresenter(val view: CreateInstructionContract.View) : CreateInstructionContract.Presenter {
    val buttonFormat = DataFormat("com.sugarizer.formats.button")

    var draggingButton: Button? = null
    var listInstructionTmp: MutableList<Instruction> = mutableListOf()
    var instructionModel: InstructionsModel = InstructionsModel()

    @Inject lateinit var jadb: JADB

    init {
        Main.appComponent.inject(this)

        instructionModel.intructions = listInstructionTmp as List<Instruction>
    }

    override fun onCreateButtonDragDone(): EventHandler<DragEvent> {
        return EventHandler<DragEvent> {
            when (draggingButton?.id) {
                "installApk" -> { onIntallApk() }
                "sleep" -> onInput(ClickInstruction.Type.SLEEP)
                "inputClick" -> { onInput(ClickInstruction.Type.CLICK) }
                "inputLongClick" -> { onInput(ClickInstruction.Type.LONG_CLICK) }
                "inputKey" -> { onInput(ClickInstruction.Type.KEY) }
                "inputSwipe" -> { onInput(ClickInstruction.Type.SWIPE) }
                "inputText" -> { onInput(ClickInstruction.Type.TEXT) }
            }

            draggingButton = null
        }
    }

    override fun onCreateButtonDragDetected(button: Button): EventHandler<MouseEvent> {
        return EventHandler<MouseEvent> {
            when (button.id) {
                "inputClick", "inputSwipe", "inputLongClick" -> {
                    jadb.listJadb.forEach { device -> run {
                        Observable.create<Any> {
                            println(jadb.convertStreamToString(device.executeShell(BuildConfig.CMD_START_X_Y, "")))
                        }
                                .subscribeOn(Schedulers.computation())
                                .subscribe()
                    }
                    }
                }
            }
            val db = button.startDragAndDrop(TransferMode.COPY)
            db.dragView = button.snapshot(null, null) as Image?
            val cc = ClipboardContent()
            cc.put(buttonFormat, "button")
            db.setContent(cc)
            draggingButton = button as Button?
        }
    }

    override fun onPaneDragOver(pane: Pane): EventHandler<DragEvent> {
        return EventHandler {
            val db = it.dragboard
            if (db.hasContent(buttonFormat)
                    && draggingButton != null
                    && draggingButton?.parent !== pane) {
                it.acceptTransferModes(TransferMode.COPY)
            }
        }
    }

    override fun onCreatePaneDragDropped(pane: Pane): EventHandler<DragEvent> {
        return EventHandler {
            val db = it.dragboard
            if (db.hasContent(buttonFormat)) {
                (draggingButton?.parent as Pane).children.remove(draggingButton)
                it.isDropCompleted = true
            }
        }
    }

    override fun onListPaneDragDropped(pane: Pane): EventHandler<DragEvent> {
        return EventHandler {
            val db = it.dragboard
            if (db.hasContent(buttonFormat)) {
                val tmp = Button()

                tmp.text = draggingButton?.text
                tmp.onDragDetected = onListButtonDetected(tmp)
                tmp.onDragDone = onListButtonDragDone()
                tmp.maxWidth = Double.MAX_VALUE


                when (tmp.id) {
                    "installApk" -> { tmp.onAction = onClickInstallApk(view.primaryStage()) }
                    "inputClick" -> { tmp.onAction = EventHandler { println(jadb.convertStreamToString(jadb.listJadb[0].executeShell("input keyevent 3", ""))) } }
                }

                pane.children.add(tmp)
                it.isDropCompleted = true
            }
        }
    }

    override fun onListButtonDetected(button: Button): EventHandler<MouseEvent> {
        return EventHandler<MouseEvent> {
            val db = button.startDragAndDrop(TransferMode.COPY)
            db.dragView = button.snapshot(null, null) as Image?
            val cc = ClipboardContent()
            cc.put(buttonFormat, "button")
            db.setContent(cc)
            draggingButton = button as Button?
        }
    }

    override fun onClickInstallApk(primaryStage: Stage): EventHandler<ActionEvent> {
        return EventHandler {
            println("Install Apk Clicked")
        }
    }

    override fun onClickCreateInstruction(): EventHandler<ActionEvent> {
        return EventHandler {
            println("Size Instruction: " + instructionModel.intructions?.size)
            view.showProgress(true)

            Observable.create<String> {
                if (view.isNameZipEnterred() && view.isDiretoryChoose()) {

                    var zipIn = ZipInUtils(view.getChoosedDirectory() + "\\" + view.getNameZipFile() + ".zip", instructionModel)

                    zipIn.startZiping()
                    zipIn.finishZip()

                    it.onComplete()
                } else {
                    //throw Throwable("Test")
                    //it.onError(Throwable("Enter a name for a Zip"))
                }
            }
                    .subscribeOn(Schedulers.computation())
                    .observeOn(JavaFxScheduler.platform())
                    .doOnComplete {
                        listInstructionTmp.clear()
                        view.reset()
                        view.showProgress(false)
                    }
                    .doOnError {
                        view.showProgress(false)
                        println("onError")
                        //println(it.message)
                        var alert = Alert(Alert.AlertType.ERROR)
                        alert.title = "Error"
                        alert.contentText = "Please enter a name for the Zip archive"

                        alert.showAndWait()
                    }
                    .subscribe()
        }
    }

    override fun onListButtonDragDone(): EventHandler<DragEvent> {
        return EventHandler {
            draggingButton = null
        }
    }

    override fun onClickChooseDirectory(primaryStage: Stage): EventHandler<ActionEvent> {
        return EventHandler {
            var directory = DirectoryChooser()
            directory.title = "Choose the output directory"
            var choosedDirectory: File = directory.showDialog(primaryStage)
            view.setChoosedDirectory(choosedDirectory.absolutePath)
        }
    }

    fun onIntallApk() {
        var directory = DirectoryChooser()
        directory.title = "Choose the apk directory"
        var choosedDirectory: File = directory.showDialog(view.primaryStage())

        var instructionModel: Instruction = Instruction()
        var model: InstallApkModel = InstallApkModel()
        var listApk: MutableList<String> = mutableListOf()

        choosedDirectory.listFiles()
                .filter { it.isFile && it.extension.equals("apk") }
                .mapTo(listApk) { it.absolutePath }

        model.numberApk = listApk.size
        model.apks = listApk

        instructionModel.data = Gson().toJson(model)
        instructionModel.ordre = listInstructionTmp.size
        instructionModel.type = InstructionsModel.Type.INTALL_APK

        listInstructionTmp.add(instructionModel)

        view.disableCreation(false)
    }

    fun onInput(type: ClickInstruction.Type){
        var click = ClickInstruction(type)
        var tmp = click.showAndWait()

        if (tmp.get().equals("RESULT_CANCEL")){
            (draggingButton?.parent as Pane).children.remove(draggingButton)
            return
        }

        var instruction: Instruction = Instruction()

        when (type) {
            ClickInstruction.Type.CLICK -> { instruction.type = InstructionsModel.Type.INSTRUCTION_CLICK }
            ClickInstruction.Type.LONG_CLICK -> { instruction.type = InstructionsModel.Type.INSTRUCTION_LONG_CLICK }
            ClickInstruction.Type.KEY -> { instruction.type = InstructionsModel.Type.INSTRUCTION_KEY }
            ClickInstruction.Type.TEXT -> { instruction.type = InstructionsModel.Type.INSTRUCTION_TEXT }
            ClickInstruction.Type.SWIPE -> { instruction.type = InstructionsModel.Type.INSTRUCTION_SWIPE }
            ClickInstruction.Type.SLEEP -> instruction.type = InstructionsModel.Type.SLEEP
        }

        instruction.data = tmp.get()
        instruction.ordre = listInstructionTmp.size

        listInstructionTmp.add(instruction)

        println("Result: " + tmp.get())

        view.disableCreation(false)
    }
}