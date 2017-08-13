package com.sugarizer.view.createinstruction

import com.sugarizer.Main
import com.sugarizer.model.Instruction
import com.sugarizer.model.InstructionsModel
import com.sugarizer.utils.shared.JADB
import javafx.scene.Node
import javafx.scene.input.DataFormat
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