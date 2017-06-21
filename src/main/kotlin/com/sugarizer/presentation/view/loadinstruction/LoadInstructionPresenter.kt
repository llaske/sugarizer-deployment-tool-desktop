package com.sugarizer.presentation.view.loadinstruction

import com.google.gson.Gson
import com.sugarizer.domain.model.InstallApkModel
import com.sugarizer.domain.model.InstructionsModel
import com.sugarizer.domain.shared.ZipOutUtils
import com.sugarizer.presentation.custom.ListItemLoadInstruction
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.Label
import javafx.stage.FileChooser

class LoadInstructionPresenter(val view: LoadInstructionContract.View) : LoadInstructionContract.Presenter {
    val zipOut: ZipOutUtils = ZipOutUtils()

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
                                InstructionsModel.Type.INTALL_APK -> view.addInstruction(ListItemLoadInstruction(it.ordre, it.type, (Gson().fromJson(it.data, InstallApkModel::class.java).numberApk.toString())))
                            }
                        }
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

}