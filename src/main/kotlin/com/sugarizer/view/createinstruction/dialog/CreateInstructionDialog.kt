package com.sugarizer.view.devicedetails.view.devicedetails

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXListView
import com.jfoenix.controls.JFXTextField
import com.sugarizer.BuildConfig
import com.sugarizer.Main
import com.sugarizer.listitem.ListItemChoosenInstruction
import com.sugarizer.listitem.ListItemInstruction
import com.sugarizer.model.InstallApkModel
import com.sugarizer.model.Instruction
import com.sugarizer.model.InstructionsModel
import com.sugarizer.utils.shared.*
import com.sugarizer.view.createinstruction.CreateInstructionView
import com.sugarizer.view.createinstruction.instructions.ClickInstruction
import io.reactivex.Observable
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.Dialog
import javafx.scene.layout.StackPane
import javafx.stage.DirectoryChooser
import javafx.stage.Stage
import java.io.File
import java.io.IOException
import javax.inject.Inject


class CreateInstructionDialog(val file: File?) : Dialog<String>() {

    @Inject lateinit var jadb: JADB
    @Inject lateinit var fileUtils: FileUtils

    @FXML lateinit var root: StackPane
    @FXML lateinit var instructionList: JFXListView<ListItemInstruction>
    @FXML lateinit var choosenInstruction: JFXListView<ListItemChoosenInstruction>
    @FXML lateinit var instructionCreate: JFXButton
    @FXML lateinit var instructionCancel: JFXButton
    @FXML lateinit var instructionName: JFXTextField

    var listInstructionTmp: MutableList<Instruction> = mutableListOf()
    var instructionModel: InstructionsModel = InstructionsModel()
    val map = HashMap<Node, Instruction>()

