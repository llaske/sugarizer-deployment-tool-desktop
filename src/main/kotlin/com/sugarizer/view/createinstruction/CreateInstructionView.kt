package com.sugarizer.view.createinstruction

import com.sugarizer.Main
import com.sugarizer.listitem.ListItemSpkInstruction
import com.sugarizer.utils.shared.SpkManager
import com.sugarizer.view.device.cellfactory.ListItemSpkInstructionCellFactory
import com.sugarizer.view.devicedetails.view.devicedetails.CreateInstructionDialog
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.layout.StackPane
import org.controlsfx.control.GridView
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
                .subscribe { (first, second) ->
                    when (first) {
                        SpkManager.State.CREATE -> onSpkAdded(second)
                        SpkManager.State.MODIFY -> onSpkModified(second)
                        SpkManager.State.DELETE -> onSpkRemoved(second)
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

    fun onSpkModified(file: File) {
        val contains = listSpk.items.any { it.file.equals(file) }

        if (!contains) {
            listSpk.items.add(listSpk.items.lastIndex, ListItemSpkInstruction(file, presenter, false).onItemAdded())
        }
    }
}