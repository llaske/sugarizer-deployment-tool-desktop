package com.sugarizer.main

import javafx.stage.Stage
import com.sugarizer.domain.shared.JADB
import com.sugarizer.inject.AppComponent
import tornadofx.App
import view.main.MainView
import javax.inject.Inject

class Main : App(MainView::class) {

    @Inject lateinit var jadb: JADB

    companion object {
        lateinit var appComponent: AppComponent
    }

    init {
        appComponent = AppComponent.init(this)
    }

    override fun start(stage: Stage) {
        super.start(stage)

        appComponent.inject(this)

        stage.isResizable = false
        stage.height = 480.0
        stage.width = 800.0
    }

    override fun stop() {
        jadb.stopADB()

        super.stop()
    }
}