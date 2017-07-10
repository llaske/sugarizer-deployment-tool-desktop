package com.sugarizer.presentation.view.createinstruction.instructions

import com.google.gson.Gson
import com.sugarizer.domain.model.*
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Button
import javafx.scene.control.Dialog
import javafx.scene.control.RadioButton
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox
import java.io.IOException
import kotlin.coroutines.experimental.ContinuationInterceptor

class ClickInstruction(val type: Type) : Dialog<String>() {
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

    enum class Type {
        CLICK,
        LONG_CLICK,
        KEY,
        TEXT,
        SWIPE,
        SLEEP
    }

    init {
        var resource = String()

        when (type) {
            Type.CLICK -> { resource = "/layout/instruction/instruction-click.fxml" }
            Type.LONG_CLICK -> { resource = "/layout/instruction/instruction-long-click.fxml" }
            Type.KEY -> { resource = "/layout/instruction/instruction-key.fxml" }
            Type.TEXT -> { resource = "/layout/instruction/instruction-text.fxml" }
            Type.SWIPE -> { resource = "/layout/instruction/instruction-swipe.fxml" }
            Type.SLEEP -> resource = "/layout/instruction/instruction-sleep.fxml"
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
                Type.KEY -> { (boxOne.children[0] as RadioButton).isSelected = true }
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

    fun getSelectedButton(): Int {
        return checkBox(boxOne) ?: checkBox(boxTwo) ?: checkBox(boxThree) ?: return 0
    }

    fun checkBox(box: VBox): Int? {
        box.children
                .filter { (it as RadioButton).isSelected }
                .forEach { return it.id.toInt() }

        return null
    }

    fun getStringFromModel(): String {
        when (type) {
            Type.CLICK -> { return Gson().toJson(ClickModel(x.text.toInt(), y.text.toInt())) }
            Type.LONG_CLICK -> { return Gson().toJson(LongClickModel(x.text.toInt(), y.text.toInt(), duration.text.toInt())) }
            Type.SWIPE -> { return Gson().toJson(SwipeModel(x.text.toInt(), y.text.toInt(), x2.text.toInt(), y2.text.toInt(), duration.text.toInt())) }
            Type.KEY -> { return Gson().toJson(KeyModel(getSelectedButton())) }
            Type.TEXT -> { return  Gson().toJson(TextModel(text.text)) }
            Type.SLEEP -> return Gson().toJson(SleepModel(duration.text.toLong()))
        }

        return ""
    }
}

class ClickInstructionView: GridPane() {

}