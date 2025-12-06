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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.vlaados.freeze.R
import com.vlaados.freeze.ui.BottomBarHeight
import com.vlaados.freeze.ui.theme.PrimaryBlue

@Composable
fun FriendsScreen(
    onCreateSharedGoal: () -> Unit,
    viewModel: FriendsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.checkGroupStatus()
    }

    var showCreateChallenge by remember { mutableStateOf(false) }
    var showGroupSettings by remember { mutableStateOf(false) }

    if (showCreateChallenge) {
        CreateChallengeScreen(
            onBackClick = { showCreateChallenge = false },
            onCreate = { name, date ->
                viewModel.createGroupChallenge(name, date)
                showCreateChallenge = false
            }
        )
        return
    }

    if (showGroupSettings) {
        GroupSettingsScreen(
            onBackClick = { showGroupSettings = false },
            onGoalNameChanged = { newName -> viewModel.updateGoalName(newName) },
            onGoalDateChanged = { newDateMillis -> viewModel.updateGoalDate(newDateMillis) },
            onResetSavings = { viewModel.resetGroupSavings() }
        )
        return
    }

    val state = uiState
    when (state) {
        is FriendsViewModel.FriendsUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize().background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(10, 30, 116), Color(10, 118, 209))
                )
            ), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        }
        is FriendsViewModel.FriendsUiState.NoGroup -> {
            NoGroupScreen(
                viewModel = viewModel
            )
        }
        is FriendsViewModel.FriendsUiState.GroupNoGoal -> {
            CreateSharedGoalScreen(onCreateGoal = { name, amount, date ->
                viewModel.createSharedGoal(name, amount, date)
            })
        }
        is FriendsViewModel.FriendsUiState.GroupWithGoal -> {
            SharedGoalDetailScreen(
                group = state.group,
                members = state.members,
                challenges = state.challenges,
                onCreateChallengeClick = { showCreateChallenge = true },
                onSettingsClick = { showGroupSettings = true }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoGroupScreen(
    viewModel: FriendsViewModel
) {
    var friendCode by remember { mutableStateOf("") }
    val myId by viewModel.currentUserId.collectAsState()
    
    var dialogEvent by remember { mutableStateOf<FriendsViewModel.FriendEvent?>(null) }
    
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
                    val title = when (dialogEvent) {
                        is FriendsViewModel.FriendEvent.ShowSuccess -> "Успех!"
                        is FriendsViewModel.FriendEvent.ShowError -> "Ошибка"
                        else -> ""
                    }
                    val msg = when (val e = dialogEvent) {
                        is FriendsViewModel.FriendEvent.ShowSuccess -> e.message
                        is FriendsViewModel.FriendEvent.ShowError -> e.message
                        else -> ""
                    }

                    Text(
                        text = title,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Text(
                        text = msg,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(bottom = BottomBarHeight),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
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
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = "У вас пока нет друзей",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Ты можешь добавить друга и соревноваться с ним кто больше сэкономит",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Вот твой код:",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (myId.isNotEmpty()) myId else "...",
                color = Color.White,
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.2f))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Добавте друга по коду:",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = friendCode,
                    onValueChange = { friendCode = it },
                    placeholder = { Text("Введите код") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.addFriend(friendCode) },
                    enabled = friendCode.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4AADFF),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFF4AADFF).copy(alpha = 0.5f),
                        disabledContentColor = Color.White.copy(alpha = 0.7f)
                    )
                ) {
                    Text("Добавить", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
