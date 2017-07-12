package com.sugarizer.presentation.view.appmanager

import com.jfoenix.controls.JFXCheckBox
import com.sugarizer.domain.shared.JADB
import com.sugarizer.main.Main
import com.sugarizer.presentation.custom.ListItemApplication
import io.reactivex.Observable
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import sun.rmi.runtime.Log
import tornadofx.View
import java.net.URL
import java.util.*
import javax.inject.Inject

class AppManagerView : Initializable, AppManagerContract.View {
    @Inject lateinit var jadb: JADB

    @FXML lateinit var chooseRepository: Node
    @FXML lateinit var install: Node
    @FXML lateinit var listApp: ListView<ListItemApplication>
    @FXML lateinit var repository: Label
    @FXML lateinit var forceInstall: JFXCheckBox

    val presenter: AppManagerPresenter

    init {
        Main.appComponent.inject(this)

        presenter = AppManagerPresenter(this, jadb)
    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        Main.appComponent.inject(this)

        chooseRepository.onMouseClicked = presenter.onChooseRepositoryClick(Main.primaryStage)
        install.onMouseClicked = presenter.onInstallClick()
    }

    override fun onFileRemoved(item: ListItemApplication) {
        listApp.items.remove(item)
    }

    override fun onFileAdded(item: ListItemApplication) {
        println("File Added ?")
        listApp.items.add(item)
        println("Size: " + listApp.items.size)
    }

    override fun setRepository(string: String) {
        repository.text = string
    }

    override fun setInstallDisable(boolean: Boolean) {
        install.isDisable = boolean
    }

    override fun isForceInstall(): Boolean {
        return forceInstall.isSelected
    }

    override fun start() {
        val tmpNb = listApp.items.size
        var i = 0

        Observable.create<String> { subscriber -> run {
            listApp.items.forEach {
                    it.startInstall(isForceInstall())
                            .subscribeOn(Schedulers.computation())
                            .observeOn(JavaFxScheduler.platform())
                            .doOnComplete {
                                println("setInstall - 1")
                                subscriber.onNext("")
                            }
                            .subscribe {}
                }
            }
        }
                .subscribeOn(Schedulers.computation())
                .observeOn(JavaFxScheduler.platform())
                .doOnComplete { setInstallDisable(false) }
                .subscribe {
                    println("setInstall - onNext")
                    ++i

                    if (i >= tmpNb) {
                        println("setInstall - Finish")
                        setInstallDisable(false)
                    }
                }
    }
}