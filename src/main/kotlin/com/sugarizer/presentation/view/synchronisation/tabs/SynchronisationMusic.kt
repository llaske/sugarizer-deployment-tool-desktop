package com.sugarizer.presentation.view.synchronisation.tabs

import com.jfoenix.controls.JFXListView
import com.sugarizer.domain.model.MusicModel
import com.sugarizer.domain.shared.database.MusicDAO
import com.sugarizer.main.Main
import com.sugarizer.presentation.view.synchronisation.SynchronisationView
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.layout.GridPane
import java.io.IOException
import javax.inject.Inject

class SynchronisationMusic : GridPane() {
    @FXML lateinit var list: JFXListView<String>

    @Inject lateinit var musicDAO: MusicDAO

    var type: SynchronisationView.Type = SynchronisationView.Type.MUSIC

    init {
        Main.appComponent.inject(this)

        val loader = FXMLLoader(javaClass.getResource("/layout/custom-synchronisation-tab.fxml"))

        loader.setRoot(this)
        loader.setController(this)

        try {
            loader.load<GridPane>()

            musicDAO.searchMusic().forEach { list.items.add(it.musicName) }
            musicDAO.toObservable().subscribe {
                when (it.first) {
                    MusicDAO.State.ADDED -> onAdded(it.second)
                    MusicDAO.State.CHANGED -> onChanged(it.second)
                    MusicDAO.State.REMOVED -> onRemoved(it.second)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun onAdded(musicModel: MusicModel) {
        if (!list.items.contains(musicModel.musicName)){
            list.items.add(musicModel.musicName)
        }
    }

    fun onChanged(musicModel: MusicModel) {

    }

    fun onRemoved(musicModel: MusicModel) {
        if (list.items.contains(musicModel.musicName)){
            list.items.remove(musicModel.musicName)
        }
    }
}