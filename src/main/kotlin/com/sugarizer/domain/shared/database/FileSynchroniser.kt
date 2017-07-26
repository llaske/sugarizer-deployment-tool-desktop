package com.sugarizer.domain.shared.database

import com.sugarizer.domain.model.MusicModel
import com.sugarizer.main.Main
import io.reactivex.Observable
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import java.io.File
import javax.inject.Inject

class FileSynchroniser {
    enum class State {
        ONGOING,
        NEW_FILE,
        FINISH
    }

    @Inject lateinit var repDAO: RepositoryDAO
    @Inject lateinit var musicDAO: MusicDAO

    init {
        Main.appComponent.inject(this)
    }

    fun startSearching(): Observable<State> {
        return Observable.create { subscriber ->
            println("Research started !")
            subscriber.onNext(State.ONGOING)

            var list = repDAO.searchRepositories()


            list.forEach { item ->
                println("Item: " + item.repositoryName)
                var repository = File(item.repositoryPath)

                println("Size Rep: " + repository.listFiles().size)
                if (repository.listFiles().size > 0) {
                    insertFile(repository.listFiles(), 0)
                }
            }

            subscriber.onNext(State.FINISH)
            subscriber.onComplete()
        }
    }

    fun insertFile(list: Array<File>, index: Int){
        val music = list[index]

//        println("Music Name: " + music.name)
//        println("Music Ext: " + music.extension)

        if (!music.isDirectory && music.exists() && MusicModel.extensions.contains(music.extension)) {
//            println("Music detected")
            musicDAO.insertMusic(MusicModel(music.name, music.absolutePath, 0))
                    .subscribeOn(Schedulers.computation())
                    .observeOn(JavaFxScheduler.platform())
                    .doOnComplete { if (index < list.size - 1) insertFile(list, index + 1)}
                    .subscribe()
        } else if (index < list.size - 1) {
            insertFile(list, index + 1)
        }
    }
}