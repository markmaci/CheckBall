package com.example.checkball.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.checkball.viewmodel.Place
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourtDetailsBottomSheet(
    court: Place,
    onDismiss: () -> Unit
) {
    // Initialize the sheet state so it starts half-expanded rather than at the peek height
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val scope = rememberCoroutineScope()

    // ModalBottomSheet will already have a drag handle, so no close button necessary.
    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = Color(0xFFF2EFDE)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 500.dp)
                .padding(16.dp)
        ) {
            // Just showing the court name for now
            Text(
                text = court.name,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Placeholder text or spacing to ensure visible height
            // (We’ll add actual features later.)
            Text(
                text = "More details coming soon...",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}