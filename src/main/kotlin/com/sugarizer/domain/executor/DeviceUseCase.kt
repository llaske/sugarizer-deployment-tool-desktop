package com.sugarizer.domain.executor

import com.sugarizer.domain.abstraction.DeviceRepository
import com.sugarizer.domain.abstraction.UseCase
import com.sugarizer.domain.model.DeviceModel
import io.reactivex.Observable
import java.util.concurrent.Executor

class DeviceUseCase(threadExecutor: Executor, postThreadExecutor: Executor, deviceRepository: DeviceRepository) : UseCase<DeviceModel>(threadExecutor, postThreadExecutor) {
    val deviceRepository: DeviceRepository = deviceRepository

    override fun createObservable(): Observable<DeviceModel> {
        return deviceRepository.getDevice()
    }
}