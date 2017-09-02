package kr.youngminz.betweenclasses

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ListView
import android.widget.SimpleAdapter

class MainActivity : AppCompatActivity() {

    private val roomList by lazy { findViewById<ListView>(R.id.list_room) }
    private val db by lazy { MySQLiteOpenHelper(this, "database.db", null, 1).readableDatabase }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val cursor = db.rawQuery("""
            SELECT
              lecture_room,
              (SELECT
                MIN(start_time)
              FROM timetable AS t2
              WHERE t2.lecture_room = t1.lecture_room
              AND day_of_week = (CASE CAST(strftime('%w', datetime("now", '+9 hours')) AS INTEGER)
                WHEN 1 THEN '월'
                WHEN 2 THEN '화'
                WHEN 3 THEN '수'
                WHEN 4 THEN '목'
                WHEN 5 THEN '금'
              END)
              AND time("now", '+9 hours') <= t2.start_time)
              AS next_time
            FROM (SELECT DISTINCT
              lecture_room
            FROM timetable) AS t1
            WHERE lecture_room != ""
            AND NOT EXISTS (SELECT
              *
            FROM timetable t3
            WHERE day_of_week = (CASE CAST(strftime('%w', datetime("now", '+9 hours')) AS INTEGER)
              WHEN 1 THEN '월'
              WHEN 2 THEN '화'
              WHEN 3 THEN '수'
              WHEN 4 THEN '목'
              WHEN 5 THEN '금'
            END)
            AND time("now", '+9 hours') BETWEEN start_time AND end_time
            AND t1.lecture_room = t3.lecture_room)
            ORDER BY lecture_room""", null)

        val itemList: ArrayList<HashMap<String, String>> = ArrayList()

        while (cursor.moveToNext()) {
            val lectureRoom: String = cursor.getString(0)
            val nextStartTime: String? = cursor.getString(1)

            itemList.add(hashMapOf(
                    Pair("Lecture Room", lectureRoom),
                    Pair("Next Start Time", nextStartTime ?: "다음 수업 없음")))
        }

        cursor.close()

        val adapter = SimpleAdapter(this, itemList, android.R.layout.simple_list_item_2,
                arrayOf("Lecture Room", "Next Start Time"),
                intArrayOf(android.R.id.text1, android.R.id.text2))
        roomList.adapter = adapter
    }
}
