package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "passages")
data class PassageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val imagePath: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "phrases",
    foreignKeys = [
        ForeignKey(
            entity = PassageEntity::class,
            parentColumns = ["id"],
            childColumns = ["passageId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PhraseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val passageId: Long, // References PassageEntity
    val phrase: String,
    val meaning: String,
    val cuteExample: String,
    val exampleTranslation: String,
    val cuteContext: String,
    val isLearned: Boolean = false,
    val starRating: Int = 0, // 0 to 3 stars earned in self-review or quiz
    val timestamp: Long = System.currentTimeMillis()
)
