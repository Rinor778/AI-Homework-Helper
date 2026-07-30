package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "homework_items")
data class HomeworkItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subject: String,
    val question: String,
    val imageBase64: String? = null,
    val explanation: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val language: String = "English"
)
