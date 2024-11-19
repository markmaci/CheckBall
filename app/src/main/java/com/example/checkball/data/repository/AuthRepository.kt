package com.example.checkball.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.checkball.data.model.User
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
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

    fun logout() {
        auth.signOut()
    }

    suspend fun firebaseAuthWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user

            // Check if user exists in Firestore
            val userDocRef = db.collection("users").document(firebaseUser?.uid ?: "")
            val userDoc = userDocRef.get().await()
            val user = if (userDoc.exists()) {
                userDoc.toObject(User::class.java)
            } else {
                // If user doesn't exist, create a new user in Firestore
                val newUser = User(
                    uid = firebaseUser?.uid ?: "",
                    email = firebaseUser?.email ?: ""
                )
                userDocRef.set(newUser).await()
                newUser
            }
            Result.success(user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
