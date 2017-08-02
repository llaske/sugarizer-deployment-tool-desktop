package com.sugarizer.presentation.view.device.cellfactory

import com.sugarizer.presentation.custom.ListItemDevice
import org.controlsfx.control.GridCell

class ListItemDeviceCell : GridCell<ListItemDevice>() {
    override fun updateItem(item: ListItemDevice?, empty: Boolean) {
        super.updateItem(item, empty)
        if (empty || item == null) {
            graphic = null
        } else {
            graphic = item
        }
    }
}