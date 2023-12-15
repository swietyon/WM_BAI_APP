package com.example.bamproj

import java.time.LocalDateTime
import java.util.Date

data class Note(
    val uid: Long,
    val title: String,
    val content: String,
    val date: Date
)