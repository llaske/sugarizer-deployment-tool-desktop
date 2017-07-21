package com.sugarizer.domain.shared.database

import com.sugarizer.domain.model.MusicModel
import com.sugarizer.domain.model.RepositoryModel
import com.sun.rowset.CachedRowSetImpl;
import javafx.beans.property.ObjectProperty

import java.sql.*;
import java.util.*
import kotlin.collections.ArrayList

class DBUtil {
    enum class Type {
        INTEGER,
        STRING,
        BOOLEAN
    }

    //Connection String
    //String connStr = "jdbc:oracle:thin:Username/Password@IP:Port/SID";
    //Username=HR, Password=HR, IP=localhost, IP=1521, SID=xe
    //private val connStr: String = "jdbc:oracle:thin:@localhost:1521:xe"
    private val connStr: String = "jdbc:sqlite:test.db"
    //private val JDBC_DRIVER: String = "oracle.jdbc.driver.OracleDriver"
    private val JDBC_DRIVER: String = "org.sqlite.JDBC"
    private var conn: Connection? = null

    //Connect to DB
    fun dbConnect() { // throws SQLException, ClassNotFoundException
        //Setting Oracle JDBC Driver
        try {
            Class.forName(JDBC_DRIVER);
        } catch (e: ClassNotFoundException) {
            System.out.println("Where is your Oracle JDBC Driver?");
            e.printStackTrace();
            throw e;
        }

        System.out.println("Oracle JDBC Driver Registered!");

        //Establish the Oracle Connection using Connection String
        try {
            conn = DriverManager.getConnection(connStr)

            conn?.let {
                var stmt = it.createStatement()
                println("Creation - S")
                stmt.executeUpdate(RepositoryModel.sqlCreate)
                stmt.executeUpdate(MusicModel.sqlCreate)
                stmt.close()
                println("Creation - E")
            }
        } catch (e: SQLException) {
            System.out.println("Connection Failed! Check output console" + e);
            e.printStackTrace();
            throw e;
        }
    }

    //Close Connection
    fun dbDisconnect() {
        try {
            conn?.let {
                if (!it.isClosed) {
                    it.close();
                }
            }
        } catch (e: Exception){
           throw e;
        }
    }

    fun dbExecuteQuery(queryStmt: String, args: ArrayList<Pair<Type, Any>>): ResultSet {
        var stmt: Statement? = null
        var resultSet: ResultSet? = null
        var crs: CachedRowSetImpl? = null
        var prep: PreparedStatement? = null

        try {
            dbConnect();
            System.out.println("Select statement: " + queryStmt + "\n");
            conn?.let { prep = it.prepareStatement(queryStmt) }
            prep?.let { args.forEachIndexed { index, s ->
                when (s.first) {
                    Type.INTEGER -> it.setInt(index + 1, s.second as Int)
                    Type.STRING -> it.setString(index + 1, s.second as String)
                    Type.BOOLEAN -> it.setBoolean(index + 1, s.second as Boolean)
                }
            }

                resultSet = it.executeQuery()
            }

            crs = CachedRowSetImpl()
            crs.populate(resultSet)
        } catch (e: SQLException) {
            System.out.println("Problem occurred at executeQuery operation : " + e);
            throw e;
        } finally {
            resultSet?.let { it.close(); }
            stmt?.let { it.close() }
            //dbDisconnect();
        }
        return crs as ResultSet
    }

    fun dbExecuteUpdate(sqlStmt: String, args: ArrayList<Pair<Type, Any>>) {
        var stmt: PreparedStatement? = null

        try {
            dbConnect();
            conn?.let { stmt = it.prepareStatement(sqlStmt) }
            stmt?.let { args.forEachIndexed { index, s ->
                    when (s.first) {
                        Type.INTEGER -> it.setInt(index + 1, s.second as Int)
                        Type.STRING -> it.setString(index + 1, s.second as String)
                        Type.BOOLEAN -> it.setBoolean(index + 1, s.second as Boolean)
                    }
                }

                it.executeUpdate()
            }
        } catch (e: SQLException) {
            System.out.println("Problem occurred at executeUpdate operation : " + e);
            throw e;
        } finally {
            stmt?.let { it.close() }
            //dbDisconnect();
        }
    }
}