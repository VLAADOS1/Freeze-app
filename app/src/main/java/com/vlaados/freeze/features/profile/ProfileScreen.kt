package com.vlaados.freeze.features.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vlaados.freeze.R
import com.vlaados.freeze.ui.BottomBarHeight
import com.vlaados.freeze.ui.theme.PrimaryBlue

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onSettingsClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedAchievement = remember { mutableStateOf<AchievementUiModel?>(null) }

    if (selectedAchievement.value != null) {
        val achievement = selectedAchievement.value!!
        androidx.compose.ui.window.Dialog(onDismissRequest = { selectedAchievement.value = null }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                     coil.compose.AsyncImage(
                        model = achievement.photoUrl,
                        contentDescription = achievement.title,
                        modifier = Modifier
                            .size(120.dp),
                        placeholder = painterResource(id = R.drawable.sneginka),
                        error = painterResource(id = R.drawable.sneginka)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = achievement.title,
                        color = Color.Black,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = achievement.description,
                        color = Color.Gray,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (achievement.targetValue > 0) {
                        Text(
                            text = "${achievement.currentValue} / ${achievement.targetValue}",
                            color = Color(0xFF4AADFF),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { 
                                (achievement.currentValue.toFloat() / achievement.targetValue.toFloat()).coerceIn(0f, 1f) 
                            },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFF4AADFF),
                            trackColor = Color.LightGray,
                        )
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

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(top = 32.dp, bottom = BottomBarHeight)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4AADFF))
                                .border(BorderStroke(2.dp, Color.White), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = uiState.avatarInitials,
                                color = Color.White,
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = uiState.name,
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center
                        )

                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                Button(
                    onClick = onSettingsClick,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f), contentColor = Color.White)
                ) {
                    Text("Настройки", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (uiState.achievements.isNotEmpty()) {
                item {
                    Text(
                        text = "Достижения",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            }


            
            val chunkedAchievements = uiState.achievements.chunked(2)
            items(chunkedAchievements.size) { index ->
                Row(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    chunkedAchievements[index].forEach { achievement ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                                .clickable { selectedAchievement.value = achievement }
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                        
                                coil.compose.AsyncImage(
                                    model = achievement.photoUrl,
                                    contentDescription = achievement.title,
                                    modifier = Modifier
                                        .size(60.dp)
                                        .scale(2.1f) // Scaled 1.6 * 1.3 ≈ 2.1
                                        .alpha(1f), // Fully opaque
                                    placeholder = painterResource(id = R.drawable.sneginka), // Fallback if url fails
                                    error = painterResource(id = R.drawable.sneginka)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = achievement.title,
                                    color = Color.White, // Fully opaque
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 16.sp,
                                    maxLines = 2
                                )
                                if (achievement.targetValue > 0) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { 
                                            (achievement.currentValue.toFloat() / achievement.targetValue.toFloat()).coerceIn(0f, 1f) 
                                        },
                                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                        color = Color(0xFF4AADFF),
                                        trackColor = Color.White.copy(alpha = 0.3f),
                                    )
                                }
                            }
                        }
                    }
                    if (chunkedAchievements[index].size == 1) {
                        Spacer(Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
