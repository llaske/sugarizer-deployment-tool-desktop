package com.sugarizer.view.createinstruction

import com.jfoenix.controls.JFXRippler
import com.sugarizer.BuildConfig
import com.sugarizer.model.InstallApkModel
import com.sugarizer.model.Instruction
import com.sugarizer.model.InstructionsModel
import com.sugarizer.utils.shared.JADB
import com.sugarizer.utils.shared.ZipInUtils
import com.sugarizer.Main
import com.sugarizer.listitem.ListItemCreateInstruction
import com.sugarizer.listitem.ListItemCreateInstructionRemove
import com.sugarizer.listitem.ListItemSpkInstruction
import com.sugarizer.view.createinstruction.instructions.ClickInstruction
import io.reactivex.Observable
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.Alert
import javafx.scene.image.Image
import javafx.scene.input.*
import javafx.stage.DirectoryChooser
import javafx.stage.Stage
import java.io.File
import javax.inject.Inject

class CreateInstructionPresenter(val view: CreateInstructionContract.View) : CreateInstructionContract.Presenter {
    val buttonFormat = DataFormat("com.sugarizer.formats.button")

    var draggingButton: Node? = null
    var listInstructionTmp: MutableList<Instruction> = mutableListOf()
    var instructionModel: InstructionsModel = InstructionsModel()
    val map = HashMap<Node, Instruction>()

    @Inject lateinit var jadb: JADB

    enum class STEP {
        ONE,
        TWO,
        THREE,
        FOUR
    }

    init {
        Main.appComponent.inject(this)

        instructionModel.intructions = listInstructionTmp as List<Instruction>
    }
}