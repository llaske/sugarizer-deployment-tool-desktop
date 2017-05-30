package views

import io.reactivex.schedulers.Schedulers
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.GridPane
import javafx.stage.Stage
import model.Device
import model.DeviceEvent
import tornadofx.*
import utils.JADB
import javax.inject.Inject

class DevicesView : View() {
    override val root: GridPane by fxml("/layout/view-devices.fxml")

    @Inject val jadb = JADB()

    val columnName: TableColumn<Device, String> by fxid(propName = "columnName")
    val columnStatus: TableColumn<Device, String> by fxid(propName = "columnStatus")
    val columnAction: TableColumn<Device, String> by fxid(propName = "columnAction")

    val tableDevice: TableView<Device> by fxid(propName = "deviceTable")

    init {
        println("Device View initialize")

        println("Number Devices connected: " + jadb.numberDevice())

        columnName.cellValueFactory = PropertyValueFactory<Device, String>("name")
        columnStatus.cellValueFactory = PropertyValueFactory<Device, String>("status")
        columnAction.cellValueFactory = PropertyValueFactory<Device, String>("action")

        with(tableDevice) {
            jadb.watcher()
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(Schedulers.io())
                    .subscribe { deviceEvent ->
                        run {
                            when (deviceEvent.status) {
                                DeviceEvent.Status.ADDED -> {
                                    println("Added")
                                    items.add(deviceEvent.device)
                                }

                                DeviceEvent.Status.REMOVED -> {
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

            jadb.watchChanged()
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(Schedulers.io())
                    .subscribe { deviceEvent ->
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
                var device: Device = selectedItem!!

                println("Double click on : " + device.name.get())

//                device.push("testfile.rnd", "/sdcard/testfile.rnd")
//                        .subscribeOn(Schedulers.newThread())
//                        .doOnComplete { println("File pushed") }
//                        .subscribe()

                var details: View = DeviceDetailsView(device)
                var stage: Stage = Stage()
                stage.isFocused = true
                stage.initOwner(modalStage)
                details.openWindow()

                //println(convertStreamToString(device.executeShell("rm sdcard/testfile.rnd")))

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