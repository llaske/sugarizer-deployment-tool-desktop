package com.sugarizer.presentation.view.createinstruction

import com.sugarizer.main.Main
import com.sugarizer.presentation.custom.ListItemCreateInstruction
import javafx.beans.property.SimpleBooleanProperty
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import javafx.scene.layout.StackPane
import tornadofx.View
import javafx.scene.layout.VBox
import javafx.stage.Stage
import java.net.URL
import java.util.*

class CreateInstructionView : Initializable, CreateInstructionContract.View {

    //val listPane: VBox by fxid("tab1")
    //val createPane: VBox by fxid("tab2")

//    val installApk: Button by fxid("installApk")
//    val pushFile: Button by fxid("pushFile")
//    val deleteFile: Button by fxid("deleteFile")
//
//    val creation: Button by fxid("creation")
//    val choosedDirectory: Label by fxid("choosedDirectory")
//    val chooseDirectory: Button by fxid("chooseDirectory")
//    val nameZip: TextField by fxid("nameZip")

    @FXML lateinit var createInstruction: GridPane
    @FXML lateinit var progress: VBox
    @FXML lateinit var tab1: VBox
    @FXML lateinit var tab2: VBox
    @FXML lateinit var installApk: ListItemCreateInstruction
    @FXML lateinit var pushFile: ListItemCreateInstruction
    @FXML lateinit var deleteFile: ListItemCreateInstruction
    @FXML lateinit var creation: Button
    @FXML lateinit var choosedDirectory: Label
    @FXML lateinit var chooseDirectory: Button
    @FXML lateinit var nameZip: TextField

    val presenter: CreateInstructionPresenter = CreateInstructionPresenter(this)

    val inWork = SimpleBooleanProperty(false)

    init {

    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        for (tmp in  tab2.children){
            tmp.onDragDetected = presenter.onCreateButtonDragDetected(tmp as ListItemCreateInstruction)
            tmp.onDragDone = presenter.onCreateButtonDragDone()
            tmp.addButton.onMouseClicked = EventHandler { presenter.onAddInstruction(tmp.id, tmp.getTitleTest()) }
        }

        tab1.onDragOver = presenter.onPaneDragOver()
        tab1.onDragDropped = presenter.onListPaneDragDropped()
        tab2.onDragOver = presenter.onPaneDragOver()
        tab2.onDragDropped = presenter.onCreatePaneDragDropped()

        creation.onAction = presenter.onClickCreateInstruction()
        chooseDirectory.onAction = presenter.onClickChooseDirectory(Main.primaryStage)
    }

    override fun showProgress(boolean: Boolean) {
        progress.isVisible = boolean
        createInstruction.isDisable = boolean
        inWork.set(boolean)
    }

    override fun primaryStage(): Stage {
        return Main.primaryStage
    }

    override fun disableCreation(boolean: Boolean) {
        creation.isDisable = boolean
    }

    override fun reset() {
        choosedDirectory.text = ""
        nameZip.text = "Name of output zip"
        tab1.children.clear()
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
        tab1.children.add(node)
    }

    override fun onRemoveChildren(node: Node) {
        tab1.children.remove(node)
    }
}