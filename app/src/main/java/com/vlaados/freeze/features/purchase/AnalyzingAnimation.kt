package com.vlaados.freeze.features.purchase

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vlaados.freeze.R
import kotlinx.coroutines.delay

@Composable
fun AnalyzingAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    var loadingText by remember { mutableStateOf("Анализирую...") }

    LaunchedEffect(Unit) {
        val texts = listOf(
            "Смотрю в кошелек...",
            "Оцениваю риски...",
            "Спрашиваю у жабы...",
            "Вспоминаю твои цели...",
            "Анализирую...",
            "Почти готово..."
        )
        var index = 0
        while (true) {
            loadingText = texts[index]
            delay(2000)
            index = (index + 1) % texts.size
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.freezesyd), // Or another relevant icon
            contentDescription = "Analyzing",
            modifier = Modifier
                .size(80.dp)
                .scale(scale)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = loadingText,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
