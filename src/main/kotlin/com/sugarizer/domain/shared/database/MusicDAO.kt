package com.sugarizer.domain.shared.database

import com.sugarizer.domain.model.MusicModel
import com.sugarizer.main.Main
import io.reactivex.Observable
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import java.sql.ResultSet
import java.sql.SQLException
import javax.inject.Inject
import javax.print.DocFlavor

class MusicDAO {
    @Inject lateinit var dbUtils: DBUtil

    enum class State {
        ADDED,
        CHANGED,
        REMOVED
    }

    enum class StateSearch {
        FOUND,
        NOT_FOUND
    }

    var bus: PublishSubject<Pair<State, MusicModel>> = PublishSubject.create()

    init {
        Main.appComponent.inject(this)
    }

    @Throws(SQLException::class, ClassNotFoundException::class)
    fun searchMusic(musicID: Int): MusicModel? {
        val selectStmt = "SELECT * FROM " + MusicModel.NAME_TABLE + " WHERE " + MusicModel.MUSIC_ID + "= ?"
        try {
            val rsEmp = dbUtils.dbExecuteQuery(selectStmt, arrayListOf(Pair(DBUtil.Type.INTEGER, musicID)))
            val music = getMusicFromResultSet(rsEmp)

            return music
        } catch (e: SQLException) {
            println("While searching an employee with $musicID id, an error occurred: $e")
            throw e
        }
    }

    @Throws(SQLException::class, ClassNotFoundException::class)
    fun searchMusic(): ObservableList<MusicModel> {
        val selectStmt = "SELECT * FROM " + MusicModel.NAME_TABLE + ";"

        try {
            val rsEmps = dbUtils.dbExecuteQuery(selectStmt, arrayListOf())
            val musicList = getMusicList(rsEmps)
            return musicList
        } catch (e: SQLException) {
            println("SQL select operation has been failed: " + e)
            throw e
        }

    }

    @Throws(SQLException::class, ClassNotFoundException::class)
    fun searchMusicByPath(path: String): Observable<Pair<StateSearch, MusicModel>> {
        return Observable.create { subscriber ->
            val selectStmt = "SELECT * FROM " + MusicModel.NAME_TABLE + " WHERE " + MusicModel.MUSIC_PATH + "= ?;"
            try {
                val rsEmps = dbUtils.dbExecuteQuery(selectStmt, arrayListOf(Pair(DBUtil.Type.STRING, path)))
                val music = getMusicFromResultSet(rsEmps)

                if (music == null) {
                    subscriber.onNext(Pair(StateSearch.NOT_FOUND, MusicModel("", "", 0)))
                } else {
                    subscriber.onNext(Pair(StateSearch.FOUND, music))
                }

                subscriber.onComplete()
            } catch (e: SQLException) {
                println("SQL select operation has been failed: " + e)
                throw e
            }
        }
    }



    @Throws(SQLException::class, ClassNotFoundException::class)
    fun updateMusic(music: MusicModel) {
        val updateStmt = " UPDATE " + MusicModel.NAME_TABLE +
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
    fun deleteMusic(id: Int) {
        val updateStmt = "BEGIN\n" +
                " DELETE FROM " + MusicModel.NAME_TABLE +
                " WHERE " + MusicModel.MUSIC_ID + "= ?; COMMIT; END;"

        try {
            dbUtils.dbExecuteUpdate(updateStmt, arrayListOf(Pair(DBUtil.Type.INTEGER, id)))
        } catch (e: SQLException) {
            print("Error occurred while DELETE Operation: " + e)
            throw e
        }

    }

    @Throws(SQLException::class, ClassNotFoundException::class)
    fun insertMusic(music: MusicModel): Observable<MusicModel> {
        return Observable.create { subscriber ->
            searchMusicByPath(music.musicPath)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(JavaFxScheduler.platform())
                    .subscribe { (first, second) ->
                        when (first) {
                            StateSearch.NOT_FOUND -> {
                                println("Music not found.")
                                Observable.create<Any> {
                                    try {
                                        val updateStmt = "INSERT INTO " + MusicModel.NAME_TABLE + " (" + MusicModel.MUSIC_NAME + "," + MusicModel.MUSIC_PATH + "," + MusicModel.MUSIC_DURATION + ") VALUES (?, ?, ?);"
                                        dbUtils.dbExecuteUpdate(updateStmt, arrayListOf(Pair(DBUtil.Type.STRING, music.musicName), Pair(DBUtil.Type.STRING, music.musicPath), Pair(DBUtil.Type.INTEGER, music.musicDuration)))
                                    } catch (e: SQLException) {
                                        e.printStackTrace()
                                    }

                                    it.onComplete()
                                }
                                        .subscribeOn(Schedulers.computation())
                                        .observeOn(JavaFxScheduler.platform())
                                        .doOnComplete {
                                            println("Music Inserted")
                                            Platform.runLater { bus.onNext(Pair(State.ADDED, music)) }
                                            subscriber.onComplete()
                                        }
                                        .subscribe()
                            }
                            StateSearch.FOUND -> subscriber.onComplete()
                        }
                    }
        }
    }

    @Throws(SQLException::class)
    private fun getMusicFromResultSet(rs: ResultSet): MusicModel? {
        var music: MusicModel? = null

        if (rs.next()) {
            music = MusicModel(
                    rs.getString(MusicModel.MUSIC_NAME),
                    rs.getString(MusicModel.MUSIC_PATH),
                    rs.getInt(MusicModel.MUSIC_DURATION))

            music.musicID = rs.getInt(MusicModel.MUSIC_ID)
        }

        return music
    }

    @Throws(SQLException::class, ClassNotFoundException::class)
    private fun getMusicList(rs: ResultSet): ObservableList<MusicModel> {
        val repList = FXCollections.observableArrayList<MusicModel>()

        while (rs.next()) {
            val music = MusicModel(
                    rs.getString(MusicModel.MUSIC_NAME),
                    rs.getString(MusicModel.MUSIC_PATH),
                    rs.getInt(MusicModel.MUSIC_DURATION))

            music.musicID = rs.getInt(MusicModel.MUSIC_ID)

            repList.add(music)
        }
        return repList
    }

    fun toObservable() : Observable<Pair<State, MusicModel>> {
        return bus
    }

    fun hasObservers() : Boolean {
        return bus.hasObservers()
    }
}