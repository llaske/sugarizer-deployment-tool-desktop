package com.sugarizer.domain.abstraction

import com.sugarizer.domain.model.DeviceModel

interface DeviceRepository {
    fun getDevice(): io.reactivex.Observable<DeviceModel>
}