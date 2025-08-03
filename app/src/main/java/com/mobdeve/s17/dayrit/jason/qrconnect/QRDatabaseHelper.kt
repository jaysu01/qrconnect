package com.mobdeve.s17.dayrit.jason.qrconnect

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// entity class for room
@Entity(tableName = "qr_history")
data class QRHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "type")
    val type: String,
    @ColumnInfo(name = "content")
    val content: String,
    @ColumnInfo(name = "timestamp")
    val timestamp: String
)

// dao interface for room operations
@Dao
interface QRHistoryDao {
    @Query("SELECT * FROM qr_history ORDER BY id DESC")
    suspend fun getAllHistory(): List<QRHistoryEntity>

    @Insert
    suspend fun insertHistory(history: QRHistoryEntity)

    @Query("DELETE FROM qr_history WHERE id = :id")
    suspend fun deleteHistoryById(id: Int)

    @Query("DELETE FROM qr_history")
    suspend fun clearAllHistory()
}

// room database
@Database(
    entities = [QRHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class QRDatabase : RoomDatabase() {
    abstract fun historyDao(): QRHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: QRDatabase? = null

        fun getDatabase(context: Context): QRDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QRDatabase::class.java,
                    "qr_history.db"
                )
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // database is created
                        }
                    })
                    .fallbackToDestructiveMigration() // for simplicity
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// updated helper class that maintains existing interface but uses room internally
class QRDatabaseHelper(private val context: Context) {
    private val database = QRDatabase.getDatabase(context)
    private val dao = database.historyDao()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    // convert QRHistoryEntity to HistoryItem for backward compatibility
    private fun QRHistoryEntity.toHistoryItem() = HistoryItem(
        id = this.id,
        type = this.type,
        content = this.content,
        timestamp = this.timestamp
    )

    // method to insert history data
    fun insertHistory(type: String, content: String, timestamp: String) {
        coroutineScope.launch {
            try {
                dao.insertHistory(
                    QRHistoryEntity(
                        type = type,
                        content = content,
                        timestamp = timestamp
                    )
                )
            } catch (e: Exception) {
                // handle error silently to maintain compatibility
            }
        }
    }

    // method to fetch all history records
    fun getAllHistory(): List<HistoryItem> {
        return try {
            runBlocking {
                dao.getAllHistory().map { it.toHistoryItem() }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // method to delete a specific history entry
    fun deleteHistory(id: Int) {
        coroutineScope.launch {
            try {
                dao.deleteHistoryById(id)
            } catch (e: Exception) {
                // handle error silently to maintain compatibility
            }
        }
    }

    // method to clear all history
    fun clearAllHistory() {
        coroutineScope.launch {
            try {
                dao.clearAllHistory()
            } catch (e: Exception) {
                // handle error silently to maintain compatibility
            }
        }
    }

    // new methods for better coroutine support
    suspend fun insertHistoryAsync(type: String, content: String, timestamp: String) {
        dao.insertHistory(
            QRHistoryEntity(
                type = type,
                content = content,
                timestamp = timestamp
            )
        )
    }

    suspend fun getAllHistoryAsync(): List<HistoryItem> {
        return dao.getAllHistory().map { it.toHistoryItem() }
    }

    suspend fun deleteHistoryAsync(id: Int) {
        dao.deleteHistoryById(id)
    }

    suspend fun clearAllHistoryAsync() {
        dao.clearAllHistory()
    }

    // cleanup method
    fun close() {
        // room handles connection management automatically
        // this method is kept for interface compatibility
    }

    companion object {
        // constants maintained for backward compatibility
        const val DATABASE_NAME = "qr_history.db"
        const val DATABASE_VERSION = 1
        const val TABLE_NAME = "qr_history"
        const val COLUMN_ID = "id"
        const val COLUMN_TYPE = "type"
        const val COLUMN_CONTENT = "content"
        const val COLUMN_TIMESTAMP = "timestamp"
    }
}