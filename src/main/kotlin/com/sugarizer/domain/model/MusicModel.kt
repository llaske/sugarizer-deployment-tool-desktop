package com.sugarizer.domain.model

class MusicModel(name: String, path: String, duration: Int) {

    companion object {
        val NAME_TABLE = "musics"
        val MUSIC_ID = "music_id"
        val MUSIC_NAME = "music_name"
        val MUSIC_DURATION = "music_duration"
        val MUSIC_PATH = "music_path"

        val sqlCreate = "CREATE TABLE IF NOT EXISTS $NAME_TABLE " +
                "($MUSIC_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                " $MUSIC_NAME TEXT NOT NULL," +
                " $MUSIC_PATH TEXT NOT NULL," +
                " $MUSIC_DURATION INT NOT NULL)"

        val extensions = arrayListOf("mp3")
    }

    var musicID: Int = -1
    var musicName: String = name
    var musicDuration: Int = duration
    var musicPath: String = path
}