package navigation

import com.sugarizer.domain.model.DeviceModel
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

class DeviceObserver : Observer<DeviceModel> {
    override fun onError(e: Throwable?) {
    }

    override fun onNext(t: DeviceModel?) {
    }

    override fun onComplete() {
    }

    override fun onSubscribe(d: Disposable?) {
    }
}