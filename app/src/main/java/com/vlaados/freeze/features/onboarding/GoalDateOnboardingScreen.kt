package com.vlaados.freeze.features.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDateOnboardingScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    onBackClick: (() -> Unit)? = null,
    onNextClick: (() -> Unit)? = null
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        selectableDates = object : androidx.compose.material3.SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                // Disable past and current dates
                val today = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                return utcTimeMillis > today.timeInMillis
            }
        }
    )
    val confirmEnabled by remember { derivedStateOf { datePickerState.selectedDateMillis != null } }
    val userProfile by authViewModel.userProfile.collectAsState()
    val updateState by authViewModel.updateProfileState.collectAsState()

    LaunchedEffect(userProfile) {
        if (datePickerState.selectedDateMillis == null) {
            userProfile?.goal_date?.let { dateString ->
                if (dateString.isNotEmpty()) {
                    try {
                        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                        val date = sdf.parse(dateString)
                        if (date != null) {
                            datePickerState.selectedDateMillis = date.time
                        }
                    } catch (e: Exception) {
                        // Ignore parse errors
                    }
                }
            }
        }
    }

    val selectedDate = datePickerState.selectedDateMillis?.let {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = it
        }
        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(calendar.time)
    } ?: ""

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
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = { showDatePicker = false },
                        enabled = confirmEnabled
                    ) {
                        Text("OK", color = Color.Black)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel", color = Color.Black)
                    }
                },
                colors = androidx.compose.material3.DatePickerDefaults.colors(
                    containerColor = Color.White,
                )
            ) {
                androidx.compose.runtime.CompositionLocalProvider(
                    androidx.compose.material3.LocalContentColor provides Color.Black
                ) {
                    DatePicker(
                        state = datePickerState,
                        colors = androidx.compose.material3.DatePickerDefaults.colors(
                            containerColor = Color.White,
                            titleContentColor = Color.Black,
                            headlineContentColor = Color.Black,
                            weekdayContentColor = Color.Black,
                            subheadContentColor = Color.Black,
                            navigationContentColor = Color.Black,
                            yearContentColor = Color.Black,
                            currentYearContentColor = Color.Black,
                            selectedYearContentColor = Color.White,
                            selectedYearContainerColor = PrimaryBlue,
                            dayContentColor = Color.Black,
                            disabledDayContentColor = Color.Gray,
                            selectedDayContentColor = Color.White,
                            selectedDayContainerColor = PrimaryBlue,
                            todayDateBorderColor = PrimaryBlue,
                            todayContentColor = PrimaryBlue
                        )
                    )
                }
            }
        }
        }


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
                text = "Знакомство",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.height(20.dp))

            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = R.drawable.freezeroad),
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
                        text = "К какой дате ты хочешь накопить на экономмии?",
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
                Box(modifier = Modifier.clickable { showDatePicker = true }) {
                    OutlinedTextField(
                        value = selectedDate,
                        onValueChange = { },
                        readOnly = true,
                        enabled = false,
                        placeholder = { Text("Выберите дату") },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                            cursorColor = PrimaryBlue,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            disabledTextColor = Color.Black,
                            disabledBorderColor = Color.White.copy(alpha = 0.7f),
                            disabledContainerColor = Color.White,
                            focusedPlaceholderColor = Color.Gray,
                            unfocusedPlaceholderColor = Color.Gray,
                            disabledPlaceholderColor = Color.Gray
                        )
                    )
                }

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = {
                        userProfile?.let { user ->
                            // If date string is valid, save it.
                            if (selectedDate.isNotEmpty()) {
                                 val isoDate = datePickerState.selectedDateMillis?.let { millis ->
                                     val calendar = java.util.Calendar.getInstance().apply { timeInMillis = millis }
                                     java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault()).format(calendar.time)
                                 } ?: user.goal_date // Fallback to existing if conversion fails (unlikely if selectedDate is valid)
                                 
                                 val updatedUser = user.copy(goal_date = isoDate)
                                 authViewModel.updateUserProfile(updatedUser)
                                 onNextClick?.invoke()
                            } else {
                                val dateToSave = if (selectedDate.isNotEmpty()) {
                                     datePickerState.selectedDateMillis?.let { millis ->
                                         val calendar = java.util.Calendar.getInstance().apply { timeInMillis = millis }
                                         java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault()).format(calendar.time)
                                     } ?: user.goal_date
                                } else user.goal_date
                                
                                val updatedUser = user.copy(goal_date = dateToSave)
                                authViewModel.updateUserProfile(updatedUser)
                                onNextClick?.invoke()
                            }
                        }
                    },
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
                        "Далее", color = Color.White,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(Modifier.height(20.dp))
            }
        }
    }

@Preview
@Composable
fun GoalDateOnboardingScreenPreview() {
    FreezeTheme {
    }
}
