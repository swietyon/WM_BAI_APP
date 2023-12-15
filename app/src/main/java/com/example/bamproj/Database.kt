package com.example.bamproj

import android.database.Cursor
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.*
import androidx.room.RoomDatabase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LocalDateTimeConverter {

    @RequiresApi(Build.VERSION_CODES.O)
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? {
        return dateTime?.format(formatter)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun toLocalDateTime(dateTimeString: String?): LocalDateTime? {
        return dateTimeString?.let { LocalDateTime.parse(it, formatter) }
    }
}
@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    fun getAll(): List<User>

    @Query("SELECT * FROM user")
    fun getAllCursor(): Cursor

    @Insert
    fun insert(user: User)

    @Query("SELECT * FROM user WHERE user_name = :userName")
    fun getUserByUsername(userName: String): User?
}


@Dao
interface NoteDao {
    @Query("SELECT * FROM note WHERE user_name = :userName")
    fun getNoteByUsername(userName: String): List<NoteEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(note: NoteEntity): Long

    @Query("SELECT * FROM note WHERE uid = :uid")
    fun getByUid(uid: Long): NoteEntity

    @Update
    fun update(note: NoteEntity)

    @Query("DELETE FROM note WHERE title = :title AND content = :content")
    fun deleteNoteByTitleAndContent(title: String, content: String)

    @Query("DELETE FROM note WHERE title = :title")
    fun deleteByTitle(title: String)
}

@Entity
data class User(
    @PrimaryKey(autoGenerate = true) var uid: Int? = null,
    @ColumnInfo(name = "user_name") var userName: String,
    @ColumnInfo(name = "password") var password: String,
    @ColumnInfo(name = "address") var address: String,
    @ColumnInfo(name = "phone_number") var phoneNumber: String
)
@Entity(tableName = "note")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) var uid: Long? = null,
    @ColumnInfo(name = "user_name") var userName: String,
    @ColumnInfo(name = "content") var content: String,
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "creation_time") var creationTime: LocalDateTime
)

@Database(entities = [User::class, NoteEntity::class], version = 8)
@TypeConverters(LocalDateTimeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun noteDao(): NoteDao
}
