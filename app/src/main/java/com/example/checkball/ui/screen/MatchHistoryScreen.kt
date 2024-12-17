package com.example.checkball.ui.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.checkball.R
import com.example.checkball.viewmodel.MatchHistoryViewModel
import com.example.checkball.data.model.Match
import com.example.checkball.data.model.User
import com.example.checkball.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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

    val context = LocalContext.current

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFFFFA500))
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF2EFDE)) // Set the background to tan
                .padding(16.dp)
        ) {
            Button(
                onClick = {
                    showDialog.value = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(bottom = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add New Game",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add New Match", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                }
            }

            if (matchHistory.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No matches recorded yet.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(matchHistory) { match ->
                        MatchCard(match = match)
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
                Toast.makeText(context, "Match added successfully!", Toast.LENGTH_SHORT).show()
            },
            userId = userId
        )
    }
}

@Composable
fun MatchCard(match: Match) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .shadow(4.dp, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2EFDE))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = if (match.result == "Win") Color(0xFF4CAF50) else Color(0xFFF44336),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(
                        id = if (match.result == "Win") R.drawable.win_icon else R.drawable.loss_icon
                    ),
                    contentDescription = if (match.result == "Win") "Win" else "Loss",
                    modifier = Modifier.size(24.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Opponent: ${match.opponent}",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Date: ${match.date}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Text(
                        text = "You: ${match.pointsScored} pts",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${match.opponent}: ${match.opponentPointsScored} pts",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Text(
                        text = "Assists: ${match.assists}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Rebounds: ${match.rebounds}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchFormDialog(
    onDismiss: () -> Unit,
    onSubmit: (Match) -> Unit,
    userId: String
) {
    var opponent by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var pointsScored by remember { mutableStateOf("") }
    var opponentPointsScored by remember { mutableStateOf("") }
    var assists by remember { mutableStateOf("") }
    var rebounds by remember { mutableStateOf("") }
    var showToast by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = android.app.DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDay ->
            date = String.format("%02d/%02d/%04d", selectedMonth + 1, selectedDay, selectedYear)
        },
        year,
        month,
        day
    )

    LaunchedEffect(Unit) {
        date = String.format("%02d/%02d/%04d", month + 1, day, year)
    }

    if (showToast) {
        LaunchedEffect(Unit) {
            Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            showToast = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add New Match",
                color = Color.Black,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column {
                TextField(
                    value = opponent,
                    onValueChange = { opponent = it },
                    label = { Text("Opponent") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.Transparent
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { datePickerDialog.show() }) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Select Date",
                                tint = Color(0xFFFFA500)
                            )
                        }
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = Color.Transparent,
                        focusedBorderColor = Color(0xFFFFA500),
                        unfocusedBorderColor = Color.Gray,
                        cursorColor = Color(0xFFFFA500)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = pointsScored,
                    onValueChange = { pointsScored = it.filter { it.isDigit() } },
                    label = { Text("Your Points Scored") },
                    modifier = Modifier
                        .fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.Transparent
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = opponentPointsScored,
                    onValueChange = { opponentPointsScored = it.filter { it.isDigit() } },
                    label = { Text("Opponent Points Scored") },
                    modifier = Modifier
                        .fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.Transparent
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = assists,
                    onValueChange = { assists = it.filter { it.isDigit() } },
                    label = { Text("Assists") },
                    modifier = Modifier
                        .fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.Transparent
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = rebounds,
                    onValueChange = { rebounds = it.filter { it.isDigit() } },
                    label = { Text("Rebounds") },
                    modifier = Modifier
                        .fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (opponent.isNotEmpty() &&
                        date.isNotEmpty() &&
                        pointsScored.isNotEmpty() &&
                        opponentPointsScored.isNotEmpty() &&
                        assists.isNotEmpty() &&
                        rebounds.isNotEmpty()
                    ) {
                        val newMatch = Match(
                            userId = userId,
                            date = date,
                            opponent = opponent,
                            pointsScored = pointsScored.toInt(),
                            opponentPointsScored = opponentPointsScored.toInt(),
                            assists = assists.toInt(),
                            rebounds = rebounds.toInt()
                        )
                        onSubmit(newMatch)
                    } else {
                        showToast = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
            ) {
                Text("Save Match", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFFA500))
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color(0xFFF2EFDE) // Set the dialog background to tan
    )
}