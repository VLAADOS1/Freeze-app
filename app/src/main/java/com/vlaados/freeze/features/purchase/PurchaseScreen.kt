package com.vlaados.freeze.features.purchase

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.outlined.CurrencyRuble
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.core.content.ContextCompat
import com.vlaados.freeze.R
import com.vlaados.freeze.ui.BottomBarHeight
import com.vlaados.freeze.ui.theme.PrimaryBlue
import com.vlaados.freeze.ui.PriceVisualTransformation
import androidx.compose.material.icons.filled.Link
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester

private enum class InputMode {
    LINK,
    MANUAL
}

private class SpeechBubbleShape(private val cornerRadius: Dp, private val tipSize: Dp) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val tipSizePx = with(density) { tipSize.toPx() }
        val cornerRadiusPx = with(density) { cornerRadius.toPx() }

        val path = Path().apply {
            val rect = Rect(left = tipSizePx, top = 0f, right = size.width, bottom = size.height)
            addRoundRect(androidx.compose.ui.geometry.RoundRect(rect, cornerRadiusPx, cornerRadiusPx))

            moveTo(x = tipSizePx, y = rect.center.y - tipSizePx / 2)
            lineTo(x = 0f, y = rect.center.y)
            lineTo(x = tipSizePx, y = rect.center.y + tipSizePx / 2)
            close()
        }
        return Outline.Generic(path)
    }
}

