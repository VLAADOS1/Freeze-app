package com.vlaados.freeze.features.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseDialog(viewModel: PurchaseViewModel = viewModel()) {
    var itemName by remember { mutableStateOf("") }
    var itemPrice by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { /* TODO */ },
        title = { Text("Суд Фризи") },
        text = {
            Column {
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("Название товара") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = itemPrice,
                    onValueChange = { itemPrice = it },
                    label = { Text("Цена") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Комментарий") }
                )
            }
        },
        confirmButton = {
            Button(onClick = { /* TODO: Send to AI */ }) {
                Text("Отправить")
            }
        },
        dismissButton = {
            Button(onClick = { /* TODO */ }) {
                Text("Отмена")
            }
        }
    )
}
