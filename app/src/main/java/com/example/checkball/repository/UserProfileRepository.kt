package com.example.checkball.repository

import com.example.checkball.di.UserProfile
import com.example.checkball.di.FirestoreService

class UserProfileRepository(private val firestoreService: FirestoreService) {

    suspend fun saveUserProfile(userProfile: UserProfile) {
        firestoreService.saveUserProfile(userProfile)
    }

    suspend fun getUserProfile(): UserProfile? {
        return firestoreService.getUserProfile()
    }
}
