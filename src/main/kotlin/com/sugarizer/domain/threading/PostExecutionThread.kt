package com.sugarizer.domain.threading

interface PostExecutionThread {
    fun getSchedule() : io.reactivex.Scheduler
}
