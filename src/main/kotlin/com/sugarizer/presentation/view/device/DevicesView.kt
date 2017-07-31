package com.sugarizer.presentation.view.device

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXDialog
import com.jfoenix.controls.JFXDrawer
import com.jfoenix.controls.JFXListView
import com.sugarizer.domain.model.DeviceEventModel
import com.sugarizer.domain.shared.JADB
import com.sugarizer.domain.shared.RxBus
import com.sugarizer.domain.shared.SpkManager
import com.sugarizer.domain.shared.StringUtils
import com.sugarizer.main.Main
import com.sugarizer.presentation.custom.ListItemDevice
import com.sugarizer.presentation.custom.ListItemSpk
import com.sugarizer.presentation.view.device.cellfactory.ListItemDeviceCellFactory
import com.sugarizer.presentation.view.device.cellfactory.ListItemSpkCellFactory
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Label
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import org.controlsfx.control.GridView
import view.main.MainView
import java.io.File
import java.net.URL
import java.util.*
import javax.inject.Inject

class DevicesView : Initializable, DeviceContract.View {
    @Inject lateinit var jadb: JADB
    @Inject lateinit var bus: RxBus
    @Inject lateinit var stringUtils: StringUtils
    @Inject lateinit var spkManager: SpkManager

    @FXML lateinit var devices: GridView<ListItemDevice>
    @FXML lateinit var root: StackPane
    @FXML lateinit var drawer: JFXDrawer
    @FXML lateinit var dropZone: Label
    @FXML lateinit var instructionDialog: JFXDialog
    @FXML lateinit var listInstruction: JFXListView<String>
    @FXML lateinit var launchInstruction: JFXButton
    @FXML lateinit var cancelInstruction: JFXButton
    @FXML lateinit var inWork: StackPane

    @FXML lateinit var spk: GridView<ListItemSpk>

    val presenter: DevicePresenter

    init {
        Main.appComponent.inject(this)

        presenter = DevicePresenter(this, jadb, bus, stringUtils)
    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        devices.cellHeight = 150.0
        devices.cellWidth = 150.0
        devices.cellFactory =  ListItemDeviceCellFactory()

        spk.cellWidthProperty().bind(spk.widthProperty())
        spk.cellHeight = 100.0
        spk.horizontalCellSpacing = 20.0
        spk.cellFactory = ListItemSpkCellFactory()

        dropZone.onDragOver = presenter.onDragOver()
        dropZone.onDragDropped = presenter.onDropped()
        launchInstruction.onAction = presenter.onLaunchInstruction()
        cancelInstruction.onAction = presenter.onCancelInstruction()
        instructionDialog.onDialogClosed = presenter.onDialogClosed()

        spkManager.toObservable()
                .observeOn(JavaFxScheduler.platform())
                .subscribe { file ->
                    when (file.first) {
                        SpkManager.State.CREATE -> onSpkAdded(file.second)
                        SpkManager.State.MODIFY -> println("Modified")
                        SpkManager.State.DELETE -> onSpkRemoved(file.second)
                    }
                }

        spkManager.startWatching()
    }

    override fun onDeviceAdded(deviceEventModel: DeviceEventModel) {
        Platform.runLater {
            devices.items.add(ListItemDevice(deviceEventModel.device).onItemAdded())
        }
    }

    override fun onDeviceChanged(deviceEventModel: DeviceEventModel) {
//        tableDevice.items.filter {
//            it.jadbDevice.serial.equals(deviceEventModel.device.jadbDevice.serial) }
//                .forEach {
//                    it.setAction(deviceEventModel.device.action.get())
//                    it.setName(deviceEventModel.device.name.get())
//                    it.setStatus(deviceEventModel.device.status.get())
//                    it.setDevice(deviceEventModel.device.jadbDevice)
//                }
    }

    override fun onDeviceRemoved(deviceEventModel: DeviceEventModel) {
        devices.items.filter { it.device.serial.get().equals(deviceEventModel.device.jadbDevice.serial) }
                .forEach { device -> run {
                        Platform.runLater {
                            var tmp = device.onItemRemoved()

                            tmp.setOnFinished {
                                devices.items.remove(device)
                                devices.items.size
                            }

                            tmp.play()
                        }
                    }
                }
    }

    override fun showDialog(list: List<File>) {
        println("Size: " + list.size)
        list.forEach { listInstruction.items.add(it.nameWithoutExtension) }

        instructionDialog.show(MainView.root)
    }

    override fun closeDialog() {
        instructionDialog.close()
    }

    override fun setInWork(boolean: Boolean) {
        inWork.isVisible = boolean
    }

    override fun getDevices(): List<ListItemDevice> {
        return devices.items
    }

    override fun get(): Stage {
        return Stage()
    }

    override fun getParent(): StackPane {
        return root
    }

    override fun closeDrawer(){
        drawer.close()
    }

    fun onSpkAdded(file: File) {
        val contains = spk.items.any { it.file.equals(file) }

        if (!contains) {
            spk.items.add(ListItemSpk(file).onItemAdded())
        }
    }

    fun onSpkRemoved(file: File) {
        println("Delete")

        var tmp: ListItemSpk? = null

        for (item in spk.items) {
            println("try find")
            if (item.file.nameWithoutExtension.equals(file.nameWithoutExtension)) {
                println("Finded ?")
                tmp = item
            }
        }

        tmp?.let {
            var fade = it.onItemRemoved()
            fade.setOnFinished { Platform.runLater { spk.items.remove(tmp) } }
            fade.play()
        }
    }
}