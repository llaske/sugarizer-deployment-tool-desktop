package com.sugarizer.view.device

import com.jfoenix.controls.events.JFXDialogEvent
import com.sugarizer.model.DeviceEventModel
import com.sugarizer.model.DeviceModel
import com.sugarizer.listitem.ListItemDevice
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.input.DragEvent
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import java.io.File

interface DeviceContract {
    enum class Dialog {
        APK,
        SPK
    }

    interface View {
        fun onDeviceChanged(deviceEventModel: DeviceEventModel)

        fun onDeviceAdded(deviceEventModel: DeviceEventModel)

        fun onDeviceRemoved(deviceEventModel: DeviceEventModel)

        fun showProgressFlash(message: String)

        fun hideProgressFlash()

        fun <T> showDialog(list: List<T>, type: Dialog)

        fun closeDialog(type: Dialog)

        fun getDevices(): List<ListItemDevice>

        fun getParent(): StackPane

        fun get(): Stage

        fun closeDrawer()

        fun onDeviceUnauthorized(deviceEvent: DeviceEventModel)
    }

    interface Presenter {
        fun onPingClick(deviceModel: DeviceModel): EventHandler<ActionEvent>

        fun onDragOver(): EventHandler<DragEvent>

        fun onDropped(): EventHandler<DragEvent>

        fun onLaunchApk(): EventHandler<ActionEvent>

        fun onCancelApk(): EventHandler<ActionEvent>

        fun onApkDialogClosed(): EventHandler<JFXDialogEvent>

        fun onLaunchInstruction(): EventHandler<ActionEvent>

        fun onCancelInstruction(): EventHandler<ActionEvent>

        fun onInstructionDialogClosed(): EventHandler<JFXDialogEvent>

        fun onSpkFlashClicked(file: File)
    }
}