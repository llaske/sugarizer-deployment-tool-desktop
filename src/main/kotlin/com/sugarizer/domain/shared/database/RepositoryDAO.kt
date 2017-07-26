package com.sugarizer.domain.shared.database

import com.sugarizer.domain.model.RepositoryModel
import com.sugarizer.main.Main
import io.reactivex.Observable
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import javafx.beans.property.adapter.JavaBeanBooleanProperty
import java.sql.SQLException
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import java.sql.ResultSet
import javax.inject.Inject

class RepositoryDAO {
    @Inject lateinit var dbUtils: DBUtil

    enum class State {
        ADDED,
        CHANGED,
        REMOVED
    }

    var bus: PublishSubject<Pair<State, RepositoryModel>> = PublishSubject.create()

    init {
        Main.appComponent.inject(this)
    }

    @Throws(SQLException::class, ClassNotFoundException::class)
    fun searchRepository(repositoryID: Int): Observable<RepositoryModel> {
        return Observable.create<RepositoryModel> { subscriber ->
            val selectStmt = "SELECT * FROM " + RepositoryModel.NAME_TABLE + " WHERE " + RepositoryModel.REPOSITORY_ID + "= ?"
            try {
                val rsEmp = dbUtils.dbExecuteQuery(selectStmt, arrayListOf(Pair(DBUtil.Type.INTEGER, repositoryID)))
                val employee = getEmployeeFromResultSet(rsEmp)

                employee?.let {
                    println("Repo finded !")
                    subscriber.onNext(it)
                }
                subscriber.onComplete()
            } catch (e: SQLException) {
                println("While searching an employee with $repositoryID id, an error occurred: $e")
            }
        }
    }

    @Throws(SQLException::class, ClassNotFoundException::class)
    fun searchRepositoryByPath(repositoryPath: String): Observable<RepositoryModel> {
        return Observable.create<RepositoryModel> { subscriber ->
            val selectStmt = "SELECT * FROM " + RepositoryModel.NAME_TABLE + " WHERE " + RepositoryModel.REPOSITORY_PATH + "= ?"
            try {
                val rsEmp = dbUtils.dbExecuteQuery(selectStmt, arrayListOf(Pair(DBUtil.Type.STRING, repositoryPath)))
                val repo = getEmployeeFromResultSet(rsEmp)

                repo?.let {
                    subscriber.onNext(it)
                }
                subscriber.onComplete()
            } catch (e: SQLException) {
                println("While searching an employee with $repositoryPath path, an error occurred: $e")
            }
        }
    }

    @Throws(SQLException::class)
    private fun getEmployeeFromResultSet(rs: ResultSet): RepositoryModel? {
        var rep: RepositoryModel? = null
        if (rs.next()) {
            rep = RepositoryModel()
            rep.repositoryID = rs.getInt(RepositoryModel.REPOSITORY_ID)
            rep.repositoryPath = rs.getString(RepositoryModel.REPOSITORY_PATH)
            rep.repositoryName.set(rs.getString(RepositoryModel.REPOSITORY_NAME))
            rep.repositoryCategoryID.set(getCategory(rs.getInt(RepositoryModel.REPOSITORY_CATEGORY_ID)))
            rep.repositoryNumberFile.set(rs.getInt(RepositoryModel.REPOSITORY_NUMBER_FILE))
        }
        return rep
    }

    fun getCategory(i: Int): RepositoryModel.Category {
        when (i) {
            1 -> return RepositoryModel.Category.MUSIC
            2 -> return RepositoryModel.Category.VIDEO
            3 -> return RepositoryModel.Category.DOCUMENT
        }

        return RepositoryModel.Category.NONE
    }

    @Throws(SQLException::class, ClassNotFoundException::class)
    fun searchRepositories(): ObservableList<RepositoryModel> {
        val selectStmt = "SELECT * FROM " + RepositoryModel.NAME_TABLE + ";"

        try {
            val rsEmps = dbUtils.dbExecuteQuery(selectStmt, arrayListOf())

            val empList = getEmployeeList(rsEmps)

            return empList
        } catch (e: SQLException) {
            println("SQL select operation has been failed: " + e)
        }

        return FXCollections.emptyObservableList()
    }

