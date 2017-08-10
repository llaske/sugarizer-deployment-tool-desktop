package com.sugarizer.listitem

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXDialog
import com.jfoenix.controls.JFXRippler
import com.jfoenix.effects.JFXDepthManager
import com.sugarizer.model.DeviceModel
import com.sugarizer.utils.shared.JADB
import com.sugarizer.Main
import com.sugarizer.view.device.type.APK
import com.sugarizer.view.devicedetails.view.devicedetails.CreateInstructionDialog
import com.sugarizer.view.devicedetails.view.devicedetails.DeviceDetailsPresenter
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import io.reactivex.Observable
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import javafx.animation.FadeTransition
import javafx.application.Platform
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.StringProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.JavaFXBuilderFactory
import javafx.scene.control.Label
import javafx.scene.layout.StackPane
import javafx.util.Duration
import tornadofx.onDoubleClick
import tornadofx.useMaxSize
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ListItemInstruction() : StackPane() {
    @FXML lateinit var nameLabel: Label
    @FXML lateinit var icon: FontAwesomeIconView
    @FXML lateinit var add: JFXButton

    @Inject lateinit var jadb: JADB

    enum class Type {
        APK,
        CLICK,
        LONGCLICK,
        SWIPE,
        TEXT,
        KEY,
        PUSH,
        DELETE,
        SLEEP
    }

    var type = SimpleObjectProperty<Type>()

    init {
        Main.appComponent.inject(this)

        val loader = FXMLLoader(javaClass.getResource("/layout/list-item/list-item-instruction.fxml"))

        loader.setRoot(this)
        loader.setController(this)

        try {
            loader.load<StackPane>()

            add.onMouseClicked = EventHandler {}
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun setAdd(item: CreateInstructionDialog){
        add.onAction = EventHandler { item.addInstruction(type.get()) }
    }

    fun getName(): String {
        return nameLabel.text
    }

    fun setName(name: String) {
        nameLabel.text = name
    }

    fun nameProperty(): StringProperty {
        return nameLabel.textProperty()
    }

    fun getIcon(): String {
        return icon.glyphName
    }

    fun setIcon(name: String) {
        icon.glyphName = name
    }

    fun iconProperty(): ObjectProperty<String> {
        return icon.glyphNameProperty()
    }

    fun getType(): Type {
        return type.get()
    }

    fun setType(typeNew: Type) {
        type.set(typeNew)
    }

    fun typeProperty(): ObjectProperty<Type> {
        return type
    }
}