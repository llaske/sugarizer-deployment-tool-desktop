package domain.abstraction

import io.reactivex.Observable
import domain.model.DeviceModel

interface DeviceRepository {
    fun getDevice(): Observable<DeviceModel>
}