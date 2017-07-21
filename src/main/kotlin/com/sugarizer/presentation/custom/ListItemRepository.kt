package com.sugarizer.presentation.custom

import com.jfoenix.controls.JFXRippler
import com.sugarizer.domain.model.RepositoryModel
import com.sugarizer.domain.shared.JADB
import com.sugarizer.domain.shared.database.RepositoryDAO
import com.sugarizer.main.Main
import io.reactivex.Observable
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.rxkotlin.toSingle
import io.reactivex.schedulers.Schedulers
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.OverrunStyle
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.Separator
import javafx.scene.input.MouseEvent
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.RowConstraints
import javafx.scene.layout.StackPane
import net.dongliu.apk.parser.ApkFile
import se.vidstige.jadb.JadbDevice
import tornadofx.gridpaneColumnConstraints
import java.io.File
import java.io.IOException
import javax.inject.Inject

class ListItemRepository(val repositoryModel: RepositoryModel) : GridPane() {
    @FXML lateinit var type: Label
    @FXML lateinit var name: Label
    @FXML lateinit var path: Label
    @FXML lateinit var remove: JFXRippler

    @Inject lateinit var repDAO: RepositoryDAO

    init {
        Main.appComponent.inject(this)

        val loader = FXMLLoader(javaClass.getResource("/layout/list-item/list-item-repository.fxml"))

        loader.setRoot(this)
        loader.setController(this)

        try {
            loader.load<GridPane>()

            type.text = repositoryModel.getRepositoryCategoryID()
            name.text = repositoryModel.getRepositoryName()
            path.text = repositoryModel.repositoryPath

            remove.onMouseClicked = onClickRemove()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun onClickRemove(): EventHandler<in MouseEvent>? {
        return EventHandler {
            repDAO.deleteRepWithId(repositoryModel.repositoryID)
        }
    }
}