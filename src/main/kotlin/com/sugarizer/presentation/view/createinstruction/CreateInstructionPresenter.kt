package com.sugarizer.presentation.view.createinstruction

import com.google.gson.Gson
import com.sugarizer.domain.model.InstallApkModel
import com.sugarizer.domain.model.Instruction
import com.sugarizer.domain.model.InstructionsModel
import com.sugarizer.domain.model.Model
import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.image.Image
import javafx.scene.input.*
import javafx.scene.input.TransferMode
import javafx.scene.layout.Pane
import javafx.event.ActionEvent
import javafx.stage.DirectoryChooser
import javafx.stage.Stage
import java.io.File


class CreateInstructionPresenter(val view: CreateInstructionContract.View) : CreateInstructionContract.Presenter {
    val buttonFormat = DataFormat("com.sugarizer.formats.button")

    var draggingButton: Button? = null
    var listInstructionTmp: MutableList<Instruction<*>> = mutableListOf()
    var instructionModel: InstructionsModel<Model> = InstructionsModel()

    init {
        instructionModel.intructions = listInstructionTmp as List<Instruction<Model>>
    }

    override fun onCreateButtonDragDone(): EventHandler<DragEvent> {
        return EventHandler<DragEvent> {
            when (draggingButton?.text) {
                "Install Application" -> { onIntallApk() }
            }

            draggingButton = null
        }
    }

    override fun onCreateButtonDragDetected(button: Button): EventHandler<MouseEvent> {
        return EventHandler<MouseEvent> {
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

                when (tmp.text) {
                    "Install Application" -> { tmp.onAction = onClickInstallApk(view.primaryStage()) }
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
            println(Gson().toJson(instructionModel, InstructionsModel::class.java).toString())

            view.showProgress(true)
        }
    }

    override fun onListButtonDragDone(): EventHandler<DragEvent> {
        return EventHandler {
            draggingButton = null
        }
    }

    fun onIntallApk() {
        var directory = DirectoryChooser()
        directory.title = "Choose the apk directory"
        var choosedDirectory: File = directory.showDialog(view.primaryStage())

        var instructionModel: Instruction<InstallApkModel> = Instruction()
        var model: InstallApkModel = InstallApkModel()
        var listApk: MutableList<String> = mutableListOf()

        choosedDirectory.listFiles()
                .filter { it.isFile && it.extension.equals("apk") }
                .mapTo(listApk) { it.absolutePath }

        model.numberApk = listApk.size
        model.apks = listApk

        instructionModel.data = model
        instructionModel.ordre = listInstructionTmp.size
        instructionModel.type = InstructionsModel.Type.INTALL_APK

        listInstructionTmp.add(instructionModel)

        view.disableCreation(false)
    }
}