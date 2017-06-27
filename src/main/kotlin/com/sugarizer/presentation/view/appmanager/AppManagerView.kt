package com.sugarizer.presentation.view.appmanager

import com.sugarizer.domain.shared.JADB
import com.sugarizer.main.Main
import com.sugarizer.presentation.custom.ListItemApplication
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import tornadofx.View
import javax.inject.Inject

class AppManagerView : View(), AppManagerContract.View {
    override val root: GridPane by fxml("/layout/view-app-manager.fxml")

    @Inject lateinit var jadb: JADB

    val chooser: Button by fxid("chooseRepository")
    val install: Button by fxid("install")
    val repositoryArea: Label by fxid("repository")
    val presenter: AppManagerPresenter
    val listView: ListView<ListItemApplication> by fxid("listApp")
    val forceInstall: CheckBox by fxid("forceInstall")

    init {
        Main.appComponent.inject(this)

        presenter = AppManagerPresenter(this, jadb)

        chooser.onAction = presenter.onChooseRepositoryClick(primaryStage)
        install.onAction = presenter.onInstallClick()
    }

    override fun onFileRemoved(item: ListItemApplication) {
        listView.items.remove(item)
    }

    override fun onFileAdded(item: ListItemApplication) {
        listView.items.add(item)
    }

    override fun setRepository(string: String) {
        repositoryArea.text = string
    }

    override fun setInstallDisable(boolean: Boolean) {
        install.isDisable = boolean
    }

    override fun isForceInstall(): Boolean {
        return forceInstall.isSelected
    }

    override fun start() {
        listView.items.forEach {
            it.startInstall(isForceInstall())
        }
    }
}