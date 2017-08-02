package com.sugarizer.utils.inject

import com.sugarizer.utils.shared.*
import dagger.Module
import dagger.Provides
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import javax.inject.Singleton

@Module
class AppModule() {
    @Singleton
    @Provides
    fun provideJADB() : JADB {
        return JADB()
    }

    @Singleton
    @Provides
    fun providesRxBus() : RxBus {
        return RxBus()
    }

    @Singleton
    @Provides
    fun providesStringUtils() : StringUtils {
        return StringUtils()
    }

    @Singleton
    @Provides
    fun providesSpkManager(): SpkManager {
        return SpkManager()
    }

    @Singleton
    @Provides
    fun providesNotificationBus(): NotificationBus {
        return NotificationBus()
    }

    @Singleton
    @Provides
    fun providesAnitmationUtils(): AnimationUtils {
        return AnimationUtils()
    }
}