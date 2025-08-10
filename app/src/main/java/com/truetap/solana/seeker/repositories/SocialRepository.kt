package com.truetap.solana.seeker.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class SocialNote(val txId: String, val message: String, val private: Boolean)
data class SocialLike(val txId: String, val user: String)
data class SocialComment(val txId: String, val user: String, val text: String)

@Singleton
class SocialRepository @Inject constructor() {
    private val notes = MutableStateFlow<List<SocialNote>>(emptyList())
    private val likes = MutableStateFlow<List<SocialLike>>(emptyList())
    private val comments = MutableStateFlow<List<SocialComment>>(emptyList())

    val notesFlow: Flow<List<SocialNote>> = notes.asStateFlow()
    val likesFlow: Flow<List<SocialLike>> = likes.asStateFlow()
    val commentsFlow: Flow<List<SocialComment>> = comments.asStateFlow()

    fun addNote(txId: String, message: String, private: Boolean) {
        notes.value = notes.value + SocialNote(txId, message, private)
    }

    fun like(txId: String, user: String) {
        if (likes.value.any { it.txId == txId && it.user == user }) return
        likes.value = likes.value + SocialLike(txId, user)
    }

    fun comment(txId: String, user: String, text: String) {
        comments.value = comments.value + SocialComment(txId, user, text)
    }
}


