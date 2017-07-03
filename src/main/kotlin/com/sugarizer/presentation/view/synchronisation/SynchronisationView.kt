package com.sugarizer.presentation.view.synchronisation

import com.sugarizer.presentation.custom.SynchronisationTab
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.Parent
import javafx.scene.control.TabPane
import javafx.scene.layout.GridPane
import tornadofx.View

class SynchronisationView : View(), SynchronisationContract.View {
    override val root: TabPane by fxml("/layout/view-synchronisation.fxml")

    enum class Type {
        DOCUMENT,
        MUSIC,
        VIDEO;

        override fun toString(): String {
            when (this) {
                DOCUMENT -> return "document"
                MUSIC -> return "music"
                VIDEO -> return "video"
                else -> return ""
            }
        }
    }

    val document: SynchronisationTab by fxid("document")
    val music: SynchronisationTab by fxid("music")
    val video: SynchronisationTab by fxid("video")

    init {
        document.type = Type.DOCUMENT
        music.type = Type.MUSIC
        video.type = Type.VIDEO
    }
}