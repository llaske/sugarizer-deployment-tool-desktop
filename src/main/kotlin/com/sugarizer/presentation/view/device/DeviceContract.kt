package com.sugarizer.presentation.view.device

import com.sugarizer.domain.model.DeviceEventModel
import com.sugarizer.domain.model.DeviceModel
import com.sugarizer.presentation.custom.ListItemDevice
import javafx.event.ActionEvent
import javafx.event.Event
import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.input.DragEvent
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import javax.swing.text.TableView

interface DeviceContract {
    interface View {
        fun onDeviceChanged(deviceEventModel: DeviceEventModel)

        fun onDeviceAdded(deviceEventModel: DeviceEventModel)

        fun onDeviceRemoved(deviceEventModel: DeviceEventModel)

        fun showDialog(list: List<String>)

        fun closeDialog()

        fun setInWork(boolean: Boolean)

        fun getDevices(): List<ListItemDevice>

        fun getParent(): StackPane

        fun get(): Stage

        fun closeDrawer()
    }

    interface Presenter {
        fun onPingClick(deviceModel: DeviceModel): EventHandler<ActionEvent>

        fun onDragOver(): EventHandler<DragEvent>

        fun onDropped(): EventHandler<DragEvent>

        fun onLaunchInstruction(): EventHandler<ActionEvent>

        fun onCancelInstruction(): EventHandler<ActionEvent>


    }
}