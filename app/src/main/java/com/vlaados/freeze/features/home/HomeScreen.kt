package com.vlaados.freeze.features.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vlaados.freeze.R
import com.vlaados.freeze.ui.BottomBarHeight
import com.vlaados.freeze.ui.theme.FreezeTheme
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onPurchaseClick: () -> Unit,
    successMessage: String? = null
) {
    val savedAmount by viewModel.savedAmount.collectAsState()
    val goalProgress by viewModel.goalProgress.collectAsState()
    val daysLeft by viewModel.daysLeft.collectAsState()
    val goalName by viewModel.goalName.collectAsState()
    
    var showSuccessDialog by remember(successMessage) { mutableStateOf(!successMessage.isNullOrBlank()) }

    if (showSuccessDialog && !successMessage.isNullOrBlank()) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showSuccessDialog = false }) {
             androidx.compose.material3.Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(9, 73, 161)
            ) {
                 Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = successMessage,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    androidx.compose.material3.Button(
                        onClick = { showSuccessDialog = false },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4AADFF),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Отлично!", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    val mascotSize = 300.dp
    val buyButtonImageScale = 1.3f
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) buyButtonImageScale * 1.1f else buyButtonImageScale,
        label = "scale",
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 500f)
    )

    val timePeriods = remember { listOf("День", "Месяц", "Год", "Всё время") }
    var selectedPeriodIndex by remember { mutableStateOf(1) }

    LaunchedEffect(selectedPeriodIndex) {
        viewModel.updatePeriod(timePeriods[selectedPeriodIndex])
    }

    val timePeriodInteractionSource = remember { MutableInteractionSource() }
    val isTimePeriodPressed by timePeriodInteractionSource.collectIsPressedAsState()
    val animatedTimePeriodScale by animateFloatAsState(
        targetValue = if (isTimePeriodPressed) 1.1f else 1.0f,
        label = "timePeriodScale",
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 500f)
    )

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
                .padding(16.dp)
                .padding(bottom = BottomBarHeight),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Freeze Logo",
                    modifier = Modifier.fillMaxWidth(0.4f)
                )
            }
            Box(
                modifier = Modifier.height(264.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.freezeinfo),
                    contentDescription = "Freezie Mascot",
                    modifier = Modifier.size(mascotSize)
                )
            }

            goalProgress?.let { goal ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Цель: ${goalName ?: "Моя цель"}", fontWeight = FontWeight.Bold, color = Color.White)
                        val progress = goal.first
                        val percentText = if (progress > 0.0 && progress < 0.01) {
                            String.format(java.util.Locale.US, "%.3f%%", progress * 100)
                        } else {
                            "${(progress * 100).toInt()}%"
                        }
                        Text(percentText, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { goal.first },
                        modifier = Modifier.fillMaxWidth().height(16.dp).clip(RoundedCornerShape(8.dp)),
                        color = Color(0xFF4AADFF),
                        trackColor = Color.White.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("сэкономил: ${goal.second} ₽", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                        daysLeft?.let {
                            val daysString = when {
                                it % 100 in 11..14 -> "$it дней"
                                it % 10 == 1 -> "$it день"
                                it % 10 in 2..4 -> "$it дня"
                                else -> "$it дней"
                            }
                            Text("осталось $daysString", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                        }
                    }
                }
            } ?: run {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Установите цель в настройках!", color = Color.White.copy(alpha = 0.7f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            AutoResizingText(
                text = "${savedAmount.toInt()} ₽",
                maxFontSize = 64.sp,
                minFontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Box(
                modifier = Modifier
                    .scale(animatedTimePeriodScale)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.2f))
                    .clickable(
                        interactionSource = timePeriodInteractionSource,
                        indication = null,
                        onClick = {
                            selectedPeriodIndex = (selectedPeriodIndex + 1) % timePeriods.size
                        }
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = buildAnnotatedString {
                        append("Сэкономлено за: ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(timePeriods[selectedPeriodIndex])
                        }
                    },
                    color = Color.White,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(300.dp)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = onPurchaseClick
                        )
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.buy),
                        contentDescription = "Хочу купить",
                        modifier = Modifier
                            .size(250.dp)
                            .scale(animatedScale)
                            .offset(y = 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AutoResizingText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    maxFontSize: TextUnit = 64.sp,
    minFontSize: TextUnit = 12.sp
) {
    var fontSize by remember { mutableStateOf(maxFontSize) }
    var currentText by remember { mutableStateOf(text) }

    if (text != currentText) {
        currentText = text
        fontSize = maxFontSize
    }

    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        softWrap = false,
        maxLines = 1,
        onTextLayout = { result ->
            if (result.didOverflowWidth) {
                val neededWidth = result.multiParagraph.maxIntrinsicWidth
                val availableWidth = result.size.width.toFloat()
                
                if (neededWidth > availableWidth && fontSize > minFontSize) {
                    val scale = availableWidth / neededWidth
                    val newSize = fontSize * scale * 0.95f
                    fontSize = if (newSize < minFontSize) minFontSize else newSize
                }
            }
        }
    )
}
