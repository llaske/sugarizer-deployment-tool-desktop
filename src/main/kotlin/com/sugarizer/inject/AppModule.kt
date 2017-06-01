package com.sugarizer.inject

import com.sugarizer.domain.shared.JADB
import com.sugarizer.domain.shared.RxBus
import com.sugarizer.domain.threading.PostExecutionThread
import com.sugarizer.domain.threading.UseCaseExecutor
import dagger.Module
import dagger.Provides
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import javax.inject.Singleton

@Module
class AppModule() {

    @Provides
    fun providePostExecutionThread() : PostExecutionThread {
        return object : PostExecutionThread {
            override fun getSchedule(): Scheduler {
                return Schedulers.io()
            }
        }
    }

    @Provides
    fun provideUseCaseExecutor() : UseCaseExecutor {
        return object : UseCaseExecutor {
            override fun getScheduler(): Scheduler {
                return Schedulers.io()
            }
        }
    }

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
}