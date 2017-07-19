package com.sugarizer.main

import javafx.stage.Stage
import com.sugarizer.domain.shared.JADB
import com.sugarizer.inject.AppComponent
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
    }

    override fun start(stage: Stage) {
        primaryStage = stage
        super.start(stage)

        appComponent.inject(this)

        stage.isResizable = false
        stage.height = 480.0
        stage.width = 800.0

        Main.primaryView = primaryView
    }

    override fun stop() {
        jadb.stopADB()
        jadb.stopWatching()

        super.stop()
    }
}