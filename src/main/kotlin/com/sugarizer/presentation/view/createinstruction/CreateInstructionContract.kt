package com.sugarizer.presentation.view.createinstruction

import com.sugarizer.presentation.custom.ListItemCreateInstruction
import com.sugarizer.presentation.custom.ListItemCreateInstructionRemove
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.input.DragEvent
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.stage.Stage
import jdk.nashorn.internal.ir.CallNode

interface CreateInstructionContract {
    interface View {
        fun showProgress(boolean: Boolean)

        fun canCreate(boolean: Boolean)

        fun reset()

        fun isNameZipEnterred(): Boolean

        fun isOutputDirectoyChoose(): Boolean

        fun getChoosedDirectory(): String

        fun getNameZipFile(): String

        fun setChoosedDirectory(string: String)

        fun setIsInstructionAdded(boolean: Boolean)

        fun onAddChildren(node: Node)

        fun onRemoveChildren(node: Node)

        fun translateTo(step: CreateInstructionPresenter.STEP, runnable: Runnable? = null)
    }

    interface Presenter {
        fun onClickStep(step: CreateInstructionPresenter.STEP)

        fun onCreateButtonDragDetected(button: ListItemCreateInstruction): EventHandler<MouseEvent>

        fun onCreateButtonDragDone(): EventHandler<DragEvent>

        fun onListButtonDetected(button: ListItemCreateInstructionRemove): EventHandler<MouseEvent>

        fun onListButtonDragDone(): EventHandler<DragEvent>

        fun onPaneDragOver(): EventHandler<DragEvent>

        fun onCreatePaneDragDropped(): EventHandler<DragEvent>

        fun onListPaneDragDropped(): EventHandler<DragEvent>

        fun onClickCreateInstruction(): EventHandler<ActionEvent>

        fun onClickChooseDirectory(primaryStage: Stage): EventHandler<MouseEvent>

        fun onAddInstruction(id: String, title: String)

        fun onClickStepOne(): EventHandler<ActionEvent>

        fun onClickStepTwo(): EventHandler<ActionEvent>

        fun onClickStepThree(): EventHandler<ActionEvent>

        fun onClickStepFour(): EventHandler<ActionEvent>
    }
}