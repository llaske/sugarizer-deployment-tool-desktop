package com.sugarizer.presentation.view.loadinstruction

import com.google.gson.Gson
import com.sugarizer.domain.model.InstallApkModel
import com.sugarizer.domain.model.InstructionsModel
import com.sugarizer.domain.shared.JADB
import com.sugarizer.domain.shared.ZipOutUtils
import com.sugarizer.main.Main
import com.sugarizer.presentation.custom.ListItemLoadInstruction
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.Label
import javafx.stage.FileChooser
import java.io.File
import javax.inject.Inject

class LoadInstructionPresenter(val view: LoadInstructionContract.View) : LoadInstructionContract.Presenter {
    val zipOut: ZipOutUtils = ZipOutUtils()

    @Inject lateinit var jadb: JADB

    init {
        Main.appComponent.inject(this)
    }

    override fun onClickLoad(): EventHandler<ActionEvent> {
        return EventHandler {
            var directory = FileChooser()
            directory.title = "Choose instruction zip"
            var file = directory.showOpenDialog(view.primaryStage())

            view.setNameZip(file.name)

            zipOut.loadZip(file.absolutePath)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(JavaFxScheduler.platform())
                    .doOnComplete {
                        println("Size: " + zipOut.instruction?.intructions?.size)
                        zipOut.instruction?.intructions?.forEach {
                            when (it.type) {
                                InstructionsModel.Type.INTALL_APK -> view.addInstruction(it.ordre as Int, ListItemLoadInstruction(it.ordre, it.type, (Gson().fromJson(it.data, InstallApkModel::class.java).numberApk.toString())))
                            }
                        }

                        view.canStart(true)
                    }
                    .subscribe {
                        when (it) {
                            ZipOutUtils.STATUS.NOT_COMPLETE -> { view.showProgress(false) }
                            ZipOutUtils.STATUS.IN_PROGRESS -> { view.showProgress(true) }
                            ZipOutUtils.STATUS.COMPLETE -> { view.showProgress(false) }
                        }
                    }
        }
    }

    override fun onClickStart(): EventHandler<ActionEvent> {
        return EventHandler {
            view.canStart(false)

            zipOut.instruction?.intructions?.forEach { instruction ->
                run {
                    view.setProgressOnInstruction(instruction.ordre as Int, true)

                    when (instruction.type) {
                        InstructionsModel.Type.INTALL_APK -> {
                            val install = Gson().fromJson(instruction.data, InstallApkModel::class.java)

                            install.apks?.forEachIndexed { index, apk ->
                                run {
                                    jadb.listJadb.forEach {
                                        jadb.installAPK(it, File("tmp\\" + apk), true)
                                                .subscribeOn(Schedulers.computation())
                                                .observeOn(JavaFxScheduler.platform())
                                                .doOnComplete {
                                                    if (index.equals(install.apks?.size?.minus(1))) {
                                                        view.setProgressOnInstruction(instruction.ordre as Int, false)
                                                    }
                                                }
                                                .subscribe()
                                    }
                                }
                            }
                        }
                        InstructionsModel.Type.PUSH_FILE -> TODO()
                        InstructionsModel.Type.DELETE_FILE -> TODO()
                        null -> TODO()
                    }
                }.let {
                    view.canStart(true)
                }
            }
        }
    }
}