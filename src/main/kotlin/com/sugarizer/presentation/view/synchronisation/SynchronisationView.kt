package com.sugarizer.presentation.view.synchronisation

import com.jfoenix.controls.*
import com.sugarizer.domain.model.RepositoryModel
import com.sugarizer.domain.shared.database.FileSynchroniser
import com.sugarizer.domain.shared.database.RepositoryDAO
import com.sugarizer.main.Main
import com.sugarizer.presentation.custom.ListItemRepository
import com.sugarizer.presentation.custom.SynchronisationTab
import com.sugarizer.presentation.view.synchronisation.tabs.SynchronisationMusic
import io.reactivex.Observable
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.layout.StackPane
import javafx.stage.DirectoryChooser
import tornadofx.selectedItem
import java.net.URL
import java.util.*
import javafx.scene.control.*
import javax.inject.Inject

class SynchronisationView : Initializable, SynchronisationContract.View {
    enum class Type {
        DOCUMENT,
        MUSIC,
        VIDEO;

        override fun toString(): String {
            when (this) {
                DOCUMENT -> return "document"
                MUSIC -> return "music"
                VIDEO -> return "video"
                else -> return ""
            }
        }
    }

    @FXML lateinit var document: SynchronisationTab
    @FXML lateinit var music: SynchronisationMusic
    @FXML lateinit var video: SynchronisationTab
    @FXML lateinit var addRepository: JFXRippler
    @FXML lateinit var repositoryAdd: JFXDialog
    @FXML lateinit var rootSync: StackPane
    @FXML lateinit var okInstruction: JFXButton
    @FXML lateinit var cancelInstruction: JFXButton
    @FXML lateinit var selectRepository: JFXButton
    @FXML lateinit var typeRepository: ComboBox<String>
    @FXML lateinit var listRepository: JFXListView<ListItemRepository>
    @FXML lateinit var typeColumn: TableColumn<RepositoryModel, String>
    @FXML lateinit var nameColumn: TableColumn<RepositoryModel, String>
    @FXML lateinit var nbFileColumn: TableColumn<RepositoryModel, Int>

    @Inject lateinit var repositoryDAO: RepositoryDAO
    @Inject lateinit var fileSynchroniser: FileSynchroniser

    var path = ""

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        Main.appComponent.inject(this)

        document.type = Type.DOCUMENT
        music.type = Type.MUSIC
        video.type = Type.VIDEO

        addRepository.onMouseClicked = EventHandler { repositoryAdd.show(rootSync) }
        okInstruction.onAction = onOkClick()
        cancelInstruction.onAction = EventHandler { repositoryAdd.close() }
        selectRepository.onAction = onSelectRepositoryClick()

//        typeColumn.cellValueFactory = PropertyValueFactory<RepositoryModel, String>("repositoryCategoryID")
//        nameColumn.cellValueFactory = PropertyValueFactory<RepositoryModel, String>("repositoryName")
//        nbFileColumn.cellValueFactory = PropertyValueFactory<RepositoryModel, Int>("repositoryNumberFile")

        repositoryDAO.searchRepositories().forEach { listRepository.items.add(ListItemRepository(it)) }

        repositoryDAO.toObservable().subscribe {
            when (it.first) {
                RepositoryDAO.State.ADDED -> onRepositoryAdded(it.second)
                RepositoryDAO.State.CHANGED -> onRepositoryChanged(it.second)
                RepositoryDAO.State.REMOVED -> onRepositoryRemoved(it.second)
            }
        }
    }

    fun onSelectRepositoryClick(): EventHandler<ActionEvent> {
        return EventHandler {
            var directory = DirectoryChooser()
            directory.title = "Choose the apk directory"
            var choosedDirectory = directory.showDialog(Main.primaryStage)

            path = choosedDirectory.absolutePath
            selectRepository.text = choosedDirectory.name
        }
    }

    fun onOkClick(): EventHandler<ActionEvent> {
        return EventHandler {
            Observable.create<Any> { subscriber ->
                typeRepository.selectedItem?.let {
                    if (!selectRepository.text.equals("Select") && selectRepository.text.isNotEmpty()){
                        repositoryDAO.insertRep(selectRepository.text, path, getIntFromType(it))
                                .subscribeOn(Schedulers.computation())
                                .doOnComplete { subscriber.onComplete() }
                                .subscribe()
                    } else {
                        subscriber.onComplete()
                    }
                }
            }
                    .subscribeOn(Schedulers.computation())
                    .observeOn(JavaFxScheduler.platform())
                    .doOnComplete {
                        repositoryAdd.close()
                        it.consume()

                        println("Starting search...")

                        fileSynchroniser.startSearching()
                                .subscribeOn(Schedulers.computation())
                                .observeOn(JavaFxScheduler.platform())
                                .doOnComplete { println("Search Finish") }
                                .subscribe { println("State: " + it) }

                        println("Size Rep:" + repositoryDAO.searchRepositories().size)
                    }
                    .subscribe()
        }
    }

    fun getIntFromType(type: String): Int {
        when (type) {
            "Music" -> return 1
            "Video" -> return 2
            "Document" -> return 3
        }

        return 0
    }

    fun onRepositoryAdded(repositoryModel: RepositoryModel){
        listRepository.items.add(ListItemRepository(repositoryModel))
    }

    fun onRepositoryRemoved(repositoryModel: RepositoryModel){
        println("Size B: " + listRepository.items.size)
        listRepository.items
                .filter { it.repositoryModel.repositoryID.equals(repositoryModel.repositoryID) }
                .forEach { listRepository.items.remove(it) }
        println("Size A: " + listRepository.items.size)
    }

    fun onRepositoryChanged(repositoryModel: RepositoryModel){

    }
}