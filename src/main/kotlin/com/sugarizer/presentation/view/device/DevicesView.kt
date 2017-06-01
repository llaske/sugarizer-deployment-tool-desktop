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
import tornadofx.*
import view.devicedetails.DeviceDetailsView
import javax.inject.Inject

class DevicesView : View() {
    override val root: GridPane by fxml("/layout/view-devices.fxml")

    @Inject lateinit var jadb: JADB
    @Inject lateinit var bus: RxBus

    val columnName: TableColumn<DeviceModel, String> by fxid(propName = "columnName")
    val columnStatus: TableColumn<DeviceModel, String> by fxid(propName = "columnStatus")
    val columnAction: TableColumn<DeviceModel, String> by fxid(propName = "columnAction")

    val tableDevice: TableView<DeviceModel> by fxid(propName = "deviceTable")

    init {
        Main.appComponent.inject(this)

        columnName.cellValueFactory = PropertyValueFactory<DeviceModel, String>("name")
        columnStatus.cellValueFactory = PropertyValueFactory<DeviceModel, String>("status")
        columnAction.cellValueFactory = PropertyValueFactory<DeviceModel, String>("action")

        with(tableDevice) {
            jadb.watcher()
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(Schedulers.io())
                    .subscribe { deviceEvent ->
                        run {
                            when (deviceEvent.status) {
                                DeviceEventModel.Status.ADDED -> {
                                    println("Added")
                                    items.add(deviceEvent.device)
                                }

                                DeviceEventModel.Status.REMOVED -> {
                                    items.filter { it.name.get().equals(deviceEvent.device.name.get()) }
                                            .forEach {
                                                println("Removed")
                                                items.remove(it)
                                            }
                                }
                                else -> { }
                            }
                        }
                    }

            bus.toObservable().subscribe { deviceEvent ->
                run {
                    println("onChanged")

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

                println("Double click on : " + device.name.get())

//                presentation.device.push("testfile.rnd", "/sdcard/testfile.rnd")
//                        .subscribeOn(Schedulers.newThread())
//                        .doOnComplete { println("File pushed") }
//                        .subscribe()
                var details: View = DeviceDetailsView(device)
                var stage: Stage = Stage()
                stage.isFocused = true
                stage.initOwner(modalStage)
                details.openWindow()

                //println(convertStreamToString(presentation.device.executeShell("rm sdcard/testfile.rnd")))

                //println("File deleted")

//                for (tmp in selectedItem?.getDevice()?.list("/sdcard/")!!) {
//                    println("isDirectory: " + tmp.isDirectory)
//
//                    try {
//                        println("Name: " + tmp.name)
//                    } catch (e: UnsupportedOperationException) {
//                        println("Problem when get name")
//                    }
//                }
            }
        }
    }

    fun convertStreamToString(`is`: java.io.InputStream): String {
        val s = java.util.Scanner(`is`).useDelimiter("\\A")
        return if (s.hasNext()) s.next() else ""
    }
}