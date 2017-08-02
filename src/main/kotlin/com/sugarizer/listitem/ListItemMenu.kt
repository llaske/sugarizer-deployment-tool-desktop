package com.sugarizer.listitem

import com.jfoenix.controls.JFXRippler
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.beans.property.ObjectProperty
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.layout.Pane
import java.io.IOException

class ListItemMenu : JFXRippler() {
    @FXML lateinit var background: Pane
    @FXML lateinit var icon: FontAwesomeIconView

    init {
        val loader = FXMLLoader(javaClass.getResource("/layout/list-item/list-item-menu.fxml"))

        loader.setRoot(this)
        loader.setController(this)

        try {
            loader.load<JFXRippler>()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun getGlyph(): String {
        return icon.glyphName
    }

    fun setGlyph(glyph: String){
        try {
            icon.glyphName = glyph
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun propertyGlyph(): ObjectProperty<String> {
        return icon.glyphNameProperty()
    }

    fun setSelected(boolean: Boolean){
        if (boolean) {
            background.style = "-fx-background-color: #03A9F4;"
            icon.glyphStyle = "-fx-fill: #FFFFFF;"
        } else {
            background.style = "-fx-background-color: #FFFFFF;"
            icon.glyphStyle = "-fx-fill: #000000;"
        }
    }
}