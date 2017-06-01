package com.sugarizer.domain.threading

import io.reactivex.Scheduler

interface UseCaseExecutor {
    fun getScheduler() : Scheduler
}