package domain.threading

import io.reactivex.Scheduler

interface PostExecutionThread {
    fun getSchedule() : Scheduler
}
