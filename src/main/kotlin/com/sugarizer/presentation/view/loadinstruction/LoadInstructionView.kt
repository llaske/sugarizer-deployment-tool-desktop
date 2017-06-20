package com.sugarizer.presentation.view.loadinstruction

import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.GridPane
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.stage.Stage
import tornadofx.View

class LoadInstructionView : View(), LoadInstructionContract.View{
    override val root: StackPane by fxml("/layout/view-load-instruction.fxml")

    val loadInstruction: Button by fxid("loadInstruction")
    val nameFile: Label by fxid("nameFile")
    val progress: VBox by fxid("progress")
    val grid: GridPane by fxid("content")
    val listInstruction: VBox by fxid("listInstruction")

    val presenter: LoadInstructionPresenter = LoadInstructionPresenter(this)

    init {
        loadInstruction.onAction = presenter.onClickLoad()
    }

    override fun primaryStage(): Stage {
        return primaryStage
    }

    override fun setNameZip(name: String) {
        nameFile.text = name
    }

    override fun showProgress(boolean: Boolean) {
        progress.isVisible = boolean
        grid.isDisable = boolean
    }

    override fun addInstruction(node: Node) {
        listInstruction.children.add(node)
    }
}