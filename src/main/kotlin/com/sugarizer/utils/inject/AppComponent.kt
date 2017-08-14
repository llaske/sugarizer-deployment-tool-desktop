package com.sugarizer.utils.inject

import com.sugarizer.model.DeviceModel
import com.sugarizer.utils.shared.JADB
import com.sugarizer.Main
import com.sugarizer.listitem.ListItemChoosenInstruction
import com.sugarizer.listitem.ListItemDevice
import com.sugarizer.listitem.ListItemInstruction
import com.sugarizer.view.createinstruction.CreateInstructionPresenter
import com.sugarizer.view.createinstruction.CreateInstructionView
import com.sugarizer.view.device.DevicePresenter
import com.sugarizer.view.device.DevicesView
import com.sugarizer.view.device.type.APK
import com.sugarizer.view.device.type.SPK
import com.sugarizer.view.devicedetails.view.devicedetails.CreateInstructionDialog
import com.sugarizer.view.devicedetails.view.devicedetails.DeviceDetailsPresenter
import view.main.MainView

@javax.inject.Singleton
@dagger.Component(modules = arrayOf(AppModule::class))
interface AppComponent {
    fun inject(main: Main)

    fun inject(devicesView: DevicesView)

    fun inject(mainView: MainView)

    fun inject(jadb: JADB)

    fun inject(deviceModel: DeviceModel)

    fun inject(createInstructionPresenter: CreateInstructionPresenter)

    fun inject(deviceDetailsPresenter: DeviceDetailsPresenter)

    fun inject(listItemDevice: ListItemDevice)

    fun inject(devicePresenter: DevicePresenter)

    fun inject(spk: SPK)

    fun inject(apk: APK)

    fun inject(createInstructionView: CreateInstructionView)

    fun inject(listItemInstruction: ListItemInstruction)

    fun inject(listItemChoosenInstruction: ListItemChoosenInstruction)

    fun inject(createInstructionDialog: CreateInstructionDialog)

    companion object {
        fun init(application: javafx.application.Application) : AppComponent {
            return DaggerAppComponent.builder()
                    .appModule(AppModule())
                    .build()
        }
    }
}