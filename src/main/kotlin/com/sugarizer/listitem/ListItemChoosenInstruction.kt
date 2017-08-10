package com.sugarizer.listitem

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXListView
import com.jfoenix.controls.JFXRippler
import com.jfoenix.effects.JFXDepthManager
import com.sugarizer.Main
import com.sugarizer.model.Instruction
import com.sugarizer.utils.shared.JADB
import com.sugarizer.view.createinstruction.CreateInstructionContract
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

            nameLabel.text = instruction.type.toString()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}