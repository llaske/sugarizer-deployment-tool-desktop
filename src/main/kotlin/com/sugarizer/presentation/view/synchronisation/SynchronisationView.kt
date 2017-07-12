package com.sugarizer.presentation.view.synchronisation

import com.sugarizer.presentation.custom.SynchronisationTab
import javafx.fxml.FXML
import javafx.fxml.Initializable
import java.net.URL
import java.util.*

class SynchronisationView : Initializable, SynchronisationContract.View {
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

    @FXML lateinit var document: SynchronisationTab
    @FXML lateinit var music: SynchronisationTab
    @FXML lateinit var video: SynchronisationTab

    init {
        document.type = Type.DOCUMENT
        music.type = Type.MUSIC
        video.type = Type.VIDEO
    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {

    }
}