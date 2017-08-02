package com.sugarizer.utils.inject

import com.sugarizer.model.DeviceModel
import com.sugarizer.utils.shared.JADB
import com.sugarizer.Main
import com.sugarizer.listitem.ListItemDevice
import com.sugarizer.view.createinstruction.CreateInstructionPresenter
import com.sugarizer.view.device.DevicePresenter
import com.sugarizer.view.device.DeviceSideMenu
import com.sugarizer.view.device.DevicesView
import com.sugarizer.view.device.type.SPK
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

    fun inject(deviceSideMenu: DeviceSideMenu)

    fun inject(devicePresenter: DevicePresenter)

    fun inject(spk: SPK)

    companion object {
        fun init(application: javafx.application.Application) : AppComponent {
            return DaggerAppComponent.builder()
                    .appModule(AppModule())
                    .build()
        }
    }
}