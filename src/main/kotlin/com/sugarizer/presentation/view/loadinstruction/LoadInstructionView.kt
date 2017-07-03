package com.sugarizer.presentation.view.loadinstruction

import com.sugarizer.presentation.custom.ListItemLoadInstruction
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.value.ObservableBooleanValue
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
    val startInstruction: Button by fxid("startInstruction")
    val map: HashMap<Int, ListItemLoadInstruction> = HashMap()

    val presenter: LoadInstructionPresenter = LoadInstructionPresenter(this)

    var inWork = SimpleBooleanProperty(false)

    init {
        loadInstruction.onAction = presenter.onClickLoad()
        startInstruction.onAction = presenter.onClickStart()
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

    override fun addInstruction(ordre: Int, item: ListItemLoadInstruction) {
        if (!map.containsKey(ordre)) {
            map.put(ordre, item)
            listInstruction.children.add(item)
        }
    }

    override fun setProgressOnInstruction(ordre: Int, boolean: Boolean) {
        if (map.containsKey(ordre)) {
            map[ordre]?.setProgress(boolean)
        }
    }

    override fun canStart(boolean: Boolean) {
        startInstruction.isDisable = !boolean
    }

    override fun setInWork(boolean: Boolean) {
        inWork.set(boolean)
    }

    override fun reset() {
        listInstruction.children.clear()
        map.clear()
    }
}