@Composable
fun PurchaseScreen(
    onBackClick: () -> Unit,
    onVerdictClick: () -> Unit,
    viewModel: PurchaseViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    var productName by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var emotions by remember { mutableStateOf("") }
    var hasAction by remember { mutableStateOf(false) }
    var isPhotoAdded by remember { mutableStateOf(false) }
    var isListening by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListening = false
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val newText = results?.get(0)
            if (newText != null) {
                emotions = if (emotions.isNotBlank()) "$emotions $newText" else newText
            }
        }
    }

    fun launchSpeechRecognizer() {
        isListening = true
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Говорите...")
        }
        speechRecognizerLauncher.launch(intent)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            launchSpeechRecognizer()
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                isPhotoAdded = true
            }
        }
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 1.05f else 1.0f,
        label = "scale",
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 400f)
    )

    val photoButtonInteractionSource = remember { MutableInteractionSource() }
    val isPhotoButtonPressed by photoButtonInteractionSource.collectIsPressedAsState()
    val animatedPhotoButtonScale by animateFloatAsState(
        targetValue = if (isPhotoButtonPressed) 1.05f else 1.0f,
        label = "photoButtonScale",
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
                    text = "На суд Фризи!",
                    color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(48.dp)) // Balance IconButton space
            }

            Spacer(modifier = Modifier.height(16.dp))

            var inputMode by remember { mutableStateOf(InputMode.LINK) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    .padding(4.dp)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                color = if (inputMode == InputMode.LINK) Color.White.copy(alpha = 0.3f) else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { inputMode = InputMode.LINK },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Ссылка",
                            color = Color.White,
                            fontWeight = if (inputMode == InputMode.LINK) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                color = if (inputMode == InputMode.MANUAL) Color.White.copy(alpha = 0.3f) else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { inputMode = InputMode.MANUAL },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Ввод",
                            color = Color.White,
                            fontWeight = if (inputMode == InputMode.MANUAL) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Image(
                    painter = painterResource(id = R.drawable.freezewrite),
                    contentDescription = "Freezie Mascot",
                    modifier = Modifier
                        .size(100.dp)
                        .offset(y = 10.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White, SpeechBubbleShape(10.dp, 10.dp))
                        .padding(start = 10.dp + 10.dp, end = 10.dp, top = 10.dp, bottom = 10.dp)
                ) {
                    Text(
                        text = if (inputMode == InputMode.LINK) "Кидай ссылку, я гляну что там почем!" else "Ну-ка, рассказывай, что это за штука и сколько за нее просят? И фото приложи, если есть.",
                        color = PrimaryBlue,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (inputMode == InputMode.LINK) {
                InputField(
                    icon = Icons.Filled.Link, 
                    text = productName, // Reusing productName for link for now
                    onValueChange = { productName = it }, 
                    placeholder = "Вставь ссылку сюда",
                    singleLine = false
                )
                Spacer(modifier = Modifier.height(16.dp))
                InputField(
                    icon = Icons.Outlined.Lightbulb,
                    text = emotions,
                    onValueChange = { emotions = it },
                    placeholder = "Твои эмоции: почему тебе это так нужно?",
                    singleLine = false,
                    modifier = Modifier.height(100.dp),
                    trailingIcon = {
                        Icon(
                            imageVector = if (isListening) Icons.Filled.Mic else Icons.Outlined.Mic,
                            contentDescription = "Voice Input",
                            tint = Color.White,
                            modifier = Modifier.clickable {
                                when (PackageManager.PERMISSION_GRANTED) {
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) -> {
                                        launchSpeechRecognizer()
                                    }
                                    else -> {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            }
                        )
                    }
                )
            } else {
                InputField(
                    icon = Icons.Outlined.ShoppingBag, 
                    text = productName, 
                    onValueChange = { productName = it }, 
                    placeholder = "Название товара"
                )
                Spacer(modifier = Modifier.height(16.dp))
                InputField(
                    icon = Icons.Outlined.CurrencyRuble,
                    text = price,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }
                        if (digits.length <= 9) {
                            price = digits
                        }
                    },
                    placeholder = "Цена",
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    visualTransformation = PriceVisualTransformation()
                )
                Spacer(modifier = Modifier.height(16.dp))
                InputField(
                    icon = Icons.Outlined.Lightbulb,
                    text = emotions,
                    onValueChange = { emotions = it },
                    placeholder = "Твои эмоции: почему тебе это так нужно?",
                    singleLine = false,
                    modifier = Modifier.height(100.dp),
                    trailingIcon = {
                        Icon(
                            imageVector = if (isListening) Icons.Filled.Mic else Icons.Outlined.Mic,
                            contentDescription = "Voice Input",
                            tint = Color.White,
                            modifier = Modifier.clickable {
                                when (PackageManager.PERMISSION_GRANTED) {
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) -> {
                                        launchSpeechRecognizer()
                                    }
                                    else -> {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            }
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (inputMode == InputMode.MANUAL) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .scale(animatedPhotoButtonScale)
                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                            .clickable(
                                interactionSource = photoButtonInteractionSource,
                                indication = null,
                                onClick = { imagePickerLauncher.launch("image/*") }
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Outlined.PhotoCamera, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isPhotoAdded) "Фото добавлено!" else "Добавить фото",
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(24.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = hasAction,
                            onCheckedChange = { hasAction = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color.White,
                                uncheckedColor = Color.White,
                                checkmarkColor = PrimaryBlue
                            )
                        )
                        Text(text = "Есть ли акция?", color = Color.White)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .offset(y = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    AnalyzingAnimation()
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.judgment),
                        contentDescription = "На суд Фризи!",
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .scale(animatedScale)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = {
                                    viewModel.analyzePurchase(
                                        productName = productName,
                                        price = price,
                                        emotions = emotions,
                                        isLinkMode = inputMode == InputMode.LINK,
                                        onSuccess = onVerdictClick
                                    )
                                }
                            )
                    )
                }
            }

            val error by viewModel.validationError.collectAsState()
            LaunchedEffect(error) {
                if (error != null) {
                    kotlinx.coroutines.delay(3000)
                    viewModel.clearError()
                }
            }
            
            androidx.compose.animation.AnimatedVisibility(
                visible = error != null,
                enter = androidx.compose.animation.slideInVertically { it } + androidx.compose.animation.fadeIn(),
                exit = androidx.compose.animation.slideOutVertically { it } + androidx.compose.animation.fadeOut(),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PrimaryBlue.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                        .clickable { viewModel.clearError() }
                        .padding(16.dp)
                ) {
                    Text(
                        text = error ?: "",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            Spacer(modifier = Modifier.height(BottomBarHeight))
        }
    }
}

@Composable
fun InputField(
    icon: ImageVector,
    text: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    readOnly: Boolean = false,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    val focusRequester = remember { FocusRequester() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { focusRequester.requestFocus() },
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
            )

            Spacer(modifier = Modifier.size(16.dp))

            Box(modifier = Modifier.weight(1f).padding(vertical = 16.dp)) {
                BasicTextField(
                    value = text,
                    onValueChange = onValueChange,
                    modifier = Modifier.focusRequester(focusRequester),
                    singleLine = singleLine,
                    readOnly = readOnly,
                    keyboardOptions = keyboardOptions,
                    visualTransformation = visualTransformation,
                    textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 16.sp),
                    decorationBox = { innerTextField ->
                        Box {
                            if (text.isEmpty()) {
                                Text(placeholder, color = Color.White.copy(alpha = 0.7f), fontSize = 16.sp)
                            }
                            innerTextField()
                        }
                    },
                )
            }

            if (trailingIcon != null) {
                Spacer(modifier = Modifier.size(16.dp))
                trailingIcon()
            }
        }
    }
}


