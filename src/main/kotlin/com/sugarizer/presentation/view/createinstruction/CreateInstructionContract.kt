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

        fun primaryStage(): Stage
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
    }
}