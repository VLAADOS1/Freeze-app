package com.vlaados.freeze.features.freezer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.vlaados.freeze.R
import com.vlaados.freeze.core.FrozenPurchase
import com.vlaados.freeze.ui.BottomBarHeight
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@Composable
fun FreezerScreen(
    viewModel: FreezerViewModel = hiltViewModel(),
    onPurchaseSuccess: () -> Unit,
    onPurchaseFailed: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.loadFrozenItems()
    }

    val frozenPurchases by viewModel.frozenPurchases.collectAsState()
    
    var dialogEvent by remember { mutableStateOf<FreezerViewModel.FreezeEvent?>(null) }
    
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            dialogEvent = event
        }
    }
    
    if (dialogEvent != null) {
        Dialog(onDismissRequest = { dialogEvent = null }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(9, 73, 161)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = when (dialogEvent) {
                            is FreezerViewModel.FreezeEvent.ShowSuccess -> "Молодец!"
                            is FreezerViewModel.FreezeEvent.ShowFailure -> "Не расстраивайся!"
                            else -> ""
                        },
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Text(
                        text = when (val e = dialogEvent) {
                            is FreezerViewModel.FreezeEvent.ShowSuccess -> e.message
                            is FreezerViewModel.FreezeEvent.ShowFailure -> e.message
                            else -> ""
                        },
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { dialogEvent = null },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4AADFF),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Понятно", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(10, 30, 116), Color(10, 118, 209))
                )
            )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(painter = painterResource(id = R.drawable.sneginka), contentDescription = null, modifier = Modifier.align(Alignment.TopStart).offset(x = 20.dp, y = (-10).dp).size(60.dp).alpha(0.3f).rotate(-15f))
            Image(painter = painterResource(id = R.drawable.sneginka), contentDescription = null, modifier = Modifier.align(Alignment.TopEnd).offset(x = (-30).dp, y = 20.dp).size(50.dp).alpha(0.3f).rotate(25f))
            Image(painter = painterResource(id = R.drawable.sneginka), contentDescription = null, modifier = Modifier.align(Alignment.CenterStart).offset(y = (-80).dp).size(40.dp).alpha(0.2f).rotate(10f))
            Image(painter = painterResource(id = R.drawable.sneginka), contentDescription = null, modifier = Modifier.align(Alignment.CenterEnd).offset(y = (-60).dp).size(70.dp).alpha(0.4f).rotate(-20f))
            Image(painter = painterResource(id = R.drawable.sneginka), contentDescription = null, modifier = Modifier.align(Alignment.BottomStart).offset(x = 40.dp, y = (-120).dp).size(45.dp).alpha(0.25f).rotate(5f))
            Image(painter = painterResource(id = R.drawable.sneginka), contentDescription = null, modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-40).dp, y = (-150).dp).size(55.dp).alpha(0.35f).rotate(45f))
        }
        if (frozenPurchases.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Пока товаров в заморозке нет...",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentPadding = PaddingValues(bottom = BottomBarHeight)
            ) {
                items(frozenPurchases, key = { it.id }) { purchase ->
                    FrozenPurchaseCard(
                        frozenPurchase = purchase,
                        onPurchaseSuccess = {
                            viewModel.onPurchaseAborted(purchase.id)
                        },
                        onPurchaseFailed = {
                            viewModel.onPurchaseConfirmed(purchase.id)
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun FrozenPurchaseCard(
    frozenPurchase: FrozenPurchase,
    onPurchaseSuccess: () -> Unit,
    onPurchaseFailed: () -> Unit
) {
    var timeLeft by remember { mutableStateOf(frozenPurchase.freezeUntil - System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (timeLeft > 0) {
            delay(1000)
            timeLeft = frozenPurchase.freezeUntil - System.currentTimeMillis()
        }
    }

    val totalDuration = frozenPurchase.freezeUntil - frozenPurchase.freezeStartedTimestamp
    val progress by animateFloatAsState(
        targetValue = if (totalDuration > 0) 1f - (timeLeft.toFloat() / totalDuration) else 0f,
        label = "progress"
    )

    val hours = TimeUnit.MILLISECONDS.toHours(timeLeft).coerceAtLeast(0)
    val minutes = (TimeUnit.MILLISECONDS.toMinutes(timeLeft) % 60).coerceAtLeast(0)
    val seconds = (TimeUnit.MILLISECONDS.toSeconds(timeLeft) % 60).coerceAtLeast(0)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.3f))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = frozenPurchase.purchase.name,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "${frozenPurchase.purchase.price} ₽",
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (timeLeft > 0) {
                    Text(
                        text = "Разморозка через: ${hours}ч ${minutes}м ${seconds}с",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text(
                        text = "Разморозка прошла, что ты решил?",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .clickable { onPurchaseSuccess() }
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Я передумал",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .clickable { onPurchaseFailed() }
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Я сорвался",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
