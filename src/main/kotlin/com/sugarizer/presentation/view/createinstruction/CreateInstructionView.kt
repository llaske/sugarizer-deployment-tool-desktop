package com.sugarizer.presentation.view.createinstruction

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXDialog
import com.jfoenix.controls.JFXTextField
import com.sugarizer.main.Main
import com.sugarizer.presentation.custom.ListItemCreateInstruction
import javafx.animation.TranslateTransition
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.event.ActionEvent
import javafx.event.Event
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.input.MouseEvent
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import tornadofx.View
import javafx.scene.layout.VBox
import javafx.stage.DirectoryChooser
import javafx.stage.Stage
import javafx.util.Duration
import java.io.File
import java.net.URL
import java.util.*

class CreateInstructionView : Initializable, CreateInstructionContract.View {
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

    @FXML lateinit var root: StackPane
    @FXML lateinit var subroot: StackPane

    @FXML lateinit var selectNameDialog: JFXDialog
    @FXML lateinit var selectName: JFXTextField
    @FXML lateinit var okSelectName: JFXButton

    @FXML lateinit var selectRepositoryDialog: JFXDialog
    @FXML lateinit var selectOutputDirectory: JFXButton
    @FXML lateinit var okSelectOutput: JFXButton

    @FXML lateinit var stepOne: JFXButton
    @FXML lateinit var stepTwo: JFXButton
    @FXML lateinit var stepThree: JFXButton
    @FXML lateinit var stepFour: JFXButton

    val presenter: CreateInstructionPresenter = CreateInstructionPresenter(this)
    val inWork = SimpleBooleanProperty(false)
    val highlight = JFXButton()

    var isOutputDirectoryChoosed = false
    var isNameDirectoryChoosed = false
    var isInstructionAdded = false

    var currentStep = CreateInstructionPresenter.STEP.ONE

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

        stepOne.onMouseClicked = EventHandler { presenter.onClickStep(CreateInstructionPresenter.STEP.ONE) }
        stepTwo.onMouseClicked = EventHandler { presenter.onClickStep(CreateInstructionPresenter.STEP.TWO) }
        stepThree.onMouseClicked = EventHandler { presenter.onClickStep(CreateInstructionPresenter.STEP.THREE) }
        stepFour.onMouseClicked = EventHandler { presenter.onClickStep(CreateInstructionPresenter.STEP.FOUR) }

        okSelectName.onAction = onClickOkNameDialog()
        okSelectOutput.onAction = onClickOkRepositoryDialog()
        selectOutputDirectory.onAction = onClickSelectRepository()

        highlight.onAction = onClickStep()

        root.layout()

        highlight.style = "-fx-background-color: rgba(3, 169, 244, 0.3);" +
                "-fx-border-color: #03A9F4;" +
                "-fx-border-width: 2;"
        highlight.layoutX = stepOne.layoutX
        highlight.layoutY = stepOne.layoutY
        highlight.maxWidth = 159.0
        highlight.maxHeight = 100.0
        StackPane.setAlignment(highlight, Pos.TOP_LEFT)

        selectNameDialog.isOverlayClose = false
        selectRepositoryDialog.isOverlayClose = false

        root.children.add(highlight)
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

    override fun translateTo(step: CreateInstructionPresenter.STEP, runnable: Runnable?) {
        var tmp: JFXButton? = null

        when (step) {
            CreateInstructionPresenter.STEP.ONE -> tmp = stepOne
            CreateInstructionPresenter.STEP.TWO -> tmp = stepTwo
            CreateInstructionPresenter.STEP.THREE -> tmp = stepThree
            CreateInstructionPresenter.STEP.FOUR -> tmp = stepFour
        }

        Platform.runLater {
            var translate = TranslateTransition()

            tmp?.let {
                translate.node = highlight
                translate.toX = it.layoutX
                translate.toY = it.layoutY
                translate.duration = Duration.millis(250.0)

                translate.setOnFinished { runnable?.let { Platform.runLater(runnable) } }

                translate.play()
            }

            currentStep = step


        }
    }

    fun onClickStepOne(): EventHandler<ActionEvent> {
        return EventHandler {
            selectRepositoryDialog.show(subroot)
        }
    }

    fun onClickStepTwo(): EventHandler<ActionEvent> {
        return EventHandler {
            selectNameDialog.show(subroot)
        }
    }

    fun onClickStepThree(): EventHandler<ActionEvent> {
        return EventHandler {  }
    }

    fun onClickStepFour(): EventHandler<ActionEvent> {
        return EventHandler {  }
    }

    fun onClickStep(): EventHandler<ActionEvent> {
        return EventHandler {
            when (currentStep) {
                CreateInstructionPresenter.STEP.ONE -> onClickStepOne().handle(ActionEvent())
                CreateInstructionPresenter.STEP.TWO -> onClickStepTwo().handle(ActionEvent())
                CreateInstructionPresenter.STEP.THREE -> onClickStepThree().handle(ActionEvent())
                CreateInstructionPresenter.STEP.FOUR -> onClickStepFour().handle(ActionEvent())
            }
        }
    }

    fun onClickSelectRepository(): EventHandler<ActionEvent> {
        return EventHandler {
            var directory = DirectoryChooser()
            directory.title = "Choose the output directory"
            var choosedDirectory: File = directory.showDialog(Main.primaryStage)
            selectOutputDirectory.text = choosedDirectory.name

            it.consume()
        }
    }

    fun onClickOkRepositoryDialog(): EventHandler<ActionEvent> {
        return EventHandler {
            if (!selectOutputDirectory.text.isEmpty() && !selectOutputDirectory.equals("Select")) {

                isOutputDirectoryChoosed = true
                selectRepositoryDialog.close()

                translateTo(CreateInstructionPresenter.STEP.TWO, Runnable { onClickStep().handle(ActionEvent()) })
            } else {
                isOutputDirectoryChoosed = false
                var alert = Alert(Alert.AlertType.ERROR)

                alert.title = "Error"
                alert.headerText = null
                alert.contentText = "Select a repository"

                alert.showAndWait()
            }

            it.consume()
        }
    }

    fun onClickOkNameDialog(): EventHandler<ActionEvent> {
        return EventHandler {
            if (!selectName.text.isEmpty()) {
                isNameDirectoryChoosed = true
                selectNameDialog.close()

                translateTo(CreateInstructionPresenter.STEP.THREE, Runnable { onClickStep().handle(ActionEvent()) })
            } else {
                isNameDirectoryChoosed = false
                var alert = Alert(Alert.AlertType.ERROR)

                alert.title = "Error"
                alert.headerText = null
                alert.contentText = "Choose a name"

                alert.showAndWait()
            }

            it.consume()
        }
    }
}