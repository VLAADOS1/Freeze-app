package com.vlaados.freeze.features.purchase

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.vlaados.freeze.R
import com.vlaados.freeze.ui.BottomBarHeight
import com.vlaados.freeze.ui.theme.PrimaryBlue

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

                moveTo(size.width * 0.15f, bubbleHeight)
                lineTo(size.width * 0.2f, size.height)
                lineTo(size.width * 0.25f, bubbleHeight)
                close()
            }
            return Outline.Generic(path)
        }
    }
}

private sealed class DialogState {
    object Hidden : DialogState()
    object Initial : DialogState()

    object Breathing : DialogState()
    object Success : DialogState()
    object Freezer : DialogState()
}

@Composable
fun VerdictScreen(
    onBackClick: () -> Unit,
    onFreezeClick: () -> Unit,
    onDiscussClick: () -> Unit,
    onBreathingClick: (Int, String?, String?) -> Unit,
    onRightChoiceClick: (String) -> Unit,
    viewModel: PurchaseViewModel
) {
    var dialogState by remember { mutableStateOf<DialogState>(DialogState.Hidden) }
    val verdict by viewModel.verdict.collectAsState()
    val costInTime by viewModel.costInTime.collectAsState()

    if (dialogState != DialogState.Hidden) {
        Dialog(onDismissRequest = { dialogState = DialogState.Hidden }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(9, 73, 161)
            ) {
                when (dialogState) {
                    DialogState.Initial -> {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Давай перед покупкой обсудим её с тобой?",
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
                                        onDiscussClick()
                                        dialogState = DialogState.Hidden
                                    },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF4AADFF),
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text("В чат", fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { dialogState = DialogState.Breathing },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF4AADFF),
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text("Нет, хочу купить!", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    DialogState.Breathing -> {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Моя последняя попытка. Давай сделаем дыхательную практику, чтобы ты потом не жалел. Выбери время:",
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
                                    onClick = { onBreathingClick(10, verdict?.text_purchased, verdict?.text_rejected) },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF4AADFF),
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text("10 секунд", fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { onBreathingClick(30, verdict?.text_purchased, verdict?.text_rejected) },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF4AADFF),
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text("30 секунд", fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { onBreathingClick(60, verdict?.text_purchased, verdict?.text_rejected) },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF4AADFF),
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text("1 минута", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    DialogState.Freezer -> {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "На сколько заморозим, чтобы остыть?",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            val options = verdict?.freeze_options ?: listOf(
                                FreezeOption("1 час", 3600),
                                FreezeOption("24 часа", 86400),
                                FreezeOption("3 дня", 259200),
                                FreezeOption("1 неделя", 604800)
                            )
                            
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                options.forEach { option ->
                                    Button(
                                        onClick = { 
                                            viewModel.freezeItem(option.duration_seconds) {
                                                onFreezeClick()
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().height(56.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF4AADFF),
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Text(option.label, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                    else -> {}
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(32.dp))
                }
                Text(
                    text = "Вердикт Фризи",
                    color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(15.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .background(Color.White, speechBubbleShape(16.dp, 15.dp))
                        .padding(start = 10.dp, end = 10.dp, top = 10.dp, bottom = 10.dp + 15.dp)
                ) {
                   val type = verdict?.verdict_type
                   
                   if (type == "unclear") {
                       Text(
                           modifier = Modifier.fillMaxWidth(),
                           textAlign = TextAlign.Center,
                           text = "Что-то странное...",
                           color = Color.Gray,
                           style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                       )
                   } else if (type == "rational") {
                       Text(
                           modifier = Modifier.fillMaxWidth(),
                           textAlign = TextAlign.Center,
                           text = "Кажется это разумная покупка!",
                           color = PrimaryBlue,
                           style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                       )
                   } else if (costInTime != null) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(color = Color.Black, fontSize = 16.sp)) {
                                    append("Это стоит ")
                                }
                                withStyle(style = SpanStyle(color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 20.sp)) {
                                    append(costInTime!!)
                                }
                                withStyle(style = SpanStyle(color = Color.Black, fontSize = 16.sp)) {
                                    append(" твоей работы!")
                                }
                            },
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        )
                   } else if (type == "impulsive") {
                       Text(
                           modifier = Modifier.fillMaxWidth(),
                           textAlign = TextAlign.Center,
                           text = "Если бы ты указал доход, я бы смог тебя лучше замотивировать...",
                           color = Color(0xFFE6A23C), 
                           style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                       )
                   } else {
                       // Loading or default
                       Text(
                           modifier = Modifier.fillMaxWidth(),
                           textAlign = TextAlign.Center,
                           text = "Анализирую...",
                           color = PrimaryBlue,
                           style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                       )
                   }
                }

                Spacer(modifier = Modifier.height(1.dp))

                Image(
                    painter = painterResource(id = R.drawable.freezesyd),
                    contentDescription = "Фризи выносит вердикт",
                    modifier = Modifier.size(200.dp)
                )

                Spacer(modifier = Modifier.height(1.dp))

                var isExpanded by remember { mutableStateOf(false) }
                val rotationAngle by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "rotation")

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .animateContentSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { isExpanded = !isExpanded }
                        .padding(10.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (verdict?.verdict_type == "unclear")
                                "Что-то странное... Кажется, вы ввели какой-то бред или случайный набор букв. Если я ошибаюсь, пожалуйста, попробуйте заполнить форму заново."
                            else
                                verdict?.comment ?: "Загрузка вердикта...",
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(
                            imageVector = Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Свернуть" else "Развернуть",
                            tint = Color.White,
                            modifier = Modifier.rotate(rotationAngle)
                        )
                    }
                }

                if (verdict?.verdict_type == "unclear" || verdict?.verdict_type == "rational") {
                     Spacer(modifier = Modifier.height(20.dp))
                     Button(
                        onClick = { 
                            val msg = verdict?.text_rejected ?: "Мудрое решение! Горжусь тобой. Продолжай в том же духе!"
                            onRightChoiceClick(msg)
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4AADFF),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Спасибо!", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Spacer(modifier = Modifier.height(5.dp))
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(-7.dp)
                    ) {
                        SelectableImage(drawableId = R.drawable.right, contentDescription = "Ты прав, Фризи!", onClick = { 
                            val msg = verdict?.text_rejected ?: "Мудрое решение! Горжусь тобой. Продолжай в том же духе!"
                            onRightChoiceClick(msg)
                        }, modifier = Modifier.scale(0.85f))
                        SelectableImage(drawableId = R.drawable.frizer, contentDescription = "В морозилку", onClick = { dialogState = DialogState.Freezer }, modifier = Modifier.scale(0.85f))
                        SelectableImage(drawableId = R.drawable.says, contentDescription = "Давай обсудим", onClick = onDiscussClick, modifier = Modifier.scale(0.85f))
                    }

                    Spacer(modifier = Modifier.height(0.dp))
    
                    SelectableImage(drawableId = R.drawable.dont, contentDescription = "Я всё равно хочу", onClick = { dialogState = DialogState.Initial })
                }

                Spacer(modifier = Modifier.height(BottomBarHeight))
            }
        }
    }
}

@Composable
fun SelectableImage(
    @DrawableRes drawableId: Int,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 1.1f else 1.0f, label = "image_scale_animation")

    Image(
        painter = painterResource(id = drawableId),
        contentDescription = contentDescription,
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    )
}