    @Throws(SQLException::class, ClassNotFoundException::class)
    private fun getEmployeeList(rs: ResultSet): ObservableList<RepositoryModel> {
        val repList = FXCollections.observableArrayList<RepositoryModel>()

        while (rs.next()) {
            val rep = RepositoryModel()
            rep.repositoryID = rs.getInt(RepositoryModel.REPOSITORY_ID)
            rep.repositoryPath = rs.getString(RepositoryModel.REPOSITORY_PATH)
            rep.repositoryName.set(rs.getString(RepositoryModel.REPOSITORY_NAME))
            rep.repositoryCategoryID.set(getCategory(rs.getInt(RepositoryModel.REPOSITORY_CATEGORY_ID)))
            rep.repositoryNumberFile.set(rs.getInt(RepositoryModel.REPOSITORY_NUMBER_FILE))
            //rep.repositorySynced = rs.getBoolean(RepositoryModel.REPOSITORY_SYNCED)
            //Add employee to the ObservableList
            repList.add(rep)
        }
        //return repList (ObservableList of Employees)
        return repList
    }

    @Throws(SQLException::class, ClassNotFoundException::class)
    fun updateRepEmail(empId: String, empEmail: String) {
        val updateStmt = "BEGIN" +
                " UPDATE $RepositoryModel.NAME_TABLE" +
                " SET EMAIL = ?" +
                " WHERE EMPLOYEE_ID = ?; COMMIT; END;"

        try {
            dbUtils.dbExecuteUpdate(updateStmt, arrayListOf(Pair(DBUtil.Type.STRING, empEmail), Pair(DBUtil.Type.INTEGER, empId)))
        } catch (e: SQLException) {
            print("Error occurred while UPDATE Operation: " + e)
            throw e
        }

    }

    @Throws(SQLException::class, ClassNotFoundException::class)
    fun updateRepNumberFile(repID: String, numberFile: Int) {
        val updateStmt = "BEGIN" +
                " UPDATE " + RepositoryModel.NAME_TABLE +
                " SET " + RepositoryModel.REPOSITORY_NUMBER_FILE + " = ? " +
                " WHERE " + RepositoryModel.REPOSITORY_ID + "= ?; COMMIT; END;"

        //Execute UPDATE operation
        try {
            dbUtils.dbExecuteUpdate(updateStmt, arrayListOf(Pair(DBUtil.Type.INTEGER, numberFile), Pair(DBUtil.Type.INTEGER, repID)))
        } catch (e: SQLException) {
            print("Error occurred while UPDATE Operation: " + e)
            throw e
        }

    }

    @Throws(SQLException::class, ClassNotFoundException::class)
    fun deleteRepWithId(repID: Int) {
        searchRepository(repID)
                .subscribeOn(Schedulers.computation())
                .observeOn(JavaFxScheduler.platform())
                .subscribe { repository ->
                    println("Delete ?")
                    Observable.create<Any> {
                        try {
                            val updateStmt = "DELETE FROM " + RepositoryModel.NAME_TABLE + " WHERE " + RepositoryModel.REPOSITORY_ID + " = ?; COMMIT; END;"

                            dbUtils.dbExecuteUpdate(updateStmt, arrayListOf(Pair(DBUtil.Type.INTEGER, repID)))
                        } catch (e: SQLException) {
                            print("Error occurred while DELETE Operation: " + e)
                        }

                        it.onComplete()
                    }
                            .subscribeOn(Schedulers.computation())
                            .observeOn(JavaFxScheduler.platform())
                            .doOnComplete { bus.onNext(Pair(State.REMOVED, repository)) }
                            .subscribe()
                }
    }

    @Throws(SQLException::class, ClassNotFoundException::class)
    fun insertRep(name: String, path: String, type: Int): Observable<Any> {
        return Observable.create { subscriber ->
        Observable.create<RepositoryModel> {
            try {
                val updateStmt = "INSERT INTO " + RepositoryModel.NAME_TABLE + " (" + RepositoryModel.REPOSITORY_NAME + "," + RepositoryModel.REPOSITORY_PATH + "," + RepositoryModel.REPOSITORY_CATEGORY_ID + ") VALUES (?, ?, ?);"
                println(updateStmt)
                dbUtils.dbExecuteUpdate(updateStmt, arrayListOf(Pair(DBUtil.Type.STRING, name), Pair(DBUtil.Type.STRING, path), Pair(DBUtil.Type.INTEGER, type)))
            } catch (e: SQLException) {
                print("Error occurred while DELETE Operation: " + e)
            }

            it.onComplete()
        }
                .subscribeOn(Schedulers.computation())
                .observeOn(JavaFxScheduler.platform())
                .doOnComplete { searchRepositoryByPath(path)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(JavaFxScheduler.platform())
                        .subscribe {
                            bus.onNext(Pair(State.ADDED, it))
                            subscriber.onComplete()
                        }
                }
                .subscribe()
        }
    }

    fun toObservable() : Observable<Pair<State, RepositoryModel>> {
        return bus
    }

    fun hasObservers() : Boolean {
        return bus.hasObservers()
    }
}