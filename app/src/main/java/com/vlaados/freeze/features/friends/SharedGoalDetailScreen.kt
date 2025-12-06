package com.vlaados.freeze.features.friends

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.sp
import com.vlaados.freeze.R
import com.vlaados.freeze.ui.BottomBarHeight

import com.vlaados.freeze.data.model.Group
import com.vlaados.freeze.data.model.GroupMember
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.vlaados.freeze.data.model.ChallengeData

@Composable
fun SharedGoalDetailScreen(
    group: Group, 
    members: List<GroupMember>, 
    challenges: List<ChallengeData>, 
    onCreateChallengeClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val goalName = group.goal_name ?: "Без названия"
    val goalTarget = group.goal_target_amount ?: 100000.0
    val savedAmount = members.sumOf { it.saved_for_group ?: 0.0 }
    val goalProgress = if (goalTarget > 0) (savedAmount / goalTarget).toFloat() else 0f
    
    val daysLeft = calculateDaysLeft(group.goal_date)

    val topSavers = members.sortedByDescending { it.saved_for_group }

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
                .padding(bottom = BottomBarHeight)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Freeze Logo",
                modifier = Modifier.fillMaxWidth(0.4f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.2f))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Общая цель: $goalName", fontWeight = FontWeight.Bold, color = Color.White)
                    Text("${(goalProgress * 100).toInt()}%", fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { goalProgress },
                    modifier = Modifier.fillMaxWidth().height(16.dp).clip(RoundedCornerShape(8.dp)),
                    color = Color(0xFF4AADFF),
                    trackColor = Color.White.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("сэкономлено: ${savedAmount.toInt()} ₽", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                    Text("осталось ${getDaysPlural(daysLeft)}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.2f))
                    .padding(16.dp)
            ) {
                Text("Топ экономистов:", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                topSavers.forEachIndexed { index, saver ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${index + 1}. ${saver.name ?: saver.username}", color = Color.White, fontSize = 16.sp)
                        Text("${(saver.saved_for_group ?: 0.0).toInt()} ₽", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    if (index < topSavers.size - 1) {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Челенджи:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    textAlign = TextAlign.Center
                )
                if (challenges.isEmpty()) {
                    Text(
                        text = "Активных челенджей нет",
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                } else {
                    challenges.forEach { challenge ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                                .padding(16.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(challenge.name, fontWeight = FontWeight.Bold, color = Color.White)
                                val daysLeftChallenge = calculateDaysLeft(challenge.end_date)
                                Text("осталось ${getDaysPlural(daysLeftChallenge)}", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                             challenge.participants?.forEach {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(it.name ?: it.username, color = Color.White)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
                Button(
                    onClick = onCreateChallengeClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4AADFF),
                        contentColor = Color.White
                    )
                ) {
                    Text("Создать челендж", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onSettingsClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White
                    )
                ) {
                    Text("Настройки", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

fun calculateDaysLeft(dateString: String?): Long {
    return try {
        if (dateString.isNullOrBlank()) return 0
        val sdfIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = try {
             sdfIso.parse(dateString)
        } catch (e: Exception) {
             SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(dateString)
        } ?: Date()
        
        val diff = date.time - System.currentTimeMillis()
        (diff / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
    } catch (e: Exception) {
        0
    }
}

fun getDaysPlural(days: Long): String {
    val d = days.toInt()
    val remainder10 = d % 10
    val remainder100 = d % 100
    
    val word = when {
        remainder100 in 11..19 -> "дней"
        remainder10 == 1 -> "день"
        remainder10 in 2..4 -> "дня"
        else -> "дней"
    }
    return "$d $word"
}
