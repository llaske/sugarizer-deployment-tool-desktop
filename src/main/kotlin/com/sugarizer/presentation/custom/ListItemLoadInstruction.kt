package com.sugarizer.presentation.custom

import com.sugarizer.domain.model.Instruction
import com.sugarizer.domain.model.InstructionsModel
import com.sugarizer.presentation.view.createinstruction.instructions.ClickInstructionView
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.RowConstraints
import tornadofx.gridpaneColumnConstraints

class ListItemLoadInstruction(val instruction: Instruction) : GridPane() {
    val ordreLabel: Label = Label(instruction.ordre.toString())
    val typeLabel: Label = Label(instruction.type.toString())
    val info: Label = Label("")
    val progress: ProgressIndicator = ProgressIndicator()
    val separator: Separator = Separator(Orientation.HORIZONTAL)

    init {
        maxHeight = 50.0
        maxWidth = Double.MAX_VALUE

        val rowOne = RowConstraints()
        val rowTwo = RowConstraints()

        val columnOne = ColumnConstraints()
        val columnTwo = ColumnConstraints()
        val columnThree = ColumnConstraints()
        val columnFour = ColumnConstraints()

        columnOne.percentWidth = 10.0
        columnTwo.percentWidth = 40.0
        columnThree.percentWidth = 40.0
        columnFour.percentWidth = 10.0

        rowConstraints.add(rowOne)
        rowConstraints.add(rowTwo)

        columnConstraints.add(columnOne)
        columnConstraints.add(columnTwo)
        columnConstraints.add(columnThree)
        columnConstraints.add(columnFour)

        ordreLabel.alignment = Pos.CENTER
        ordreLabel.maxWidth = Double.MAX_VALUE
        typeLabel.alignment = Pos.CENTER
        typeLabel.maxWidth = Double.MAX_VALUE
        info.alignment = Pos.CENTER
        info.maxWidth = Double.MAX_VALUE
        progress.maxWidth = Double.MAX_VALUE
        separator.maxWidth = Double.MAX_VALUE

        progress.isVisible = false

        gridpaneColumnConstraints.let {
            setRowIndex(ordreLabel, 0)
            setColumnIndex(ordreLabel, 0)

            setRowIndex(typeLabel, 0)
            setColumnIndex(typeLabel, 1)

            setRowIndex(info, 0)
            setColumnIndex(info, 2)

            setRowIndex(progress, 0)
            setColumnIndex(progress, 3)

            setRowIndex(separator, 1)
            setColumnIndex(separator, 0)
            setColumnSpan(separator, 4)
        }

        children.add(ordreLabel)
        children.add(typeLabel)
        children.add(info)
        children.add(progress)
        children.add(separator)

        //instruction.content + " " + type?.let { getUnitFromType(it) }
    }

    private fun  getUnitFromType(type: InstructionsModel.Type): String {
        when (type) {
            InstructionsModel.Type.INTALL_APK -> { return "apks" }
            InstructionsModel.Type.PUSH_FILE -> { return "files"}
            InstructionsModel.Type.DELETE_FILE -> { return "files" }
        }

        return ""
    }

    fun setProgress(boolean: Boolean) {
        progress.isVisible = boolean
    }
}