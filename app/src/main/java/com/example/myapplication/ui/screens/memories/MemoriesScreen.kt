package com.example.myapplication.ui.screens.memories

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MemoriesScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "함께한 추억",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "여기에 추억을 기록합니다",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

