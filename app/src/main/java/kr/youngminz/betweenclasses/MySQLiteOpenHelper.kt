package kr.youngminz.betweenclasses

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper
import org.json.JSONArray

class MySQLiteOpenHelper(context: Context, name: String, factory: CursorFactory?, version: Int) :
        SQLiteOpenHelper(context, name, factory, version) {

    private val context by lazy { context }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS timetable (lecture_room TEXT, day_of_week TEXT, start_time DATETIME, end_time DATETIME)")

        val inputStream = context.assets.open("timetable.json")
        val json = JSONArray(inputStream.bufferedReader().readText())
        inputStream.close()

        (0 until json.length())
                .map { json.getJSONArray(it) }
                .forEach {
                    db.execSQL("INSERT INTO timetable VALUES (?, ?, ?, ?)",
                            arrayOf(it.getString(0),
                                    it.getString(1),
                                    it.getString(2),
                                    it.getString(3)))
                }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onCreate(db)
    }
}
