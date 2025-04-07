package com.example.timernotify.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay

/**
 * UI-компонент, отображающий интерфейс таймера:
 * - текущий отсчёт времени
 * - поле ввода
 * - кнопки управления (Старт / Стоп / Сброс)
 */
@Composable
fun TimerScreen(viewModel: TimerViewModel = hiltViewModel()) {
    val timeDisplay by viewModel.timeDisplay.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()

    Column(
           modifier = Modifier
               .fillMaxSize()
               .padding(16.dp),
           horizontalAlignment = Alignment.CenterHorizontally,
           verticalArrangement = Arrangement.Center
    ) {
        Text(
               text = timeDisplay,
               fontSize = 48.sp,
               modifier = Modifier.padding(bottom = 32.dp)
        )

            OutlinedTextField(
                   value = viewModel.inputMinutes,
                   onValueChange = { viewModel.inputMinutes = it.filter { char -> char.isDigit() } }, // Только цифры
                   label = { Text("Время в минутах") },
                   keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                   enabled = !isRunning, // Нельзя менять во время работы таймера
                   modifier = Modifier
                       .fillMaxWidth()
                       .padding(bottom = 16.dp)
            )

            Row(
                   modifier = Modifier.fillMaxWidth(),
                   horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { viewModel.startTimer() }, enabled = !isRunning) {
                    Text("Старт")
                }
                Button(onClick = { viewModel.stopTimer() }, enabled = isRunning) {
                    Text("Стоп")
                }
                Button(
                       onClick = { viewModel.resetTimer() },
                       enabled = !isRunning && timeDisplay != "00:00"
                ) { // Сброс доступен если не запущен и не 00:00
                    Text("Сброс")
                }
            }
        }
    }
    //    Column(
    //           modifier = Modifier
    //               .fillMaxSize()
    //               .padding(16.dp),
    //           horizontalAlignment = Alignment.CenterHorizontally,
    //           verticalArrangement = Arrangement.Center
    //    ) {
    //        Text(
    //               text = timeDisplay,
    //               fontSize = 48.sp,
    //               modifier = Modifier.padding(bottom = 32.dp)
    //        )
    //
    //        OutlinedTextField(
    //               value = viewModel.inputMinutes,
    //               onValueChange = { viewModel.inputMinutes = it.filter { char -> char.isDigit() } }, // Только цифры
    //               label = { Text("Время в минутах") },
    //               keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    //               enabled = !isRunning, // Нельзя менять во время работы таймера
    //               modifier = Modifier
    //                   .fillMaxWidth()
    //                   .padding(bottom = 16.dp)
    //        )
    //
    //        Row (
    //               modifier = Modifier.fillMaxWidth(),
    //               horizontalArrangement = Arrangement.SpaceEvenly
    //        ) {
    //            Button(onClick = { viewModel.startTimer() }, enabled = !isRunning) {
    //                Text("Старт")
    //            }
    //            Button(onClick = { viewModel.stopTimer() }, enabled = isRunning) {
    //                Text("Стоп")
    //            }
    //            Button(onClick = { viewModel.resetTimer() }, enabled = !isRunning && timeDisplay != "00:00") { // Сброс доступен если не запущен и не 00:00
    //                Text("Сброс")
    //            }
    //        }
    //    }
