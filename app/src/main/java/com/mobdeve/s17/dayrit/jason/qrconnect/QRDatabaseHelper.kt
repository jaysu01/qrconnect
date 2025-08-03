package com.mobdeve.s17.dayrit.jason.qrconnect

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class QRDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "qr_history.db"
        private const val DATABASE_VERSION = 1
        const val TABLE_NAME = "qr_history"
        const val COLUMN_ID = "id"
        const val COLUMN_TYPE = "type"
        const val COLUMN_CONTENT = "content"
        const val COLUMN_TIMESTAMP = "timestamp"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TYPE TEXT,
                $COLUMN_CONTENT TEXT,
                $COLUMN_TIMESTAMP TEXT
            )
        """
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // Method to insert history data
    fun insertHistory(type: String, content: String, timestamp: String) {
        val db = writableDatabase
        val query = "INSERT INTO $TABLE_NAME ($COLUMN_TYPE, $COLUMN_CONTENT, $COLUMN_TIMESTAMP) VALUES (?, ?, ?)"
        db.execSQL(query, arrayOf(type, content, timestamp))
        db.close()
    }

    // Method to fetch all history records
    fun getAllHistory(): List<HistoryItem> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)
        val historyList = mutableListOf<HistoryItem>()
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE))
                val content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT))
                val timestamp = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP))
                historyList.add(HistoryItem(id, type, content, timestamp))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return historyList
    }

    // Method to delete a specific history entry
    fun deleteHistory(id: Int) {
        val db = writableDatabase
        db.execSQL("DELETE FROM $TABLE_NAME WHERE $COLUMN_ID = ?", arrayOf(id))
        db.close()
    }

    // Method to clear all history
    fun clearAllHistory() {
        val db = writableDatabase
        db.execSQL("DELETE FROM $TABLE_NAME")
        db.close()
    }
}