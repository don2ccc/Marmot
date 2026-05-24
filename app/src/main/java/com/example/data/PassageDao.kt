package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PassageDao {

    // Passages
    @Query("SELECT * FROM passages ORDER BY timestamp DESC")
    fun getAllPassages(): Flow<List<PassageEntity>>

    @Query("SELECT * FROM passages WHERE id = :id")
    suspend fun getPassageById(id: Long): PassageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPassage(passage: PassageEntity): Long

    @Delete
    suspend fun deletePassage(passage: PassageEntity)

    @Query("DELETE FROM passages")
    suspend fun deleteAllPassages()

    // Phrases
    @Query("SELECT * FROM phrases ORDER BY timestamp DESC")
    fun getAllPhrases(): Flow<List<PhraseEntity>>

    @Query("SELECT * FROM phrases WHERE passageId = :passageId ORDER BY id ASC")
    fun getPhrasesForPassage(passageId: Long): Flow<List<PhraseEntity>>

    @Query("SELECT * FROM phrases WHERE passageId = :passageId ORDER BY id ASC")
    suspend fun getPhrasesForPassageSync(passageId: Long): List<PhraseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhrases(phrases: List<PhraseEntity>)

    @Update
    suspend fun updatePhrase(phrase: PhraseEntity)

    @Query("SELECT * FROM phrases WHERE isLearned = 0 ORDER BY RANDOM() LIMIT 5")
    suspend fun getRandomUnlearnedPhrasesSync(): List<PhraseEntity>

    @Query("SELECT * FROM phrases ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomPhrasesSync(limit: Int): List<PhraseEntity>
}
