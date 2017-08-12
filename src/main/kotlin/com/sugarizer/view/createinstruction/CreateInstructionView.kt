package com.sugarizer.view.createinstruction

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXDialog
import com.jfoenix.controls.JFXListView
import com.jfoenix.controls.JFXTextField
import com.sugarizer.Main
import com.sugarizer.listitem.ListItemChoosenInstruction
import com.sugarizer.listitem.ListItemCreateInstruction
import com.sugarizer.listitem.ListItemInstruction
import com.sugarizer.listitem.ListItemSpkInstruction
import com.sugarizer.utils.shared.SpkManager
import com.sugarizer.view.device.cellfactory.ListItemChoosenInstructionCellFactory
import com.sugarizer.view.device.cellfactory.ListItemInstructionCellFactory
import com.sugarizer.view.device.cellfactory.ListItemSpkInstructionCellFactory
import com.sugarizer.view.devicedetails.view.devicedetails.CreateInstructionDialog
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import javafx.animation.TranslateTransition
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Alert
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.stage.DirectoryChooser
import javafx.util.Duration
import org.controlsfx.control.GridView
import tornadofx.useMaxSize
import view.main.MainView
import java.io.File
import java.net.URL
import java.util.*
import javax.inject.Inject

class CreateInstructionView : Initializable, CreateInstructionContract.View {
    @Inject lateinit var spkManager: SpkManager

    @FXML lateinit var root: StackPane
    @FXML lateinit var listSpk: GridView<ListItemSpkInstruction>

    enum class Type {
        APK,
        CLICK,
        LONGCLICK,
        SWIPE,
        TEXT,
        KEY,
        PUSH,
        DELETE,
        SLEEP,
        OPENAPP
    }

    val presenter: CreateInstructionPresenter = CreateInstructionPresenter(this)

    init {
        Main.appComponent.inject(this)
    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        listSpk.cellHeight = 150.0
        listSpk.cellWidth = 150.0
        listSpk.cellFactory = ListItemSpkInstructionCellFactory()

        var fake = ListItemSpkInstruction(File(""), presenter, true)

        listSpk.items.add(0, fake)

        spkManager.toObservable()
                .observeOn(JavaFxScheduler.platform())
                .subscribe { file ->
                    when (file.first) {
                        SpkManager.State.CREATE -> onSpkAdded(file.second)
                        SpkManager.State.MODIFY -> println("Modified")
                        SpkManager.State.DELETE -> onSpkRemoved(file.second)
                    }
                }

        fake.setOnMouseClicked { CreateInstructionDialog(null).showAndWait() }
    }

    fun onSpkAdded(file: File) {
        val contains = listSpk.items.any { it.file.equals(file) }

        if (!contains) {
            listSpk.items.add(listSpk.items.lastIndex, ListItemSpkInstruction(file, presenter, false).onItemAdded())
        }
    }

    fun onSpkRemoved(file: File) {
        val tmp: ListItemSpkInstruction? = listSpk.items.lastOrNull { it.file.nameWithoutExtension.equals(file.nameWithoutExtension) }

        tmp?.let {
            var fade = it.onItemRemoved()
            fade.setOnFinished { Platform.runLater { listSpk.items.remove(tmp) } }
            fade.play()
        }
    }
}