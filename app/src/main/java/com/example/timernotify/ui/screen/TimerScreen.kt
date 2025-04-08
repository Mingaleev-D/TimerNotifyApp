package com.example.timernotify.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun TimerScreen(
       viewModel: TimerViewModel = hiltViewModel()
) {
    val timeInput by viewModel.timeInput.collectAsState()
    val remainingTime by viewModel.remainingTime.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()

    Column(
           modifier = Modifier
               .fillMaxSize()
               .padding(24.dp),
           verticalArrangement = Arrangement.Center,
           horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
               value = timeInput,
               onValueChange = { viewModel.updateTimeInput(it) },
               label = { Text("Время (мин)") },
               keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Text(
               text = remainingTime,
               style = MaterialTheme.typography.headlineMedium
        )

        Row(
               horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                   onClick = { viewModel.startTimer() },
                   enabled = !isRunning
            ) {
                Text("Старт")
            }

            Button(
                   onClick = { viewModel.stopTimer() },
                   enabled = isRunning
            ) {
                Text("Стоп")
            }

            Button(
                   onClick = { viewModel.resetTimer() }
            ) {
                Text("Сброс")
            }
        }
    }
}
