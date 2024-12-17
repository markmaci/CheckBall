package com.example.checkball.ui.screen

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.checkball.viewmodel.Place
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.checkball.viewmodel.UserProfileViewModel
import androidx.compose.foundation.shape.RoundedCornerShape

data class Post(
    val id: String = "",
    val caption: String = "",
    val description: String = "",
    val likes: Int = 0,
    val authorId: String = "",
    var authorDisplayName: String = ""
)

@Composable
fun HighlightsDetailsCard(court: Place?, onBack: () -> Unit, userProfileViewModel: UserProfileViewModel) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"

    var showForm by remember { mutableStateOf(false) }
    var caption by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var selectedUserId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(court) {
        court?.let {
            fetchPostsWithAuthors(it, firestore) { fetchedPosts ->
                posts = fetchedPosts
            }
        }
    }

    if (selectedUserId != null) {
        GuestUserProfileDialog(
            userId = selectedUserId!!,
            onDismiss = { selectedUserId = null },
            userProfileViewModel = userProfileViewModel
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF8B4513))
            }
            Text(
                text = court?.name ?: "Court Details",
                style = MaterialTheme.typography.titleLarge.copy(fontFamily = lacquierRegular),
                color = Color(0xFF8B4513),
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
            IconButton(onClick = { showForm = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Post", tint = Color(0xFF8B4513))
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(posts) { post ->
                PostCard(
                    post = post,
                    onAuthorClick = { userId ->
                        selectedUserId = userId
                    },
                    onLike = { likedPost, liked ->
                        toggleLike(court, likedPost, firestore, context, currentUserId, liked) {
                            court?.let {
                                fetchPostsWithAuthors(it, firestore) { updatedPosts ->
                                    posts = updatedPosts
                                }
                            }
                        }
                    }
                )
            }
        }

        if (showForm) {
            AddPostForm(
                onDismiss = { showForm = false },
                onSubmit = { captionText, descriptionText ->
                    if (court != null) {
                        val user = FirebaseAuth.getInstance().currentUser
                        val userId = user?.uid ?: "anonymous"
                        val displayName = user?.displayName ?: "Anonymous"

                        submitPostToFirestore(
                            court, captionText, descriptionText, firestore, context, userId, displayName
                        ) {
                            fetchPostsWithAuthors(court, firestore) { updatedPosts ->
                                posts = updatedPosts
                            }
                        }
                    }
                    showForm = false
                }
            )
        }
    }
}

@Composable
fun PostCard(
    post: Post,
    onAuthorClick: (String) -> Unit,
    onLike: (Post, Boolean) -> Unit
) {
    var liked by remember { mutableStateOf(false) }
    var likeCount by remember { mutableStateOf(post.likes) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2EFDE))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = post.caption,
                    style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFF8B4513))
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = post.description,
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.DarkGray)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { onAuthorClick(post.authorId) }) {
                    Text(
                        text = "By ${post.authorDisplayName}",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        liked = !liked
                        likeCount = if (liked) likeCount + 1 else likeCount - 1
                        onLike(post, liked)
                    }) {
                        Icon(
                            imageVector = if (liked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (liked) Color.Red else Color.Gray
                        )
                    }
                    Text(
                        text = "$likeCount likes",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray)
                    )
                }
            }
        }
    }
}


private fun fetchPostsWithAuthors(
    court: Place,
    firestore: FirebaseFirestore,
    onPostsFetched: (List<Post>) -> Unit
) {
    val parkId = "${court.location.latitude},${court.location.longitude}"
    firestore.collection("parks")
        .document(parkId)
        .collection("socialDetails")
        .get()
        .addOnSuccessListener { snapshot ->
            val posts = mutableListOf<Post>()
            val usersToFetch = mutableSetOf<String>()

            for (doc in snapshot.documents) {
                val post = Post(
                    id = doc.id,
                    caption = doc.getString("caption") ?: "",
                    description = doc.getString("description") ?: "",
                    likes = (doc.getLong("likes") ?: 0).toInt(),
                    authorId = doc.getString("userId") ?: "anonymous"
                )
                posts.add(post)
                usersToFetch.add(post.authorId)
            }

            fetchUserProfiles(usersToFetch.toList(), firestore) { userProfiles ->
                posts.forEach { post ->
                    post.authorDisplayName = userProfiles[post.authorId] ?: "Unknown"
                }
                onPostsFetched(posts)
            }
        }
}

private fun fetchUserProfiles(
    userIds: List<String>,
    firestore: FirebaseFirestore,
    onProfilesFetched: (Map<String, String>) -> Unit
) {
    val profiles = mutableMapOf<String, String>()
    userIds.forEach { userId ->
        firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { doc ->
                profiles[userId] = doc.getString("displayName") ?: "Unknown"
                if (profiles.size == userIds.size) {
                    onProfilesFetched(profiles)
                }
            }
            .addOnFailureListener {
                profiles[userId] = "Unknown"
                if (profiles.size == userIds.size) {
                    onProfilesFetched(profiles)
                }
            }
    }
}

private fun submitPostToFirestore(
    court: Place,
    caption: String,
    description: String,
    firestore: FirebaseFirestore,
    context: Context,
    userId: String,
    displayName: String,
    onSuccess: () -> Unit
) {
    val parkId = "${court.location.latitude},${court.location.longitude}"
    val postData = mapOf(
        "caption" to caption,
        "description" to description,
        "likes" to 0,
        "userId" to userId,
        "author" to displayName,
        "timestamp" to System.currentTimeMillis()
    )

    firestore.collection("parks")
        .document(parkId)
        .collection("socialDetails")
        .add(postData)
        .addOnSuccessListener {
            Toast.makeText(context, "Post submitted successfully!", Toast.LENGTH_SHORT).show()
            onSuccess()
        }
}

private fun toggleLike(
    court: Place?,
    post: Post,
    firestore: FirebaseFirestore,
    context: Context,
    userId: String,
    liked: Boolean,
    onComplete: () -> Unit
) {
    if (court == null) return
    val parkId = "${court.location.latitude},${court.location.longitude}"
    val postRef = firestore.collection("parks")
        .document(parkId)
        .collection("socialDetails")
        .document(post.id)
    val likeRef = postRef.collection("likes").document(userId)

    if (liked) {
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(postRef)
            val currentLikes = snapshot.getLong("likes") ?: 0
            transaction.update(postRef, "likes", currentLikes + 1)
            transaction.set(likeRef, mapOf("userId" to userId))
        }.addOnSuccessListener { onComplete() }
    } else {
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(postRef)
            val currentLikes = snapshot.getLong("likes") ?: 0
            transaction.update(postRef, "likes", (currentLikes - 1).coerceAtLeast(0))
            transaction.delete(likeRef)
        }.addOnSuccessListener { onComplete() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPostForm(
    onDismiss: () -> Unit,
    onSubmit: (String, String) -> Unit
) {
    var caption by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(12.dp),
        containerColor = Color.White,
        title = {
            Text(
                text = "Add Post",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color(0xFF8B4513)
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    label = { Text("Caption") },
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFF8B4513),
                        cursorColor = Color(0xFF8B4513)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFF8B4513),
                        cursorColor = Color(0xFF8B4513)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { onSubmit(caption, description) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B4513)),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("Submit", color = Color.White)
                }
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF8B4513))
                ) {
                    Text("Cancel")
                }
            }
        }
    )
}


