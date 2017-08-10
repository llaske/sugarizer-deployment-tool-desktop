package com.sugarizer.view.device.cellfactory

import com.sugarizer.listitem.ListItemChoosenInstruction
import com.sugarizer.listitem.ListItemInstruction
import com.sugarizer.listitem.ListItemSpk
import com.sugarizer.listitem.ListItemSpkInstruction
import javafx.util.Callback
import org.controlsfx.control.GridCell
import org.controlsfx.control.GridView

class ListItemChoosenInstructionCellFactory : Callback<GridView<ListItemChoosenInstruction>, GridCell<ListItemChoosenInstruction>> {
    override fun call(param: GridView<ListItemChoosenInstruction>?): GridCell<ListItemChoosenInstruction> {
        return ListItemChoosenInstructionCell()
    }
}