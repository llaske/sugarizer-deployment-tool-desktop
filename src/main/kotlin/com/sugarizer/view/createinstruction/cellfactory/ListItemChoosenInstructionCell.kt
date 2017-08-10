package com.sugarizer.view.device.cellfactory

import com.sugarizer.listitem.ListItemChoosenInstruction
import com.sugarizer.listitem.ListItemInstruction
import com.sugarizer.listitem.ListItemSpk
import com.sugarizer.listitem.ListItemSpkInstruction
import javafx.geometry.Insets
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