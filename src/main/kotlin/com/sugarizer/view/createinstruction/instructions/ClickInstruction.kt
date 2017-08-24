package com.sugarizer.view.createinstruction.instructions

import com.google.gson.Gson
import com.sugarizer.model.*
import com.sugarizer.view.createinstruction.CreateInstructionView
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox
import java.io.IOException

class ClickInstruction(val type: CreateInstructionView.Type) : Dialog<String>() {
    @FXML lateinit var cancel: Button
    @FXML lateinit var ok: Button
    @FXML lateinit var x: TextField
    @FXML lateinit var y: TextField
    @FXML lateinit var x2: TextField
    @FXML lateinit var y2: TextField
    @FXML lateinit var duration: TextField
    @FXML lateinit var text: TextField
    @FXML lateinit var boxOne: VBox
    @FXML lateinit var boxTwo: VBox
    @FXML lateinit var boxThree: VBox
    @FXML lateinit var packageName: TextField

    init {
        var resource = String()

        when (type) {
            CreateInstructionView.Type.CLICK -> { resource = "/layout/instruction/instruction-click.fxml" }
            CreateInstructionView.Type.LONGCLICK -> { resource = "/layout/instruction/instruction-long-click.fxml" }
            CreateInstructionView.Type.KEY -> { resource = "/layout/instruction/instruction-key.fxml" }
            CreateInstructionView.Type.TEXT -> { resource = "/layout/instruction/instruction-text.fxml" }
            CreateInstructionView.Type.SWIPE -> { resource = "/layout/instruction/instruction-swipe.fxml" }
            CreateInstructionView.Type.SLEEP -> resource = "/layout/instruction/instruction-sleep.fxml"
            CreateInstructionView.Type.OPENAPP -> resource = "/layout/instruction/instruction-openapp.fxml"
            CreateInstructionView.Type.DELETE -> resource = "/layout/instruction/instruction-delete.fxml"
        }

        val loader = FXMLLoader(javaClass.getResource(resource))
        val view = ClickInstructionView()

        loader.setRoot(view)
        loader.setController(this)

        dialogPane.scene.window.setOnCloseRequest {
            result = "RESULT_CANCEL"
            close()
        }
        var tmpTitle = type.toString().toLowerCase()
        tmpTitle = tmpTitle.replaceRange(0, 1, tmpTitle[0].toUpperCase().toString())
        title = tmpTitle

        println(type)

        result = type.toString()

        try {
            loader.load<GridPane>()
            dialogPane.content = view

            ok.onAction = onClickOk()
            cancel.onAction = onClickCancel()

            when (type) {
                CreateInstructionView.Type.KEY -> { (boxOne.children[0] as RadioButton).isSelected = true }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun onClickOk(): EventHandler<ActionEvent> {
        return EventHandler {
            result = getStringFromModel()
            close()
        }
    }

    fun onClickCancel(): EventHandler<ActionEvent> {
        return EventHandler {
            result = "RESULT_CANCEL"
            close()
        }
    }

    fun getSelectedButton(): RadioButton {
        return checkBox(boxOne) ?: checkBox(boxTwo) ?: checkBox(boxThree) ?: return RadioButton("0")
    }

    fun checkBox(box: VBox): RadioButton? {
        box.children
                .filter { (it as RadioButton).isSelected }
                .forEach { return (it as RadioButton) }

        return null
    }

    fun getStringFromModel(): String {
        when (type) {
            CreateInstructionView.Type.CLICK -> { return Gson().toJson(ClickModel(x.text.toInt(), y.text.toInt())) }
            CreateInstructionView.Type.LONGCLICK -> { return Gson().toJson(LongClickModel(x.text.toInt(), y.text.toInt(), duration.text.toInt())) }
            CreateInstructionView.Type.SWIPE -> { return Gson().toJson(SwipeModel(x.text.toInt(), y.text.toInt(), x2.text.toInt(), y2.text.toInt(), duration.text.toInt())) }
            CreateInstructionView.Type.KEY -> { return Gson().toJson(KeyModel(getSelectedButton().id.toInt(), getSelectedButton().text)) }
            CreateInstructionView.Type.TEXT -> { return  Gson().toJson(TextModel(text.text)) }
            CreateInstructionView.Type.SLEEP -> return Gson().toJson(SleepModel(duration.text.toLong()))
            CreateInstructionView.Type.OPENAPP -> return Gson().toJson(OpenAppModel(packageName.text))
            CreateInstructionView.Type.DELETE -> return Gson().toJson(DeleteFileModel(text.text))
        }

        return ""
    }
}

class ClickInstructionView: GridPane() {

}