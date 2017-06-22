package com.sugarizer.presentation.view.loadinstruction

import com.sugarizer.presentation.custom.ListItemLoadInstruction
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.stage.Stage

interface LoadInstructionContract {
    interface View {
        fun primaryStage(): Stage

        fun setNameZip(name: String)

        fun showProgress(boolean: Boolean)

        fun addInstruction(ordre: Int, item: ListItemLoadInstruction)

        fun setProgressOnInstruction(ordre: Int, boolean: Boolean)

        fun canStart(boolean: Boolean)
    }

    interface Presenter {
        fun onClickLoad(): EventHandler<ActionEvent>

        fun onClickStart(): EventHandler<ActionEvent>
    }
}