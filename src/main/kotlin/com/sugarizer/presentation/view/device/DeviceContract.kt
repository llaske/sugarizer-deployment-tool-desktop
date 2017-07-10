package com.sugarizer.presentation.view.device

import com.sugarizer.domain.model.DeviceEventModel
import com.sugarizer.domain.model.DeviceModel
import javafx.event.ActionEvent
import javafx.event.Event
import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import javax.swing.text.TableView

interface DeviceContract {
    interface View {
        fun test()

        fun onDeviceChanged(deviceEventModel: DeviceEventModel)

        fun onDeviceAdded(deviceEventModel: DeviceEventModel)

        fun onDeviceRemoved(deviceEventModel: DeviceEventModel)

        fun getParent(): StackPane

        fun get(): Stage
    }

    interface Presenter {
        fun start()

        fun onTableRowDoubleClick(selectedItem: DeviceModel)

        fun onPingClick(deviceModel: DeviceModel): EventHandler<ActionEvent>
    }
}