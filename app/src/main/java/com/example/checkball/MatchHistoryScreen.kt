package com.example.checkball

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.Alignment

@Composable
fun MatchHistoryScreen(
    userId: String,
    onBackClick: () -> Unit,
    matchHistoryViewModel: MatchHistoryViewModel
) {
    val matches = matchHistoryViewModel.matchHistory.collectAsState().value
    val isLoading = matchHistoryViewModel.isLoading.collectAsState().value

    LaunchedEffect(userId) {
        matchHistoryViewModel.fetchMatchHistory(userId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White)
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.Black
            )
        }

        Text(
            text = "Match History",
            style = MaterialTheme.typography.titleLarge.copy(color = Color.Black),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            textAlign = TextAlign.Center
        )

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                matches.forEach { match ->
                    MatchCard(match)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun MatchCard(match: Match) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color(0xFFF2F2F2), shape = RoundedCornerShape(12.dp))
            .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = match.date, style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black))
                Text(text = "Opponent: ${match.opponent}", style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black))
                Text(text = "Result: ${match.result} (${match.score})", style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "Points Scored: ${match.pointsScored}", style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black))
                Text(text = "Assists: ${match.assists}", style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black))
                Text(text = "Rebounds: ${match.rebounds}", style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black))
            }
        }
    }
}
