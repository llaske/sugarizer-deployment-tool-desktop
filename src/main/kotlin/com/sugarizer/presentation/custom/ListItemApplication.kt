package com.sugarizer.presentation.custom

import com.sugarizer.domain.shared.JADB
import com.sugarizer.main.Main
import io.reactivex.Observable
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.rxkotlin.toSingle
import io.reactivex.schedulers.Schedulers
import javafx.application.Platform
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.OverrunStyle
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.Separator
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.RowConstraints
import net.dongliu.apk.parser.ApkFile
import se.vidstige.jadb.JadbDevice
import tornadofx.gridpaneColumnConstraints
import java.io.File
import javax.inject.Inject

class ListItemApplication(val name: ApkFile, val apkFile: File) : GridPane() {

    @Inject lateinit var jadb: JADB

    val nameLabel = Label(name.apkMeta.packageName)
    val versionLabel = Label(name.apkMeta.versionName)
    val numberDeviveLabel = Label("0/0")
    val installingOn = Label("Not started")
    val progress = ProgressIndicator()

    var iteratorNumber = 0

    init {
        Main.appComponent.inject(this)

        maxHeight = 25.0
        maxWidth = Double.MAX_VALUE

        val rowOne = RowConstraints()
        val rowTwo = RowConstraints()

        val columnOne = ColumnConstraints()
        val columnTwo = ColumnConstraints()
        val columnThree = ColumnConstraints()
        val columnFour = ColumnConstraints()
        val columnFive = ColumnConstraints()

        columnOne.percentWidth = 30.0 // Name
        columnTwo.percentWidth = 10.0 // Version
        columnThree.percentWidth = 10.0 // Number Device
        columnFour.percentWidth = 40.0 // Install On (status)
        columnFive.percentWidth = 20.0 // Progress

        rowConstraints.add(rowOne)
        rowConstraints.add(rowTwo)

        columnConstraints.add(columnOne)
        columnConstraints.add(columnTwo)
        columnConstraints.add(columnThree)
        columnConstraints.add(columnFour)

        versionLabel.maxWidth = Double.MAX_VALUE
        versionLabel.alignment = Pos.CENTER
        numberDeviveLabel.isVisible = true
        numberDeviveLabel.maxWidth = Double.MAX_VALUE
        numberDeviveLabel.alignment = Pos.CENTER
        numberDeviveLabel.text = "0/" + jadb.listJadb.size.toString()
        installingOn.maxWidth = Double.MAX_VALUE
        installingOn.alignment = Pos.CENTER
        installingOn.textOverrun = OverrunStyle.LEADING_ELLIPSIS
        progress.maxWidth = Double.MAX_VALUE
        progress.maxHeight = Double.MAX_VALUE
        progress.isVisible = false

        gridpaneColumnConstraints.let {
            setRowIndex(nameLabel, 0)
            setRowIndex(versionLabel, 0)
            setRowIndex(numberDeviveLabel, 0)
            setRowIndex(installingOn, 0)
            setRowIndex(progress, 0)

            setColumnIndex(nameLabel, 0)
            setColumnIndex(versionLabel, 1)
            setColumnIndex(numberDeviveLabel, 2)
            setColumnIndex(installingOn, 3)
            setColumnIndex(progress, 4)
        }

        children.add(nameLabel)
        children.add(versionLabel)
        children.add(numberDeviveLabel)
        children.add(installingOn)
        children.add(progress)
    }

    fun startInstall(isForce: Boolean): Observable<String> {
        return Observable.create { mainSub -> run {
            Observable.create<String> { subscriber -> run {
                subscriber.onNext("Starting...")
                jadb.listDevice.forEach { deviceModel ->
                    subscriber.onNext("Installing on: " + deviceModel.name.get())

                    jadb.installAPK(deviceModel.jadbDevice, apkFile, isForce)
                            .subscribeOn(Schedulers.computation())
                            .observeOn(JavaFxScheduler.platform())
                            .doOnComplete {
                                numberDeviveLabel.text = (++iteratorNumber).toString() + "/" + jadb.listJadb.size.toString()

                                subscriber.onNext("Installed")
                                subscriber.onComplete()
                            }
                            .subscribe()
                }
            }
            }
                    .subscribeOn(Schedulers.computation())
                    .observeOn(JavaFxScheduler.platform())
                    .doOnComplete {
                        println("OnComplete Installed on all devices")
                        progress.isVisible = false

                        mainSub.onComplete()
                    }.subscribe {
                progress.isVisible = true

                installingOn.text = it
            }
        }
        }
    }

    fun installOnDevice(jadbDevice: JadbDevice, isForce: Boolean) {

    }
}