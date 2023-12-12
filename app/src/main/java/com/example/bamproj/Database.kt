package com.example.bamproj

import android.database.Cursor
import androidx.room.*
import androidx.room.RoomDatabase


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

@Entity
data class User(
    @PrimaryKey(autoGenerate = true) var uid: Int? = null,
    @ColumnInfo(name = "user_name") var userName: String,
    @ColumnInfo(name = "password") var password: String,
    @ColumnInfo(name = "address") var address: String,
    @ColumnInfo(name = "phone_number") var phoneNumber: String
)

@Database(entities = [User::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}