package com.example.data

import kotlinx.coroutines.flow.Flow

class DataRepository(private val passageDao: PassageDao) {

    val allPassages: Flow<List<PassageEntity>> = passageDao.getAllPassages()
    val allPhrases: Flow<List<PhraseEntity>> = passageDao.getAllPhrases()

    fun getPhrasesForPassage(passageId: Long): Flow<List<PhraseEntity>> {
        return passageDao.getPhrasesForPassage(passageId)
    }

    suspend fun getPassageById(id: Long): PassageEntity? {
        return passageDao.getPassageById(id)
    }

    suspend fun getPhrasesForPassageSync(passageId: Long): List<PhraseEntity> {
        return passageDao.getPhrasesForPassageSync(passageId)
    }

    suspend fun savePassageAndPhrases(
        title: String,
        content: String,
        imagePath: String?,
        extractedPhrases: List<ExtractedPhraseJson>
    ): Long {
        // Insert passage
        val passage = PassageEntity(
            title = title,
            content = content,
            imagePath = imagePath
        )
        val passageId = passageDao.insertPassage(passage)

        // Maps JSON phrases to Entity phrases with the foreign passageId
        val entities = extractedPhrases.map {
            PhraseEntity(
                passageId = passageId,
                phrase = it.phrase,
                meaning = it.meaning,
                cuteExample = it.cuteExample,
                exampleTranslation = it.exampleTranslation,
                cuteContext = it.cuteContext,
                isLearned = false,
                starRating = 0
            )
        }
        passageDao.insertPhrases(entities)
        return passageId
    }

    suspend fun deletePassage(passage: PassageEntity) {
        passageDao.deletePassage(passage)
    }

    suspend fun updatePhrase(phrase: PhraseEntity) {
        passageDao.updatePhrase(phrase)
    }

    suspend fun getRandomUnlearnedPhrasesSync(): List<PhraseEntity> {
        return passageDao.getRandomUnlearnedPhrasesSync()
    }

    suspend fun getRandomPhrasesSync(limit: Int): List<PhraseEntity> {
        return passageDao.getRandomPhrasesSync(limit)
    }
}

// Convenient transfer object matching our API responses
data class ExtractedPhraseJson(
    val phrase: String,
    val meaning: String,
    val cuteExample: String,
    val exampleTranslation: String,
    val cuteContext: String
)
