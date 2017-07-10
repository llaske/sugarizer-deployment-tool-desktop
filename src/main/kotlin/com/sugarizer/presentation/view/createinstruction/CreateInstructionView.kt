package com.sugarizer.presentation.view.createinstruction

import com.sugarizer.presentation.custom.ListItemCreateInstruction
import javafx.beans.property.SimpleBooleanProperty
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import javafx.scene.layout.StackPane
import tornadofx.View
import javafx.scene.layout.VBox
import javafx.stage.Stage
import java.util.*
import kotlin.collections.HashMap

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
    val choosedDirectory: Label by fxid("choosedDirectory")
    val chooseDirectory: Button by fxid("chooseDirectory")
    val nameZip: TextField by fxid("nameZip")

    val presenter: CreateInstructionPresenter = CreateInstructionPresenter(this)

    val inWork = SimpleBooleanProperty(false)

    init {
        for (tmp in  createPane.children){
            //tmp.maxWidth(createPane.width)
            tmp.onDragDetected = presenter.onCreateButtonDragDetected(tmp as ListItemCreateInstruction)
            tmp.onDragDone = presenter.onCreateButtonDragDone()
            tmp.addButton.onMouseClicked = EventHandler { presenter.onAddInstruction(tmp.id, tmp.getTitleTest()) }
        }
//
        listPane.onDragOver = presenter.onPaneDragOver()
        listPane.onDragDropped = presenter.onListPaneDragDropped()
        createPane.onDragOver = presenter.onPaneDragOver()
        createPane.onDragDropped = presenter.onCreatePaneDragDropped()
//
        creation.onAction = presenter.onClickCreateInstruction()
        chooseDirectory.onAction = presenter.onClickChooseDirectory(primaryStage)
    }

    override fun showProgress(boolean: Boolean) {
        progress.isVisible = boolean
        createInstruction.isDisable = boolean
        inWork.set(boolean)
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
        creation.isDisable = true
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

    override fun onAddChildren(node: Node) {
        listPane.children.add(node)
    }

    override fun onRemoveChildren(node: Node) {
        listPane.children.remove(node)
    }
}