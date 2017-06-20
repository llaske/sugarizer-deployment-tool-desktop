package com.sugarizer.presentation.view.createinstruction

import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import javafx.scene.layout.StackPane
import tornadofx.View
import javafx.scene.layout.VBox
import javafx.stage.Stage

class CreateInstructionView : View(), CreateInstructionContract.View {
    override val root: StackPane by fxml("/layout/view-create-instruction.fxml")

    val createInstruction: GridPane by fxid("createInstruction")
    val progress: VBox by fxid("progress")

    val listPane: VBox by fxid("tab1")
    val createPane: VBox by fxid("tab2")

    val installApk: Button by fxid("installApk")
    val pushFile: Button by fxid("pushFile")
    val deleteFile: Button by fxid("deleteFile")

    val creation: Button by fxid("creation")
    val choosedDirectory: TextField by fxid("choosedDirectory")
    val chooseDirectory: Button by fxid("chooseDirectory")
    val nameZip: TextField by fxid("nameZip")

    val presenter: CreateInstructionPresenter = CreateInstructionPresenter(this)

    init {
        for (tmp in  createPane.children){
            tmp.onDragDetected = presenter.onCreateButtonDragDetected(tmp as Button)
            tmp.onDragDone = presenter.onCreateButtonDragDone()
        }

        listPane.onDragOver = presenter.onPaneDragOver(listPane)
        listPane.onDragDropped = presenter.onListPaneDragDropped(listPane)
        createPane.onDragOver = presenter.onPaneDragOver(createPane)
        createPane.onDragDropped = presenter.onCreatePaneDragDropped(createPane)

        creation.onAction = presenter.onClickCreateInstruction()
        chooseDirectory.onAction = presenter.onClickChooseDirectory(primaryStage)
    }

    override fun showProgress(boolean: Boolean) {
        progress.isVisible = boolean
        createInstruction.isDisable = boolean
    }

    override fun primaryStage(): Stage {
        return primaryStage
    }

    override fun disableCreation(boolean: Boolean) {
        creation.isDisable = boolean
    }

    override fun reset() {
        choosedDirectory.text = ""
        nameZip.text = "Name of output zip"
        listPane.children.clear()
    }

    override fun isNameZipEnterred(): Boolean {
        return nameZip.text.isNotEmpty() && !nameZip.text.equals("Name of output zip")
    }

    override fun getChoosedDirectory(): String {
        return choosedDirectory.text
    }

    override fun getNameZipFile(): String {
        return nameZip.text
    }

    override fun isDiretoryChoose(): Boolean {
        return choosedDirectory.text.isNotEmpty()
    }

    override fun setChoosedDirectory(string: String) {
        choosedDirectory.text = string
    }
}