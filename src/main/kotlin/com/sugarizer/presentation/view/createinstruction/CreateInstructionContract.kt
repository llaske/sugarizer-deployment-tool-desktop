package com.sugarizer.presentation.view.createinstruction

import javafx.event.ActionEvent
import javafx.event.EventHandler
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
    }

    interface Presenter {
        fun onCreateButtonDragDetected(button: Button): EventHandler<MouseEvent>

        fun onCreateButtonDragDone(): EventHandler<DragEvent>

        fun onListButtonDetected(button: Button): EventHandler<MouseEvent>

        fun onListButtonDragDone(): EventHandler<DragEvent>

        fun onPaneDragOver(pane: Pane): EventHandler<DragEvent>

        fun onCreatePaneDragDropped(pane: Pane): EventHandler<DragEvent>

        fun onListPaneDragDropped(pane: Pane): EventHandler<DragEvent>

        fun onClickInstallApk(primaryStage: Stage): EventHandler<ActionEvent>

        fun onClickCreateInstruction(): EventHandler<ActionEvent>

        fun onClickChooseDirectory(primaryStage: Stage): EventHandler<ActionEvent>
    }
}