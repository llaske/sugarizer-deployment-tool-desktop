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
import javafx.stage.FileChooser
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
    @FXML lateinit var progress: StackPane

    var instructionModel: InstructionsModel = InstructionsModel()
    val map = HashMap<Node, Instruction>()

    init {
        Main.appComponent.inject(this)

        val loader = FXMLLoader(javaClass.getResource("/layout/dialog/dialog-create-instruction.fxml"))

        var view = CreateInstructionDialogView()
        loader.setRoot(view)
        loader.setController(this)

        dialogPane.scene.window.setOnCloseRequest {
        }

        title = "Create / Edit Instruction"

        try {
            loader.load<StackPane>()
            dialogPane.content = view

            instructionList.items.add(ListItemInstruction("APK", CreateInstructionView.Type.APK, "ARCHIVE"))
            instructionList.items.add(ListItemInstruction("Click", CreateInstructionView.Type.CLICK, "HAND_ALT_UP"))
            instructionList.items.add(ListItemInstruction("Long Click", CreateInstructionView.Type.LONGCLICK, "HAND_ALT_DOWN"))
            instructionList.items.add(ListItemInstruction("Key", CreateInstructionView.Type.KEY, "KEYBOARD_ALT"))
            instructionList.items.add(ListItemInstruction("Swipe", CreateInstructionView.Type.SWIPE, "HAND_ALT_LEFT"))
            instructionList.items.add(ListItemInstruction("Text", CreateInstructionView.Type.TEXT, "I_CURSOR"))
//            instructionList.items.add(ListItemInstruction("Push File", CreateInstructionView.Type.PUSH, "ARCHIVE"))
//            instructionList.items.add(ListItemInstruction("Delete File", CreateInstructionView.Type.DELETE, "ARCHIVE"))
            instructionList.items.add(ListItemInstruction("Sleep", CreateInstructionView.Type.SLEEP, "BED"))
            instructionList.items.add(ListItemInstruction("OpenApp", CreateInstructionView.Type.OPENAPP, "ANDROID"))

            instructionList.items.forEach { it.setAdd(this) }

            instructionCancel.onAction = EventHandler { (dialogPane.scene.window as Stage).close() }
            instructionCreate.onAction = onClickCreateInstruction()

            file?.let {
                progress.isVisible = true
                var zipOut = ZipOutUtils(fileUtils)

                instructionName.text = file.nameWithoutExtension
                instructionCreate.text = "Modify"

                zipOut.loadZip(it.absolutePath)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(JavaFxScheduler.platform())
                        .subscribe({},{},{
                            zipOut.instruction?.intructions?.forEach {
                                choosenInstruction.items.add(ListItemChoosenInstruction(choosenInstruction.widthProperty().subtract(40), this, it))
                            }

                            progress.isVisible = false
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
            choosenInstruction.items.removeAt(tmp)
            choosenInstruction.items.size
            choosenInstruction.items.add(tmp - 1, item)

            choosenInstruction.items.forEachIndexed { index: Int, choosen: ListItemChoosenInstruction? ->
                choosen?.instruction?.ordre = index
            }
        }
    }

    fun onArrowDown(item: ListItemChoosenInstruction) {
        val tmp = choosenInstruction.items
                .takeWhile { !it.equals(item) }
                .count()

        if (tmp < choosenInstruction.items.size - 1) {
            choosenInstruction.items.removeAt(tmp)
            choosenInstruction.items.size
            choosenInstruction.items.add(tmp + 1, item)

            choosenInstruction.items.forEachIndexed { index: Int, choosen: ListItemChoosenInstruction? ->
                choosen?.instruction?.ordre = index
            }
        }
    }

    fun onDeleteChoosenInstruction(item: ListItemChoosenInstruction) {
        choosenInstruction.items.remove(item)
        choosenInstruction.items.forEachIndexed { index: Int, choosen: ListItemChoosenInstruction? ->
            choosen?.instruction?.ordre = index
        }
    }

    fun addInstruction(type: CreateInstructionView.Type){
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
        var click = ClickInstruction(type)
        var tmp = click.showAndWait()

        if (tmp.get().equals("RESULT_CANCEL")){
            return null
        }

        var instruction: Instruction = Instruction()

        instruction.type = type
        instruction.data = tmp.get()
        instruction.ordre = choosenInstruction.items.size

        return instruction
    }

    fun onIntallApk(): Instruction? {
        var fileChooser = FileChooser()
        fileChooser.title = "Choose APK's"
        fileChooser.selectedExtensionFilter = FileChooser.ExtensionFilter("apk", "apk")
        var list = fileChooser.showOpenMultipleDialog(Main.primaryStage)
        println("Size: " + list.size)

        if (list != null && list.size > 0) {
            var model: InstallApkModel = InstallApkModel()
            var instruction = model.toInstruction(choosenInstruction.items.size, list)

            return instruction
        } else {
            return null
        }
    }

    fun onClickCreateInstruction(): EventHandler<ActionEvent> {
        return EventHandler {
            if (instructionName.text.isNotEmpty() && choosenInstruction.items.size > 0) {
                progress.isVisible = true
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

                    var zipIn = ZipInUtils(BuildConfig.SPK_LOCATION + separator + instructionName.text + ".spk", instructionModel, fileUtils, choosenInstruction.items)

                    zipIn.startZiping()
                    zipIn.finishZip()

                    Thread.sleep(1000)

                    it.onComplete()
                }
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(JavaFxScheduler.platform())
                        .subscribe({}, {}, {
                            progress.isVisible = false
                            choosenInstruction.items.clear()
                            (dialogPane.scene.window as Stage).close()
                        })
            }
        }
    }
}

class CreateInstructionDialogView() : StackPane() {

}