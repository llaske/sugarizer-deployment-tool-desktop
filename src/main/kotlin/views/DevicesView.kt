package views

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import javafx.scene.control.TableColumn
import javafx.scene.control.TableRow
import javafx.scene.control.TableView
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.GridPane
import javafx.util.Callback
import model.Device
import se.vidstige.jadb.JadbDevice
import se.vidstige.jadb.RemoteFile
import tornadofx.View
import tornadofx.column
import tornadofx.onDoubleClick
import tornadofx.selectedItem
import utils.JADB
import java.io.File
import java.util.*
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
            items = jadb.getDevices()

            onDoubleClick {
                var device: Device = selectedItem!!

                println("Double click on : " + device.name.get())

                device.push("testfile.rnd", "/sdcard/testfile.rnd")
                        .subscribeOn(Schedulers.newThread())
                        .doOnComplete { println("File pushed") }
                        .subscribe()

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