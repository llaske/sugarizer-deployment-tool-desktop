package com.sugarizer.domain.shared.database

import com.sugarizer.domain.model.MusicModel
import com.sugarizer.main.Main
import io.reactivex.Observable
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
            subscriber.onNext(State.ONGOING)

            var list = repDAO.searchRepositories()

            list.forEach { item ->
                println("Item: " + item.repositoryName)
                var repository = File(item.repositoryPath)

                println("Size Rep: " + repository.listFiles().size)
                if (repository.listFiles().size > 0) {
                    repository.listFiles().forEach { music ->
                        println("Music Name: " + music.name)
                        println("Music Ext: " + music.extension)
                        if (!music.isDirectory && music.exists() && MusicModel.extensions.contains(music.extension)) {
                            println("Music detected")
                            if (musicDAO.searchMusicByPath(music.absolutePath) == null) {
                                musicDAO.insertMusic(MusicModel(music.name, music.absolutePath, 0))
                                subscriber.onNext(State.NEW_FILE)

                                println("Music Added")
                            }
                        }
                    }
                }
            }

            subscriber.onNext(State.FINISH)
            subscriber.onComplete()
        }
    }
}