package com.sugarizer.presentation.view.device.cellfactory

import com.sugarizer.presentation.custom.ListItemDevice
import javafx.util.Callback
import org.controlsfx.control.GridCell
import org.controlsfx.control.GridView

class ListItemDeviceCellFactory : Callback<GridView<ListItemDevice>, GridCell<ListItemDevice>> {
    override fun call(param: GridView<ListItemDevice>?): GridCell<ListItemDevice> {
        return ListItemDeviceCell()
    }
}