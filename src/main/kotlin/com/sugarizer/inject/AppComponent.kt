package com.sugarizer.inject

import com.sugarizer.domain.model.DeviceModel
import com.sugarizer.domain.shared.JADB
import com.sugarizer.main.Main
import view.device.DevicesView
import view.main.MainView

@javax.inject.Singleton
@dagger.Component(modules = arrayOf(AppModule::class))
interface AppComponent {
    fun inject(main: Main)

    fun inject(devicesView: DevicesView)

    fun inject(mainView: MainView)

    fun inject(jadb: JADB)

    fun inject(deviceModel: DeviceModel)
    companion object {
        fun init(application: javafx.application.Application) : AppComponent {
            return DaggerAppComponent.builder()
                    .appModule(AppModule())
                    .build()
        }
    }
}