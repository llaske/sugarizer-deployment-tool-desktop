package com.sugarizer.view.device.cellfactory

import com.sugarizer.listitem.ListItemSpkInstruction
import javafx.util.Callback
import org.controlsfx.control.GridCell
import org.controlsfx.control.GridView

class ListItemSpkInstructionCellFactory : Callback<GridView<ListItemSpkInstruction>, GridCell<ListItemSpkInstruction>> {
    override fun call(param: GridView<ListItemSpkInstruction>?): GridCell<ListItemSpkInstruction> {
        return ListItemSpkInstructionCell()
    }
}