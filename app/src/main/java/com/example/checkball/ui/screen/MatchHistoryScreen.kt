package com.example.checkball.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.checkball.viewmodel.MatchHistoryViewModel
import com.example.checkball.data.model.Match
import com.example.checkball.data.model.User
import com.example.checkball.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun MatchHistoryScreen(
    navController: NavController,
    viewModel: MatchHistoryViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val matchHistory by viewModel.matchHistory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val showDialog = remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        viewModel.fetchMatchHistory(userId)
    }

    if (isLoading) {
        CircularProgressIndicator(modifier = Modifier.fillMaxSize())
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Button(
                onClick = {
                    showDialog.value = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text("Add New Match")
            }

            LazyColumn {
                items(matchHistory) { match ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .shadow(4.dp, RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text("Opponent: ${match.opponent}")
                            Text("Date: ${match.date}")
                            Text("Points Scored: ${match.pointsScored}")
                            Text("Assists: ${match.assists}")
                            Text("Rebounds: ${match.rebounds}")
                            Text("Result: ${match.result}")

                            Button(
                                onClick = {
                                    navController.navigate("matchDetails/${match.userId}")
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("View Details")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog.value) {
        MatchFormDialog(
            onDismiss = { showDialog.value = false },
            onSubmit = { newMatch ->
                viewModel.addMatch(newMatch, User(uid = userId))
                showDialog.value = false
            },
            userId = userId
        )
    }
}

@Composable
fun MatchFormDialog(
    onDismiss: () -> Unit,
    onSubmit: (Match) -> Unit,
    userId: String
) {
    var opponent by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var pointsScored by remember { mutableStateOf("") }
    var assists by remember { mutableStateOf("") }
    var rebounds by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var score by remember { mutableStateOf("") }
    var showToast by remember { mutableStateOf(false) }

    val context = LocalContext.current

    if (showToast) {
        LaunchedEffect(Unit) {
            Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            showToast = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Match") },
        text = {
            Column {
                TextField(
                    value = opponent,
                    onValueChange = { opponent = it },
                    label = { Text("Opponent") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = pointsScored,
                    onValueChange = { pointsScored = it },
                    label = { Text("Points Scored") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = assists,
                    onValueChange = { assists = it },
                    label = { Text("Assists") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = rebounds,
                    onValueChange = { rebounds = it },
                    label = { Text("Rebounds") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = result,
                    onValueChange = { result = it },
                    label = { Text("Result") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = score,
                    onValueChange = { score = it },
                    label = { Text("Score") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (opponent.isNotEmpty() && date.isNotEmpty() && pointsScored.isNotEmpty() &&
                        assists.isNotEmpty() && rebounds.isNotEmpty() && result.isNotEmpty() && score.isNotEmpty()) {
                        val newMatch = Match(
                            userId = userId,
                            date = date,
                            opponent = opponent,
                            result = result,
                            score = score,
                            pointsScored = pointsScored.toInt(),
                            assists = assists.toInt(),
                            rebounds = rebounds.toInt()
                        )
                        onSubmit(newMatch)
                    } else {
                        showToast = true
                    }
                }
            ) {
                Text("Save Match")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
