package com.sugarizer.listitem

import com.google.gson.Gson
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXListView
import com.jfoenix.controls.JFXRippler
import com.jfoenix.effects.JFXDepthManager
import com.sugarizer.Main
import com.sugarizer.model.*
import com.sugarizer.utils.shared.JADB
import com.sugarizer.view.createinstruction.CreateInstructionContract
import com.sugarizer.view.createinstruction.CreateInstructionView
import com.sugarizer.view.devicedetails.view.devicedetails.CreateInstructionDialog
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.beans.binding.DoubleBinding
import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.Control
import javafx.scene.control.Label
import javafx.scene.layout.StackPane
import java.io.IOException
import javax.inject.Inject

class ListItemChoosenInstruction(binding: DoubleBinding, val view: CreateInstructionDialog, val instruction: Instruction) : StackPane() {
    @FXML lateinit var nameLabel: Label
    @FXML lateinit var arrowUp: JFXButton
    @FXML lateinit var arrowDown: JFXButton
    @FXML lateinit var delete: JFXButton
    @FXML lateinit var card: StackPane

    @Inject lateinit var jadb: JADB

    init {
        Main.appComponent.inject(this)

        val loader = FXMLLoader(javaClass.getResource("/layout/list-item/list-item-choosen-instruction.fxml"))

        loader.setRoot(this)
        loader.setController(this)

        try {
            loader.load<StackPane>()

            JFXDepthManager.setDepth(card, 4)
            prefWidthProperty().bind(binding)
            maxWidth = Control.USE_PREF_SIZE

            arrowUp.onAction = EventHandler { view.onArrowUp(this) }
            arrowDown.onAction = EventHandler { view.onArrowDown(this) }
            delete.onAction = EventHandler { view.onDeleteChoosenInstruction(this) }

            when (instruction.type) {
                CreateInstructionView.Type.KEY -> nameLabel.text = "Key: " + Gson().fromJson(instruction.data, KeyModel::class.java).textKey
                CreateInstructionView.Type.SLEEP -> nameLabel.text = "Sleep: " + Gson().fromJson(instruction.data, SleepModel::class.java).duration + "ms"
                CreateInstructionView.Type.CLICK -> nameLabel.text = "Click: X: " + Gson().fromJson(instruction.data, ClickModel::class.java).x + ", Y: " + Gson().fromJson(instruction.data, ClickModel::class.java).y
                CreateInstructionView.Type.LONGCLICK -> nameLabel.text = "LClick: X: " + Gson().fromJson(instruction.data, LongClickModel::class.java).x + ", Y: " + Gson().fromJson(instruction.data, LongClickModel::class.java).y
                CreateInstructionView.Type.SWIPE -> nameLabel.text = "S: (X:" + Gson().fromJson(instruction.data, SwipeModel::class.java).x1 + ",Y: " + Gson().fromJson(instruction.data, SwipeModel::class.java).y1 + ") -> (X:" + Gson().fromJson(instruction.data, SwipeModel::class.java).x2 + ",Y: " + Gson().fromJson(instruction.data, SwipeModel::class.java).y2 + ")"
                CreateInstructionView.Type.TEXT -> nameLabel.text = "Text: " + Gson().fromJson(instruction.data, TextModel::class.java).text
                CreateInstructionView.Type.APK -> nameLabel.text = Gson().fromJson(instruction.data, InstallApkModel::class.java)?.numberApk.toString() + " APK's"
                CreateInstructionView.Type.OPENAPP -> nameLabel.text = "Package: " + Gson().fromJson(instruction.data, OpenAppModel::class.java).package_name
                else -> nameLabel.text = instruction.type.toString()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}