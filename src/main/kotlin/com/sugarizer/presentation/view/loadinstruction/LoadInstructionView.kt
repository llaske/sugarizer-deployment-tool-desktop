package com.sugarizer.presentation.view.loadinstruction

import com.sugarizer.main.Main
import com.sugarizer.presentation.custom.ListItemLoadInstruction
import javafx.beans.property.SimpleBooleanProperty
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.GridPane
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.stage.Stage
import tornadofx.View
import java.net.URL
import java.util.*

class LoadInstructionView : Initializable, LoadInstructionContract.View {
    @FXML lateinit var loadInstruction: Button
    @FXML lateinit var nameFile: Label
    @FXML lateinit var progress: VBox
    @FXML lateinit var content: GridPane
    @FXML lateinit var listInstruction: VBox
    @FXML lateinit var startInstruction: Button

    val presenter: LoadInstructionPresenter = LoadInstructionPresenter(this)
    val map: HashMap<Int, ListItemLoadInstruction> = HashMap()
    var inWork = SimpleBooleanProperty(false)

    init {

    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        loadInstruction.onAction = presenter.onClickLoad()
        startInstruction.onAction = presenter.onClickStart()
    }

    override fun primaryStage(): Stage {
        return Main.primaryStage
    }

    override fun setNameZip(name: String) {
        nameFile.text = name
    }

    override fun showProgress(boolean: Boolean) {
        progress.isVisible = boolean
        content.isDisable = boolean
    }

    override fun addInstruction(ordre: Int, item: ListItemLoadInstruction) {
        if (!map.containsKey(ordre)) {
            map.put(ordre, item)
            listInstruction.children.add(item)
        }
    }

    override fun setProgressOnInstruction(ordre: Int, boolean: Boolean) {
        if (map.containsKey(ordre)) {
            map[ordre]?.setProgress(boolean)
        }
    }

    override fun canStart(boolean: Boolean) {
        startInstruction.isDisable = !boolean
    }

    override fun setInWork(boolean: Boolean) {
        inWork.set(boolean)
    }

    override fun reset() {
        listInstruction.children.clear()
        map.clear()
    }
}