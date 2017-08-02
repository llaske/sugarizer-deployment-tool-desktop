package com.sugarizer.view.device.cellfactory

import com.sugarizer.listitem.ListItemDevice
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