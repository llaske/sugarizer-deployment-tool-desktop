package com.sugarizer.inject

import com.sugarizer.domain.shared.*
import com.sugarizer.domain.shared.database.DBUtil
import com.sugarizer.domain.shared.database.FileSynchroniser
import com.sugarizer.domain.shared.database.MusicDAO
import com.sugarizer.domain.shared.database.RepositoryDAO
import com.sugarizer.domain.threading.PostExecutionThread
import com.sugarizer.domain.threading.UseCaseExecutor
import dagger.Module
import dagger.Provides
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import java.io.File
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

    @Singleton
    @Provides
    fun providesStringUtils() : StringUtils {
        return StringUtils()
    }

    @Singleton
    @Provides
    fun providesDBUtils(): DBUtil {
        return DBUtil()
    }

    @Singleton
    @Provides
    fun providesRepositoryDAO(): RepositoryDAO {
        return RepositoryDAO()
    }

    @Singleton
    @Provides
    fun providesMusicDAO(): MusicDAO {
        return MusicDAO()
    }

    @Singleton
    @Provides
    fun providesFileSynchroniser(): FileSynchroniser {
        return FileSynchroniser()
    }
}