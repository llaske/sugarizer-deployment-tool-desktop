package com.sugarizer.presentation.view.loadinstruction

import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.stage.Stage

interface LoadInstructionContract {
    interface View {
        fun primaryStage(): Stage

        fun setNameZip(name: String)

        fun showProgress(boolean: Boolean)

        fun addInstruction(node: Node)
    }

    interface Presenter {
        fun onClickLoad(): EventHandler<ActionEvent>
    }
}