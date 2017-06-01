package com.sugarizer.domain.threading

import io.reactivex.Scheduler

interface PostExecutionThread {
    fun getSchedule() : io.reactivex.Scheduler
}
