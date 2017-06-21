package com.sugarizer.presentation.custom

import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.Separator
import javafx.scene.layout.*
import tornadofx.gridpaneColumnConstraints

class ListMenuItem : GridPane() {
    var name: SimpleStringProperty = SimpleStringProperty("")
    var number: Int = 0

    val titleLabel = Label(name.get())
    val numberLabel = Label(number.toString())
    val progress = ProgressIndicator()

    init {
        titleLabel.textProperty().bind(name)

        maxWidth = Double.MAX_VALUE
        maxHeight = 30.0

        val columnOne = ColumnConstraints()
        val columnTwo = ColumnConstraints()
        val columnThree = ColumnConstraints()

        val rowOne = RowConstraints()
        val rowTwo = RowConstraints()

        columnOne.percentWidth = 70.0
        columnTwo.percentWidth = 15.0
        columnThree.percentWidth = 15.0

        rowOne.percentHeight = 90.0
        rowTwo.percentHeight = 10.0

        val separator = Separator(Orientation.HORIZONTAL)
        val stack = VBox()

        stack.maxHeight = Double.MAX_VALUE
        stack.maxWidth = Double.MAX_VALUE
        stack.children.add(progress)
        stack.alignment = Pos.CENTER

        titleLabel.padding = Insets(0.0, 0.0, 0.0, 5.0)
        progress.isVisible = false

        columnConstraints.add(columnOne)
        columnConstraints.add(columnTwo)
        columnConstraints.add(columnThree)

        rowConstraints.add(rowOne)
        rowConstraints.add(rowTwo)

        gridpaneColumnConstraints.let {
            setRowIndex(titleLabel, 0)
            setRowIndex(numberLabel, 0)
            setRowIndex(stack, 0)
            setRowIndex(separator, 1)

            setColumnIndex(titleLabel, 0)
            setColumnIndex(numberLabel, 1)
            setColumnIndex(stack, 2)
            setColumnIndex(separator, 0)

            setColumnSpan(separator, 3)
        }

        children.add(titleLabel)
        children.add(numberLabel)
        children.add(stack)
        children.add(separator)
    }

    fun setName(name: String){
        this.name.set(name)
    }

    fun getName(): String {
        return this.name.get()
    }

    fun nameProperty(): StringProperty {
        return this.name
    }

    fun setProgress(boolean: Boolean) {
        progress.isVisible = boolean
    }
}