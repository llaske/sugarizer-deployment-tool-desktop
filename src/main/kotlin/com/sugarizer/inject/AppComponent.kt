package com.sugarizer.inject

import com.sugarizer.domain.model.DeviceModel
import com.sugarizer.domain.shared.JADB
import com.sugarizer.domain.shared.database.DeviceDAO
import com.sugarizer.domain.shared.database.FileSynchroniser
import com.sugarizer.domain.shared.database.MusicDAO
import com.sugarizer.domain.shared.database.RepositoryDAO
import com.sugarizer.main.Main
import com.sugarizer.presentation.custom.ListItemApplication
import com.sugarizer.presentation.custom.ListItemDevice
import com.sugarizer.presentation.custom.ListItemRepository
import com.sugarizer.presentation.view.appmanager.AppManagerView
import com.sugarizer.presentation.view.createinstruction.CreateInstructionPresenter
import com.sugarizer.presentation.view.device.DeviceSideMenu
import com.sugarizer.presentation.view.device.DevicesView
import com.sugarizer.presentation.view.devicedetails.view.devicedetails.DeviceDetailsPresenter
import com.sugarizer.presentation.view.loadinstruction.LoadInstructionPresenter
import com.sugarizer.presentation.view.synchronisation.SynchronisationView
import com.sugarizer.presentation.view.synchronisation.tabs.SynchronisationMusic
import view.main.MainView

@javax.inject.Singleton
@dagger.Component(modules = arrayOf(AppModule::class))
interface AppComponent {
    fun inject(main: Main)

    fun inject(devicesView: DevicesView)

    fun inject(mainView: MainView)

    fun inject(jadb: JADB)

    fun inject(deviceModel: DeviceModel)

    fun inject(appManagerView: AppManagerView)

    fun inject(loadInstructionPresenter: LoadInstructionPresenter)

    fun inject(listItemApplication: ListItemApplication)

    fun inject(createInstructionPresenter: CreateInstructionPresenter)

    fun inject(deviceDetailsPresenter: DeviceDetailsPresenter)

    fun inject(listItemDevice: ListItemDevice)

    fun inject(deviceSideMenu: DeviceSideMenu)

    fun inject(repositoryDAO: RepositoryDAO)

    fun inject(synchronisationView: SynchronisationView)

    fun inject(fileSynchroniser: FileSynchroniser)

    fun inject(musicDAO: MusicDAO)

    fun inject(listItemRepository: ListItemRepository)

    fun inject(synchronisationMusic: SynchronisationMusic)

    fun inject(deviceDAO: DeviceDAO)

    companion object {
        fun init(application: javafx.application.Application) : AppComponent {
            return DaggerAppComponent.builder()
                    .appModule(AppModule())
                    .build()
        }
    }
}