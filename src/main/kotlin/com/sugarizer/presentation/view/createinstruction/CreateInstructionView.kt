package com.sugarizer.presentation.view.createinstruction

import javafx.scene.control.Button
import javafx.scene.control.ProgressIndicator
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

        //installApk.onAction = presenter.onClickInstallApk(primaryStage)

        creation.onAction = presenter.onClickCreateInstruction()
    }

    override fun showProgress(boolean: Boolean) {
        progress.isVisible = true
        createInstruction.isDisable = true
    }

    override fun primaryStage(): Stage {
        return primaryStage
    }

    override fun disableCreation(boolean: Boolean) {
        creation.isDisable = boolean
    }
}