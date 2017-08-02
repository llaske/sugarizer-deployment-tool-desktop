package com.sugarizer.model

import javafx.beans.property.*

class RepositoryModel {
    enum class Category {
        NONE,
        MUSIC,
        VIDEO,
        DOCUMENT
    }

    companion object {
        val NAME_TABLE = "repositories"
        val REPOSITORY_ID = "repository_id"
        val REPOSITORY_NAME = "repository_name"
        val REPOSITORY_NUMBER_FILE = "repository_number_file"
        val REPOSITORY_CATEGORY_ID = "repository_category_id"
        val REPOSITORY_PATH = "repository_path"

        val sqlCreate = "CREATE TABLE IF NOT EXISTS $NAME_TABLE " +
                "($REPOSITORY_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                " $REPOSITORY_NAME TEXT NOT NULL," +
                " $REPOSITORY_PATH TEXT NOT NULL," +
                " $REPOSITORY_CATEGORY_ID INT NOT NULL," +
                " $REPOSITORY_NUMBER_FILE INT)"
    }

    var repositoryID: Int = -1
    var repositoryPath: String = ""

    var repositoryName: SimpleStringProperty = SimpleStringProperty()
    var repositoryCategoryID: SimpleObjectProperty<Category> = SimpleObjectProperty()
    var repositoryNumberFile: SimpleIntegerProperty = SimpleIntegerProperty()

    fun setRepositoryName(name: String) {
        repositoryName.set(name)
    }

    fun setRepositoryCategoryID(id: Category) {
        repositoryCategoryID.set(id)
    }

    fun setRepositoryNumberFile(nb: Int) {
        repositoryNumberFile.set(nb)
    }

    fun getRepositoryName(): String {
        return repositoryName.get()
    }

    fun getRepositoryCategoryID(): String {
        when (repositoryCategoryID.get()) {
            Category.MUSIC -> return "Music"
            Category.VIDEO -> return "Video"
            Category.DOCUMENT -> return "Document"
        }
        return "None"
    }

    fun getNumberFile(): Int {
        return repositoryNumberFile.get()
    }

    fun repositoryNameProperty(): StringProperty {
        return repositoryName
    }

    fun repositoryCategoryIDProperty(): ObjectProperty<Category> {
        return repositoryCategoryID
    }

    fun repositoryNumberFileProperty(): IntegerProperty {
        return repositoryNumberFile
    }
}