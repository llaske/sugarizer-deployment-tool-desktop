package inject

import dagger.Module
import dagger.Provides
import domain.shared.JADB
import domain.threading.PostExecutionThread
import domain.threading.UseCaseExecutor
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
}