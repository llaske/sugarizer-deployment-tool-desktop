package com.sugarizer.domain.shared.database

import com.sugarizer.domain.model.DeviceDBModel
import com.sugarizer.domain.model.MusicModel
import com.sugarizer.main.Main
import io.reactivex.Observable
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import java.sql.ResultSet
import java.sql.SQLException
import javax.inject.Inject

class DeviceDAO {
    @Inject lateinit var dbUtils: DBUtil

    enum class State {
        ADDED,
        CHANGED,
        REMOVED
    }

    var bus: PublishSubject<Pair<State, DeviceDBModel>> = PublishSubject.create()

    init {
        Main.appComponent.inject(this)
    }

    @Throws(SQLException::class, ClassNotFoundException::class)
    fun searchDevice(deviceID: DeviceDBModel): Observable<DeviceDBModel> {
        return Observable.create { subscriber ->
            val selectStmt = "SELECT * FROM " + DeviceDBModel.NAME_TABLE + " WHERE " + DeviceDBModel.DEVICE_ID + "= ?"
            try {
                val rsEmp = dbUtils.dbExecuteQuery(selectStmt, arrayListOf(Pair(DBUtil.Type.INTEGER, deviceID)))
                val music = getDeviceFromResultSet(rsEmp)

                music?.let {
                    subscriber.onNext(it)
                }
                subscriber.onComplete()
            } catch (e: SQLException) {
                println("While searching an employee with $deviceID id, an error occurred: $e")
                throw e
            }
        }
    }

    @Throws(SQLException::class, ClassNotFoundException::class)
    fun searchDevice(deviceUDID: String): Observable<DeviceDBModel> {
        return Observable.create { subscriber ->
            val selectStmt = "SELECT * FROM " + DeviceDBModel.NAME_TABLE + " WHERE " + DeviceDBModel.DEVICE_UDID + "= ?"
            try {
                val rsEmp = dbUtils.dbExecuteQuery(selectStmt, arrayListOf(Pair(DBUtil.Type.STRING, deviceUDID)))
                val music = getDeviceFromResultSet(rsEmp)

                music?.let {
                    subscriber.onNext(it)
                }
                subscriber.onComplete()
            } catch (e: SQLException) {
                println("While searching an employee with $deviceUDID id, an error occurred: $e")
                throw e
            }
        }
    }

    @Throws(SQLException::class, ClassNotFoundException::class)
    fun searchDevice(): ObservableList<DeviceDBModel> {
        val selectStmt = "SELECT * FROM " + DeviceDBModel.NAME_TABLE + ";"

        try {
            val rsEmps = dbUtils.dbExecuteQuery(selectStmt, arrayListOf())
            val deviceList = getDeviceList(rsEmps)

            return deviceList
        } catch (e: SQLException) {
            println("SQL select operation has been failed: " + e)
            throw e
        }

    }

    @Throws(SQLException::class, ClassNotFoundException::class)
    fun updateDevice(music: MusicModel) {
        val updateStmt = " UPDATE " + DeviceDBModel.NAME_TABLE +
                " SET " +
                MusicModel.MUSIC_NAME + "= ?," +
                MusicModel.MUSIC_DURATION + "= ?," +
                MusicModel.MUSIC_PATH + "= ?" +
                " WHERE EMPLOYEE_ID = ?; COMMIT; END;"

        try {
            dbUtils.dbExecuteUpdate(updateStmt, arrayListOf(Pair(DBUtil.Type.STRING, music.musicName),
                    Pair(DBUtil.Type.INTEGER, music.musicDuration),
                    Pair(DBUtil.Type.STRING, music.musicPath),
                    Pair(DBUtil.Type.INTEGER, music.musicID)))
        } catch (e: SQLException) {
            print("Error occurred while UPDATE Operation: " + e)
            throw e
        }
    }

    @Throws(SQLException::class, ClassNotFoundException::class)
    fun deleteDevice(id: Int) {
        val updateStmt = "BEGIN\n" +
                " DELETE FROM " + DeviceDBModel.NAME_TABLE +
                " WHERE " + MusicModel.MUSIC_ID + "= ?; COMMIT; END;"

        try {
            dbUtils.dbExecuteUpdate(updateStmt, arrayListOf(Pair(DBUtil.Type.INTEGER, id)))
        } catch (e: SQLException) {
            print("Error occurred while DELETE Operation: " + e)
            throw e
        }

    }

    @Throws(SQLException::class, ClassNotFoundException::class)
    fun insertDevice(device: DeviceDBModel) { searchDevice(device)
            .subscribeOn(Schedulers.computation())
            .observeOn(JavaFxScheduler.platform())
            .subscribe { device ->
                Observable.create<Any> {
                    try {
                        val updateStmt = "INSERT INTO " + DeviceDBModel.NAME_TABLE + " (" +
                                DeviceDBModel.DEVICE_NAME + "," +
                                DeviceDBModel.DEVICE_MODEL + "," +
                                DeviceDBModel.DEVICE_SERIAL + "," +
                                DeviceDBModel.DEVICE_VERSION_NAME + ") VALUES (?, ?, ?, ?);"
                        println(updateStmt)
                        dbUtils.dbExecuteUpdate(updateStmt, arrayListOf(Pair(DBUtil.Type.STRING, device.deviceName), Pair(DBUtil.Type.STRING, device.deviceSerial), Pair(DBUtil.Type.STRING, device.deviceModel), Pair(DBUtil.Type.STRING, device.deviceVersionName)))
                    } catch (e: SQLException) {
                        print("Error occurred while INSERT Operation: " + e)
                    }

                    it.onComplete()
                }
                        .subscribeOn(Schedulers.computation())
                        .observeOn(JavaFxScheduler.platform())
                        .doOnComplete { bus.onNext(Pair(State.ADDED, device)) }
                        .subscribe()
            }
    }

    @Throws(SQLException::class)
    private fun getDeviceFromResultSet(rs: ResultSet): DeviceDBModel? {
        var device: DeviceDBModel? = null

        if (rs.next()) {
            device = DeviceDBModel(
                    rs.getString(DeviceDBModel.DEVICE_NAME),
                    rs.getString(DeviceDBModel.DEVICE_SERIAL),
                    rs.getString(DeviceDBModel.DEVICE_MODEL),
                    rs.getString(DeviceDBModel.DEVICE_VERSION_NAME))

            device.deviceID = rs.getInt(DeviceDBModel.DEVICE_ID)
        }

        return device
    }

    @Throws(SQLException::class, ClassNotFoundException::class)
    private fun getDeviceList(rs: ResultSet): ObservableList<DeviceDBModel> {
        val repList = FXCollections.observableArrayList<DeviceDBModel>()

        while (rs.next()) {
            val device = DeviceDBModel(
                    rs.getString(DeviceDBModel.DEVICE_NAME),
                    rs.getString(DeviceDBModel.DEVICE_SERIAL),
                    rs.getString(DeviceDBModel.DEVICE_MODEL),
                    rs.getString(DeviceDBModel.DEVICE_VERSION_NAME))

            device.deviceID = rs.getInt(DeviceDBModel.DEVICE_ID)

            repList.add(device)
        }
        return repList
    }

    fun toObservable() : Observable<Pair<State, DeviceDBModel>> {
        return bus
    }

    fun hasObservers() : Boolean {
        return bus.hasObservers()
    }
}