    init {
        Main.appComponent.inject(this)
        instructionModel.intructions = listInstructionTmp as List<Instruction>

        val loader = FXMLLoader(javaClass.getResource("/layout/dialog/dialog-create-instruction.fxml"))

        var view = CreateInstructionDialogView()
        loader.setRoot(view)
        loader.setController(this)

        //dialogPane.buttonTypes.add(ButtonType.CANCEL)
        dialogPane.scene.window.setOnCloseRequest {
        }

        title = "Create / Edit Instruction"

        try {
            loader.load<StackPane>()
            dialogPane.content = view

            instructionList.items.add(ListItemInstruction("APK", CreateInstructionView.Type.APK, "ARCHIVE"))
            instructionList.items.add(ListItemInstruction("Click", CreateInstructionView.Type.CLICK, "ARCHIVE"))
            instructionList.items.add(ListItemInstruction("Long Click", CreateInstructionView.Type.LONGCLICK, "ARCHIVE"))
            instructionList.items.add(ListItemInstruction("Key", CreateInstructionView.Type.KEY, "ARCHIVE"))
            instructionList.items.add(ListItemInstruction("Swipe", CreateInstructionView.Type.SWIPE, "ARCHIVE"))
            instructionList.items.add(ListItemInstruction("Text", CreateInstructionView.Type.TEXT, "ARCHIVE"))
//            instructionList.items.add(ListItemInstruction("Push File", CreateInstructionView.Type.PUSH, "ARCHIVE"))
//            instructionList.items.add(ListItemInstruction("Delete File", CreateInstructionView.Type.DELETE, "ARCHIVE"))
            instructionList.items.add(ListItemInstruction("Sleep", CreateInstructionView.Type.SLEEP, "ARCHIVE"))
            instructionList.items.add(ListItemInstruction("OpenApp", CreateInstructionView.Type.OPENAPP, "ARCHIVE"))

            instructionList.items.forEach { it.setAdd(this) }

            instructionCancel.onAction = EventHandler { (dialogPane.scene.window as Stage).close() }
            instructionCreate.onAction = onClickCreateInstruction()

            file?.let {
                var zipOut = ZipOutUtils(fileUtils)

                instructionName.text = file.nameWithoutExtension
                instructionCreate.text = "Modify"

                zipOut.loadZip(it.absolutePath)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(JavaFxScheduler.platform())
                        .subscribe({},{},{
                            zipOut.instruction?.intructions?.forEach {
                                listInstructionTmp.add(it.ordre as Int, it)
                                choosenInstruction.items.add(ListItemChoosenInstruction(choosenInstruction.widthProperty().subtract(40), this, it))
                            }
                        })
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun onArrowUp(item: ListItemChoosenInstruction) {
        val tmp = choosenInstruction.items
                .takeWhile { !it.equals(item) }
                .count()

        if (tmp > 0) {
            listInstructionTmp.removeAt(tmp)
            choosenInstruction.items.removeAt(tmp)
            choosenInstruction.items.size
            listInstructionTmp.add(tmp - 1, item.instruction)
            choosenInstruction.items.add(tmp - 1, item)
        }
    }

    fun onArrowDown(item: ListItemChoosenInstruction) {
        val tmp = choosenInstruction.items
                .takeWhile { !it.equals(item) }
                .count()

        if (tmp < choosenInstruction.items.size - 1) {
            listInstructionTmp.removeAt(tmp)
            choosenInstruction.items.removeAt(tmp)
            choosenInstruction.items.size
            listInstructionTmp.add(tmp + 1, item.instruction)
            choosenInstruction.items.add(tmp + 1, item)
        }
    }

    fun onDeleteChoosenInstruction(item: ListItemChoosenInstruction) {
        listInstructionTmp.remove(item.instruction)
        for ((i, tmp) in listInstructionTmp.withIndex()) {
            tmp.ordre = i
        }
        choosenInstruction.items.remove(item)
    }

    fun addInstruction(type: CreateInstructionView.Type){
        println("Type:" + type)
        var tmpInstruction: Instruction? = null

        when (type) {
            CreateInstructionView.Type.APK -> { tmpInstruction = onIntallApk() }
            else -> { tmpInstruction = onInput(type) }
        }

        if (tmpInstruction != null) {
            choosenInstruction.items.add(ListItemChoosenInstruction(choosenInstruction.widthProperty().subtract(40), this, tmpInstruction))
        }
    }

    fun onInput(type: CreateInstructionView.Type): Instruction? {
        println("Type: " + type)
        var click = ClickInstruction(type)
        var tmp = click.showAndWait()

        if (tmp.get().equals("RESULT_CANCEL")){
            return null
        }
        println("RESULT not cancel")

        var instruction: Instruction = Instruction()

        instruction.type = type
        instruction.data = tmp.get()
        instruction.ordre = listInstructionTmp.size

        listInstructionTmp.add(instruction)
        println("Size:" + listInstructionTmp.size)

        return instruction
    }

    fun onIntallApk(): Instruction? {
        var directory = DirectoryChooser()
        directory.title = "Choose the apk directory"
        var choosedDirectory = directory.showDialog(Main.primaryStage)

        if (choosedDirectory != null) {
            var model: InstallApkModel = InstallApkModel()
            var instruction = model.toInstruction(listInstructionTmp.size, choosedDirectory)

            listInstructionTmp.add(instruction)

            return instruction
        } else {
            return null
        }
    }

    fun onClickCreateInstruction(): EventHandler<ActionEvent> {
        return EventHandler {
            if (instructionName.text.isNotEmpty() && listInstructionTmp.size > 0) {
                file?.let {
                    it.delete()
                }
                Observable.create<String> {

                    val separator = when (OsCheck.operatingSystemType) {
                        OsCheck.OSType.Windows -> { BuildConfig.FILE_SEPARATOR_WINDOWS }
                        OsCheck.OSType.Linux -> { BuildConfig.FILE_SEPARATOR_LINUX }
                        OsCheck.OSType.MacOS -> { BuildConfig.FILE_SEPARATOR_MAC }
                        else -> { BuildConfig.FILE_SEPARATOR_LINUX }
                    }

                    println("Size: " + listInstructionTmp.size)
                    var zipIn = ZipInUtils(BuildConfig.SPK_LOCATION + separator + instructionName.text + ".spk", instructionModel, fileUtils)

                    zipIn.startZiping()
                    zipIn.finishZip()

                    it.onComplete()
                }
                        .subscribeOn(Schedulers.computation())
                        .observeOn(JavaFxScheduler.platform())
                        .subscribe({}, {}, {
                            println("Closed ?")
                            listInstructionTmp.clear()
                            (dialogPane.scene.window as Stage).close()
                        })
            }
        }
    }
}

class CreateInstructionDialogView() : StackPane() {

}