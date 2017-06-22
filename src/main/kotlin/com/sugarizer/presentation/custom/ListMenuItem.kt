package com.sugarizer.presentation.custom

import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.Separator
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import tornadofx.gridpaneColumnConstraints

class ListMenuItem : GridPane() {
    var name: SimpleStringProperty = SimpleStringProperty("")
    var number: Int = 0

    var image = Image("/image/arrow.png")
    val menuSelect = ImageView(image)
    val titleLabel = Label(name.get())
    val numberLabel = Label(number.toString())
    val progress = ProgressIndicator()

    init {
        titleLabel.textProperty().bind(name)

        maxHeight = 25.0
        maxWidth = Double.MAX_VALUE

        val columnOne = ColumnConstraints()
        val columnTwo = ColumnConstraints()
        val columnThree = ColumnConstraints()
        val columnFour = ColumnConstraints()

        val rowOne = RowConstraints()
        val rowTwo = RowConstraints()

        columnOne.percentWidth = 10.0
        columnTwo.percentWidth = 55.0
        columnThree.percentWidth = 15.0
        columnFour.percentWidth = 15.0

        rowOne.percentHeight = 90.0
        rowTwo.percentHeight = 10.0

        val separator = Separator(Orientation.HORIZONTAL)
        val stack = VBox()

        stack.maxHeight = Double.MAX_VALUE
        stack.maxWidth = Double.MAX_VALUE
        stack.children.add(progress)
        stack.alignment = Pos.CENTER

        menuSelect.fitWidth = 25.0
        menuSelect.fitHeight = 25.0
        menuSelect.isVisible = false
        titleLabel.padding = Insets(0.0, 0.0, 0.0, 5.0)
        numberLabel.alignment = Pos.CENTER
        numberLabel.maxWidth = Double.MAX_VALUE
        progress.isVisible = false

        columnConstraints.add(columnOne)
        columnConstraints.add(columnTwo)
        columnConstraints.add(columnThree)
        columnConstraints.add(columnFour)

        rowConstraints.add(rowOne)
        rowConstraints.add(rowTwo)

        gridpaneColumnConstraints.let {
            setRowIndex(menuSelect, 0)
            setRowIndex(titleLabel, 0)
            setRowIndex(numberLabel, 0)
            setRowIndex(stack, 0)
            setRowIndex(separator, 1)

            setColumnIndex(menuSelect, 0)
            setColumnIndex(titleLabel, 1)
            setColumnIndex(numberLabel, 2)
            setColumnIndex(stack, 3)
            setColumnIndex(separator, 0)

            setColumnSpan(separator, 7)
        }

        children.add(titleLabel)
        children.add(numberLabel)
        children.add(stack)
        children.add(separator)
        children.add(menuSelect)
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

    fun setSelected(boolean: Boolean) {
        menuSelect.isVisible = boolean
    }
}