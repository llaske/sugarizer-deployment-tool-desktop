package com.sugarizer.presentation.view.appmanager

import com.sugarizer.presentation.custom.ListItemApplication
import com.sun.org.apache.xpath.internal.operations.Bool
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.stage.Stage

interface AppManagerContract {
    interface View {
        fun setRepository(string: String)

        fun setInstallDisable(boolean: Boolean)

        fun onFileAdded(item: ListItemApplication)

        fun onFileRemoved(item: ListItemApplication)

        fun isForceInstall() : Boolean

        fun start()
    }

    interface Presenter {
        fun onChooseRepositoryClick(primaryStage: Stage): EventHandler<ActionEvent>

        fun onInstallClick(): EventHandler<ActionEvent>
    }
}