package com.example.checkball.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.checkball.data.model.User
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {
    suspend fun signUp(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
            val user = User(
                uid = firebaseUser?.uid ?: "",
                email = firebaseUser?.email ?: ""
            )
            db.collection("users").document(user.uid).set(user).await()
            Result.success(user)
        } catch (e: FirebaseAuthUserCollisionException) {
            Result.failure(Exception("An account with this email already exists."))
        } catch (e: FirebaseAuthWeakPasswordException) {
            Result.failure(Exception("Password should be at least 6 characters."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
            val userDoc = db.collection("users").document(firebaseUser?.uid ?: "").get().await()
            val user = userDoc.toObject(User::class.java)
            Result.success(user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
