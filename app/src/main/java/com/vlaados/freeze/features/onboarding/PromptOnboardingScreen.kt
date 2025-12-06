package com.vlaados.freeze.features.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.vlaados.freeze.R
import com.vlaados.freeze.features.login.AuthViewModel
import com.vlaados.freeze.features.login.UpdateProfileState
import com.vlaados.freeze.ui.ErrorDialog
import com.vlaados.freeze.ui.theme.FreezeTheme
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
                // The tail of the bubble
                moveTo(size.width * 0.4f, bubbleHeight)
                lineTo(size.width * 0.45f, size.height)
                lineTo(size.width * 0.5f, bubbleHeight)
                close()
            }
            return Outline.Generic(path)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptOnboardingScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    onBackClick: (() -> Unit)? = null,
    onSuccess: () -> Unit = {}
) {
    var prompt by remember { mutableStateOf("") }
    val userProfile by authViewModel.userProfile.collectAsState()
    val updateState by authViewModel.updateProfileState.collectAsState()

    (updateState as? UpdateProfileState.Error)?.let {
        ErrorDialog(errorMessage = it.message, onDismiss = authViewModel::dismissUpdateProfileError)
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
                .padding(horizontal = 24.dp)
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (onBackClick != null) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
            }
            Spacer(Modifier.height(30.dp))
            Text(
                text = "Настройки ИИ",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.height(20.dp))

            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = R.drawable.freezeslab),
                    contentDescription = "Freezie Mascot",
                    modifier = Modifier
                        .size(450.dp)
                        .padding(top = 160.dp)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(y = 40.dp)
                        .zIndex(1f)
                        .background(Color.White, speechBubbleShape(12.dp, 15.dp))
                        .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 16.dp + 15.dp),

                    ) {
                    Text(
                        text = "Как мне с тобой общаться? Задай стиль общения или жесткость.",
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
                modifier = Modifier.offset(y = (-15).dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    placeholder = { Text("Например: Будь строгим, но справедливым") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                        cursorColor = PrimaryBlue,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedPlaceholderColor = Color.Gray,
                        unfocusedPlaceholderColor = Color.Gray
                    )
                )
                Spacer(Modifier.height(16.dp))

                if (updateState is UpdateProfileState.Loading) {
                    CircularProgressIndicator()
                } else {
                    Button(
                        onClick = {
                            userProfile?.let { user ->
                                val updatedUser = user.copy(user_prompt = prompt)
                                authViewModel.updateUserProfile(updatedUser)
                                onSuccess()
                            }
                        },
                        enabled = prompt.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4AADFF),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0x614AADFF),
                            disabledContentColor = Color.White
                        )
                    ) {
                        Text(
                            "Сохранить", color = Color.White,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}
