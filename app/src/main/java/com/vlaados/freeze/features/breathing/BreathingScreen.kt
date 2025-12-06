package com.vlaados.freeze.features.breathing

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import com.vlaados.freeze.R
import com.vlaados.freeze.ui.BottomBarHeight
import com.vlaados.freeze.ui.theme.PrimaryBlue
import kotlinx.coroutines.delay

private fun speechBubbleShape(cornerRadius: Dp, tipSize: Dp): Shape {
    return object : Shape {
        override fun createOutline(
            size: Size,
            layoutDirection: LayoutDirection,
            density: Density
        ): Outline {
            val cornerRadiusPx = with(density) { cornerRadius.toPx() }
            val tipSizePx = with(density) { tipSize.toPx() }

            val path = Path().apply {
                val bubbleHeight = size.height - tipSizePx
                addRoundRect(
                    RoundRect(
                        rect = Rect(Offset.Zero, Size(size.width, bubbleHeight)),
                        radiusX = cornerRadiusPx,
                        radiusY = cornerRadiusPx
                    )
                )
                moveTo(size.width * 0.4f, bubbleHeight)
                lineTo(size.width * 0.45f, size.height)
                lineTo(size.width * 0.5f, bubbleHeight)
                close()
            }
            return Outline.Generic(path)
        }
    }
}

@Composable
fun BreathingScreen(
    durationSeconds: Int,
    textPurchased: String?,
    textRejected: String?,
    monthlySavings: Double? = null,
    itemPrice: Double = 0.0,
    onIChangedMyMind: (String) -> Unit,
    onPurchaseConfirmed: (String) -> Unit
) {
    var timeLeft by remember { mutableStateOf(durationSeconds) }
    var showFinalDialog by remember { mutableStateOf(false) }
    var showBreakdownDialog by remember { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = timeLeft.toFloat() / durationSeconds.toFloat(),
        label = "progress"
    )

    LaunchedEffect(key1 = timeLeft) {
        if (timeLeft > 0) {
            delay(1000)
            timeLeft--
        } else {
            showFinalDialog = true
        }
    }

    if (showBreakdownDialog) {
        Dialog(onDismissRequest = { /* Prevent dismissing */ }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(9, 73, 161)
            ) {
                 Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val ms = monthlySavings ?: 0.0
                    val text = if (ms > 0) {
                        val dailySavings = ms / 30.0
                        val daysLost = if (dailySavings > 0) kotlin.math.round(itemPrice / dailySavings).toInt() else 0
                        val daysString = when {
                            daysLost % 100 in 11..14 -> "$daysLost дней"
                            daysLost % 10 == 1 -> "$daysLost день"
                            daysLost % 10 in 2..4 -> "$daysLost дня"
                            else -> "$daysLost дней"
                        }
                        "Вы откинете свою цель на $daysString, если совершите эту покупку."
                    } else {
                        "Если бы вы поделились, сколько вы откладываете, я бы замотивировал вас лучше... Вы все равно хотите сорваться?"
                    }

                    Text(
                        text = text,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = {
                                val msg = textRejected ?: "Отличный выбор! Ты спас свои деньги."
                                onIChangedMyMind(msg)
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4AADFF),
                                contentColor = Color.White
                            )
                        ) {
                            Text("Отказаться от покупки", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { 
                                val msg = textPurchased ?: "Не расстраивайся, в следующий раз все получится!" 
                                onPurchaseConfirmed(msg)
                            }, 
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4AADFF),
                                contentColor = Color.White
                            )
                        ) {
                            Text("Я сорвусь", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    } else if (showFinalDialog) {
        Dialog(onDismissRequest = { }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(9, 73, 161)
            ) {
                 Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Твоё финальное решение. Сделай выбор так, чтоб потом не жалеть.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                     Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = {
                                val msg = textRejected ?: "Отличный выбор! Ты спас свои деньги."
                                onIChangedMyMind(msg)
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4AADFF),
                                contentColor = Color.White
                            )
                        ) {
                            Text("Отказаться от покупки", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { 
                                showBreakdownDialog = true
                            }, 
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4AADFF),
                                contentColor = Color.White
                            )
                        ) {
                            Text("Я куплю", fontWeight = FontWeight.Bold)
                        }
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Остынь!",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(30.dp))

            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = R.drawable.freezechill),
                    contentDescription = "Freezie Chill Mascot",
                    modifier = Modifier
                        .size(400.dp)
                        .padding(top = 100.dp)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .zIndex(1f)
                        .background(Color.White, speechBubbleShape(12.dp, 15.dp))
                        .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 16.dp + 15.dp),
                ) {
                    Text(
                        text = "Дыши глубоко...\nОстынь и подумай ещё раз!",
                        textAlign = TextAlign.Center,
                        color = PrimaryBlue,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        lineHeight = 22.sp,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-35).dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "00:${timeLeft.toString().padStart(2, '0')}",
                    color = Color.White,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { 1f - progress },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    val msg = textRejected ?: "Отличный выбор! Ты спас свои деньги."
                    onIChangedMyMind(msg)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4AADFF),
                    contentColor = Color.White
                )
            ) {
                Text(
                    "Я передумал!",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(BottomBarHeight))
        }
    }
}
