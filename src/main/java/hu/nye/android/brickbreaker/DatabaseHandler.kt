package hu.nye.android.zhbeadando

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DatabaseHandler(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "Scores"
        private const val TABLE_USERNAMES = "Usernames"
        private const val TABLE_SCORES = "Scores"
        private const val KEY_ID = "id"
        private const val KEY_USERNAME = "username"
        private const val KEY_SCORE = "score"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableUsernames = ("CREATE TABLE $TABLE_USERNAMES (" +
                "$KEY_ID INTEGER PRIMARY KEY," +
                "$KEY_USERNAME TEXT" + ")")
        val createTableScores = ("CREATE TABLE $TABLE_SCORES (" +
                "$KEY_ID INTEGER PRIMARY KEY," +
                "$KEY_USERNAME TEXT," +
                "$KEY_SCORE INTEGER" + ")")
        db.execSQL(createTableUsernames)
        db.execSQL(createTableScores)
    }


    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERNAMES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SCORES")
        onCreate(db)
    }

    fun addScore(username: String, score: Int): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_USERNAME, username)
        values.put(KEY_SCORE, score)
        val success = db.insert(TABLE_SCORES, null, values)
        db.close()
        return success
    }

    fun getUserScores(username: String): List<String> {
        val userScores = ArrayList<String>()
        val selectQuery = "SELECT $KEY_SCORE FROM $TABLE_SCORES WHERE $KEY_USERNAME = '$username'"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        if (cursor.moveToFirst()) {
            val scoreIndex = cursor.getColumnIndex(KEY_SCORE)
            if (scoreIndex != -1) {
                do {
                    val score = cursor.getInt(scoreIndex)
                    userScores.add(score.toString())
                } while (cursor.moveToNext())
            } else {
                Log.e("DatabaseHandler", "Score column not found")
            }
        }
        cursor.close()
        db.close()
        return userScores
    }

    fun addUsername(username: String): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_USERNAME, username)
        val success = db.insert(TABLE_USERNAMES, null, values)
        db.close()
        return success
    }

}
