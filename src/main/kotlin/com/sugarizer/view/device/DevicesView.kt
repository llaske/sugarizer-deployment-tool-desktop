package com.sugarizer.view.device

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXDialog
import com.jfoenix.controls.JFXDrawer
import com.jfoenix.controls.JFXListView
import com.sugarizer.model.DeviceEventModel
import com.sugarizer.Main
import com.sugarizer.listitem.ListItemDevice
import com.sugarizer.listitem.ListItemSpk
import com.sugarizer.model.DeviceModel
import com.sugarizer.model.Instruction
import com.sugarizer.model.InstructionsModel
import com.sugarizer.utils.shared.*
import com.sugarizer.view.device.cellfactory.ListItemDeviceCellFactory
import com.sugarizer.view.device.cellfactory.ListItemSpkCellFactory
import com.sugarizer.view.devicedetails.view.devicedetails.DeviceDetailsPresenter
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import org.controlsfx.control.GridView
import tornadofx.onDoubleClick
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
    @Inject lateinit var animUtils: AnimationUtils

    @FXML lateinit var devices: GridView<ListItemDevice>
    @FXML lateinit var root: StackPane
    @FXML lateinit var drawer: JFXDrawer
    @FXML lateinit var dropZone: Label

    @FXML lateinit var apkDialog: JFXDialog
    @FXML lateinit var apkLaunch: JFXButton
    @FXML lateinit var apkCancel: JFXButton
    @FXML lateinit var apkList: JFXListView<String>

    @FXML lateinit var instructionDialog: JFXDialog
    @FXML lateinit var instructionList: JFXListView<String>
    @FXML lateinit var instructionLaunch: JFXButton
    @FXML lateinit var instructionCancel: JFXButton

    @FXML lateinit var flashProgressLayout: Node
    @FXML lateinit var flashProgressLabel: Label

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

        apkLaunch.onAction = presenter.onLaunchApk()
        apkCancel.onAction = presenter.onCancelApk()
        apkDialog.onDialogClosed = presenter.onApkDialogClosed()

        instructionLaunch.onAction = presenter.onLaunchInstruction()
        instructionCancel.onAction = presenter.onCancelInstruction()
        instructionDialog.onDialogClosed = presenter.onInstructionDialogClosed()

        spkManager.toObservable()
                .observeOn(JavaFxScheduler.platform())
                .subscribe { file ->
                    when (file.first) {
                        SpkManager.State.CREATE -> onSpkAdded(file.second)
                        SpkManager.State.MODIFY -> println("Modified")
                        SpkManager.State.DELETE -> onSpkRemoved(file.second)
                    }
                }
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

    override fun onDeviceUnauthorized(deviceEvent: DeviceEventModel) {
        devices.items.filter { it.device.serial.get().equals(deviceEvent.device.jadbDevice.serial) }
                .forEach { device -> run {
                    Platform.runLater {
                        device.changeState(ListItemDevice.State.UNAUTHORIZED)
                    }
                }
                }
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

    override fun <T> showDialog(list: List<T>, type: DeviceContract.Dialog) {
        when (type) {
            DeviceContract.Dialog.APK -> {
                var tmpList = (list as List<File>)
                tmpList.forEach { apkList.items.add(it.nameWithoutExtension) }

                apkDialog.show(MainView.root)
            }
            DeviceContract.Dialog.SPK -> {
                instructionList.items.addAll(list as List<String>)

                instructionDialog.show(MainView.root)
            }
        }
    }

    override fun closeDialog(type: DeviceContract.Dialog) {
        when (type) {
            DeviceContract.Dialog.APK -> apkDialog.close()
            DeviceContract.Dialog.SPK -> instructionDialog.close()
        }
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

    override fun showProgressFlash(message: String) {
        if (!flashProgressLayout.isVisible) {
            flashProgressLayout.isVisible = true
            animUtils.fadeIn(flashProgressLayout).play()
        }

        flashProgressLabel.text = message
    }

    override fun hideProgressFlash() {
        animUtils.fadeOut(flashProgressLayout).play()
    }

    fun onSpkAdded(file: File) {
        val contains = spk.items.any { it.file.equals(file) }

        if (!contains) {
            spk.items.add(ListItemSpk(file, presenter).onItemAdded())
        }
    }

    fun onSpkRemoved(file: File) {
        val tmp: ListItemSpk? = spk.items.lastOrNull { it.file.nameWithoutExtension.equals(file.nameWithoutExtension) }

        tmp?.let {
            var fade = it.onItemRemoved()
            fade.setOnFinished { Platform.runLater { spk.items.remove(tmp) } }
            fade.play()
        }
    }
}