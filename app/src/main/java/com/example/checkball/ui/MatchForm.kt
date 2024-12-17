package com.example.checkball.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.checkball.data.model.Match

@Composable
fun AddMatchForm(
    onDismiss: () -> Unit,
    onSave: (Match) -> Unit
) {
    var opponent by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var pointsScored by remember { mutableStateOf("") }
    var assists by remember { mutableStateOf("") }
    var rebounds by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White)
    ) {
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
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = assists,
            onValueChange = { assists = it },
            label = { Text("Assists") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = rebounds,
            onValueChange = { rebounds = it },
            label = { Text("Rebounds") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = result,
            onValueChange = { result = it },
            label = { Text("Result") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text("Cancel")
            }

            Button(
                onClick = {
                    val match = Match(
                        opponent = opponent,
                        date = date,
                        pointsScored = pointsScored.toIntOrNull() ?: 0,
                        assists = assists.toIntOrNull() ?: 0,
                        rebounds = rebounds.toIntOrNull() ?: 0,
                        result = result
                    )
                    onSave(match)
                    onDismiss()
                }
            ) {
                Text("Save")
            }
        }
    }
}
