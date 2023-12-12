package com.example.bamproj

import android.app.Application
import androidx.room.Room

class BamApplication : Application() {

    val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "example-database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
}