package com.example.checkball.data.repository

import com.example.checkball.data.model.User

import com.example.checkball.di.FirestoreService

class UserProfileRepository(private val firestoreService: FirestoreService) {


    suspend fun saveUserProfile(userProfile: User) {
        firestoreService.saveUserProfile(userProfile)
    }

    suspend fun getUserProfile(): User? {

        return firestoreService.getUserProfile()
    }
}
