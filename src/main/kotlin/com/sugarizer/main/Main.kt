package com.sugarizer.main

import com.sugarizer.domain.shared.JADB
import com.sugarizer.inject.AppComponent
import javafx.stage.Stage
import tornadofx.App
import tornadofx.UIComponent
import view.main.MainView
import javax.inject.Inject
import kotlin.reflect.KClass

class Main : App(MainView::class) {

    @Inject lateinit var jadb: JADB

    companion object {
        lateinit var appComponent: AppComponent
        lateinit var primaryStage: Stage
        lateinit var primaryView: KClass<out UIComponent>
    }

    init {
        appComponent = AppComponent.init(this)
        appComponent.inject(this)
    }

    override fun start(stage: Stage) {
        primaryStage = stage
        super.start(stage)

        stage.isResizable = true
        stage.height = 480.0
        stage.width = 800.0
        stage.minHeight = 480.0
        stage.minWidth = 800.0

        Main.primaryView = primaryView
    }

    override fun stop() {
        jadb.stopADB()
        jadb.stopWatching()

        super.stop()
    }
}