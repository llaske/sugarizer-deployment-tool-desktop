package com.sugarizer.utils.shared

import com.sugarizer.BuildConfig
import io.reactivex.Observable
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.io.File
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchEvent
import java.nio.file.WatchService

class SpkManager {
    var watcher: WatchService
    var isRunning: Boolean = false
    var bus: PublishSubject<Pair<State, File>> = PublishSubject.create()
    var file: File = File(BuildConfig.SPK_LOCATION + "\\")

    enum class State {
        CREATE,
        MODIFY,
        DELETE
    }

    init {
        if (!file.exists()) {
            file.mkdir()
        }

        var path = file.toPath().toAbsolutePath()

        watcher = path.fileSystem.newWatchService()

        path.register(watcher,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_DELETE)
    }

    fun startWatching(){
        file.listFiles().forEach {
            if (it.isFile && it.extension.equals("spk")) {
                send(Pair(State.CREATE, it))
            }
        }

        Observable.create<Any> {
            isRunning = true
            while (isRunning) {
                var key = watcher.take()
                var dir = key.watchable() as Path

                key.pollEvents().forEach {
                    var file = dir.resolve((it as WatchEvent<Path>).context()).toFile()

                    if (file.extension.equals("spk")) {
                        when (it.kind()) {
                            StandardWatchEventKinds.ENTRY_CREATE -> send(Pair(State.CREATE, file))
                            StandardWatchEventKinds.ENTRY_MODIFY -> send(Pair(State.MODIFY, file))
                            StandardWatchEventKinds.ENTRY_DELETE -> send(Pair(State.DELETE, file))
                        }
                    }
                }

                if (!key.reset()) {
                    break
                }
            }
        }
                .subscribeOn(Schedulers.computation())
                .observeOn(JavaFxScheduler.platform())
                .subscribe()
    }

    fun stopWatching(){
        isRunning = false
    }

    fun send(objects: Pair<State, File>) {
        bus.onNext(objects)
    }

    fun toObservable() : Observable<Pair<State, File>> {
        return bus
    }

    fun hasObservers() : Boolean {
        return bus.hasObservers()
    }
}