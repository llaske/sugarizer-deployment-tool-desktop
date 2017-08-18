package com.sugarizer

import com.sugarizer.utils.shared.JADB
import com.sugarizer.utils.inject.AppComponent
import javafx.scene.image.Image
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
        stage.icons.add(Image("/image/icon.png"))

        Companion.primaryView = primaryView
    }

    override fun stop() {
        jadb.stopADB()
        jadb.stopWatching()

        super.stop()
    }
}