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

        fun disableCreation(boolean: Boolean)

        fun reset()

        fun primaryStage(): Stage

        fun isNameZipEnterred(): Boolean

        fun isDiretoryChoose(): Boolean

        fun getChoosedDirectory(): String

        fun getNameZipFile(): String

        fun setChoosedDirectory(string: String)

        fun onAddChildren(node: Node)

        fun onRemoveChildren(node: Node)

        fun translateTo(step: CreateInstructionPresenter.STEP, runnable: Runnable?)
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

        fun onClickCreateInstruction(): EventHandler<MouseEvent>

        fun onClickChooseDirectory(primaryStage: Stage): EventHandler<MouseEvent>

        fun onAddInstruction(id: String, title: String)

        fun onClickStepOne(): EventHandler<ActionEvent>

        fun onClickStepTwo(): EventHandler<ActionEvent>

        fun onClickStepThree(): EventHandler<ActionEvent>

        fun onClickStepFour(): EventHandler<ActionEvent>
    }
}