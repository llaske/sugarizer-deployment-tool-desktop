package com.sugarizer.view.device.cellfactory

import com.sugarizer.listitem.ListItemSpk
import javafx.util.Callback
import org.controlsfx.control.GridCell
import org.controlsfx.control.GridView

class ListItemSpkCellFactory : Callback<GridView<ListItemSpk>, GridCell<ListItemSpk>> {
    override fun call(param: GridView<ListItemSpk>?): GridCell<ListItemSpk> {
        return ListItemSpkCell()
    }
}