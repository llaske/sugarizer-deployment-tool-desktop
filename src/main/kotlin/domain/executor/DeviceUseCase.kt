package domain.executor

import domain.abstraction.DeviceRepository
import domain.abstraction.UseCase
import io.reactivex.Observable
import domain.model.DeviceModel
import java.util.concurrent.Executor

class DeviceUseCase(threadExecutor: Executor, postThreadExecutor: Executor, deviceRepository: DeviceRepository) : UseCase<DeviceModel>(threadExecutor, postThreadExecutor) {
    val deviceRepository: DeviceRepository = deviceRepository

    override fun createObservable(): Observable<DeviceModel> {
        return deviceRepository.getDevice()
    }
}