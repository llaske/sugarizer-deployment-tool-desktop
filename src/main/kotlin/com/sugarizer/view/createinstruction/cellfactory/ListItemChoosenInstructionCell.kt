package com.sugarizer.view.device.cellfactory

import com.sugarizer.listitem.ListItemChoosenInstruction
import org.controlsfx.control.GridCell

class ListItemChoosenInstructionCell : GridCell<ListItemChoosenInstruction>() {
    override fun updateItem(item: ListItemChoosenInstruction?, empty: Boolean) {
        super.updateItem(item, empty)
        if (empty || item == null) {
            graphic = null
        } else {
            graphic = item
        }
    }
}