package com.sugarizer.inject

import com.sugarizer.domain.model.DeviceModel
import com.sugarizer.domain.shared.JADB
import com.sugarizer.main.Main
import com.sugarizer.presentation.custom.ListItemDevice
import com.sugarizer.presentation.view.createinstruction.CreateInstructionPresenter
import com.sugarizer.presentation.view.device.DevicePresenter
import com.sugarizer.presentation.view.device.DeviceSideMenu
import com.sugarizer.presentation.view.device.DevicesView
import com.sugarizer.presentation.view.devicedetails.view.devicedetails.DeviceDetailsPresenter
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

    companion object {
        fun init(application: javafx.application.Application) : AppComponent {
            return DaggerAppComponent.builder()
                    .appModule(AppModule())
                    .build()
        }
    }
}