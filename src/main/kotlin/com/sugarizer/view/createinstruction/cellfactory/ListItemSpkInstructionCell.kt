package com.sugarizer.view.device.cellfactory

import com.sugarizer.listitem.ListItemSpk
import com.sugarizer.listitem.ListItemSpkInstruction
import javafx.geometry.Insets
import org.controlsfx.control.GridCell

class ListItemSpkInstructionCell : GridCell<ListItemSpkInstruction>() {
    override fun updateItem(item: ListItemSpkInstruction?, empty: Boolean) {
        super.updateItem(item, empty)
        if (empty || item == null) {
            graphic = null
        } else {
            graphic = item
        }
    }
}