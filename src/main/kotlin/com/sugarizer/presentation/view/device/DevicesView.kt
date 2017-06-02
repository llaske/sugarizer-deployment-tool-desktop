package view.device

import com.sugarizer.main.Main
import io.reactivex.schedulers.Schedulers
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.GridPane
import javafx.stage.Stage
import com.sugarizer.domain.model.DeviceEventModel
import com.sugarizer.domain.model.DeviceModel
import com.sugarizer.domain.shared.JADB
import com.sugarizer.domain.shared.RxBus
import com.sugarizer.domain.shared.StringUtils
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableValue
import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.control.TableCell
import javafx.util.Callback
import tornadofx.*
import view.devicedetails.DeviceDetailsView
import javax.inject.Inject

class DevicesView : View() {
    override val root: GridPane by fxml("/layout/view-devices.fxml")

    @Inject lateinit var jadb: JADB
    @Inject lateinit var bus: RxBus
    @Inject lateinit var stringUtils: StringUtils

    val columnName: TableColumn<DeviceModel, String> by fxid(propName = "columnName")
    val columnStatus: TableColumn<DeviceModel, String> by fxid(propName = "columnStatus")
    val columnAction: TableColumn<DeviceModel, String> by fxid(propName = "columnAction")
    val columnPing: TableColumn<DeviceModel, String> by fxid(propName = "columnPing")

    val tableDevice: TableView<DeviceModel> by fxid(propName = "deviceTable")

    init {
        Main.appComponent.inject(this)

        columnName.cellValueFactory = PropertyValueFactory<DeviceModel, String>("name")
        columnStatus.cellValueFactory = PropertyValueFactory<DeviceModel, String>("status")
        columnAction.cellValueFactory = PropertyValueFactory<DeviceModel, String>("action")
        columnPing.cellValueFactory = PropertyValueFactory<DeviceModel, String>("ping")

        columnPing.cellFactory = Callback {
            val button = Button()

            button.maxWidth = Double.MAX_VALUE
            button.maxHeight = Double.MAX_VALUE

            object : TableCell<DeviceModel, String>() {
                override fun updateItem(item: String?, empty: Boolean) {
                    super.updateItem(item, empty)

                    if (item == null) {
                        graphic = null
                    } else {
                        button.text = "Ping"
                        button.onAction = EventHandler {

                            //println(convertStreamToString(rowItem.jadbDevice.executeShell("am startservice com.sugarizer.sugarizerdeploymenttoolapp.BroadcastService", "")))
                            //println(stringUtils.convertStreamToString(rowItem.jadbDevice.executeShell("am broadcast -a com.sugarizer.sugarizerdeploymentoolapp.Broadcast.ACTION_PING", "")))
                            rowItem.hasPackage("com.android.fmradio").subscribe { println(it) }
                            rowItem.hasPackage("com.sugarizer.sugarizerdeploymenttoolapp").subscribe { println(it) }
                            rowItem.sendLog("Test")
                            rowItem.ping()
                        }

                        graphic = button
                    }
                }
            }
        }

        with(tableDevice) {
            jadb.watcher()
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(Schedulers.io())
                    .subscribe { deviceEvent ->
                        run {
                            when (deviceEvent.status) {
                                DeviceEventModel.Status.ADDED -> { items.add(deviceEvent.device) }

                                DeviceEventModel.Status.REMOVED -> {
                                    items.filter { it.name.get().equals(deviceEvent.device.name.get()) }
                                            .forEach {
                                                items.remove(it)
                                            }
                                }
                                else -> { }
                            }
                        }
                    }

            bus.toObservable().subscribe { deviceEvent ->
                run {
                    items.filter { it.jadbDevice.serial.equals(deviceEvent.device.jadbDevice.serial) }
                            .forEach {
                                it.setAction(deviceEvent.device.action.get())
                                it.setName(deviceEvent.device.name.get())
                                it.setStatus(deviceEvent.device.status.get())
                                it.setDevice(deviceEvent.device.jadbDevice)
                            }
                }
            }

            onDoubleClick {
                var device: DeviceModel = selectedItem!!
                var details: View = DeviceDetailsView(device)
                var stage: Stage = Stage()

                stage.isFocused = true
                stage.initOwner(modalStage)
                details.openWindow()
            }
        }
    }
}