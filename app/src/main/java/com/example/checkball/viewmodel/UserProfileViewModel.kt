package com.example.checkball.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.checkball.di.UserProfile
import com.example.checkball.repository.UserProfileRepository
import com.example.checkball.di.FirestoreService
import kotlinx.coroutines.launch

class UserProfileViewModel(
    private val userProfileRepository: UserProfileRepository = UserProfileRepository(FirestoreService())
) : ViewModel() {

    private val _saveProfileStatus = mutableStateOf<SaveProfileStatus>(SaveProfileStatus.Idle)
    val saveProfileStatus: State<SaveProfileStatus> = _saveProfileStatus

    fun saveUserProfile(userProfile: UserProfile) {
        _saveProfileStatus.value = SaveProfileStatus.Loading
        viewModelScope.launch {
            try {
                userProfileRepository.saveUserProfile(userProfile)
                _saveProfileStatus.value = SaveProfileStatus.Success
            } catch (e: Exception) {
                _saveProfileStatus.value = SaveProfileStatus.Failure(e.message ?: "An unknown error occurred")
            }
        }
    }

    fun getUserProfile(callback: (UserProfile?) -> Unit) {
        viewModelScope.launch {
            try {
                val profile = userProfileRepository.getUserProfile()
                callback(profile)
            } catch (e: Exception) {
                callback(null)
            }
        }
    }
}

sealed class SaveProfileStatus {
    data object Idle : SaveProfileStatus()
    data object Loading : SaveProfileStatus()
    data object Success : SaveProfileStatus()
    data class Failure(val message: String) : SaveProfileStatus()
}
