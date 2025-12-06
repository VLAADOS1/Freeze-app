package com.vlaados.freeze.features.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vlaados.freeze.data.local.TokenStorage
import com.vlaados.freeze.data.model.CreateGroupRequest
import com.vlaados.freeze.data.model.Group
import com.vlaados.freeze.data.remote.ApiService
import com.vlaados.freeze.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val apiService: ApiService,
    private val tokenStorage: TokenStorage,
    private val userRepository: UserRepository
) : ViewModel() {

    sealed class FriendsUiState {
        object Loading : FriendsUiState()
        object NoGroup : FriendsUiState()
        data class GroupNoGoal(val group: Group) : FriendsUiState()
        data class GroupWithGoal(val group: Group, val members: List<com.vlaados.freeze.data.model.GroupMember> = emptyList(), val challenges: List<com.vlaados.freeze.data.model.ChallengeData> = emptyList()) : FriendsUiState()
    }

    sealed class FriendEvent {
        data class ShowSuccess(val message: String) : FriendEvent()
        data class ShowError(val message: String) : FriendEvent()
    }

    private val _uiState = MutableStateFlow<FriendsUiState>(FriendsUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _currentUserId = MutableStateFlow("")
    val currentUserId = _currentUserId.asStateFlow()

    private val _events = Channel<FriendEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        checkGroupStatus()
        fetchMyId()
    }

    private fun fetchMyId() {
        viewModelScope.launch {
            try {
                val token = tokenStorage.getToken().first() ?: return@launch
                val user = userRepository.getMe(token).getOrNull()
                if (user?.id != null) {
                    _currentUserId.value = user.id.toString()
                }
            } catch (e: Exception) {
            }
        }
    }

    fun checkGroupStatus() {
        viewModelScope.launch {
            try {
                _uiState.value = FriendsUiState.Loading
                val token = tokenStorage.getToken().first()
                if (token == null) {
                    _uiState.value = FriendsUiState.NoGroup
                    return@launch
                }

                val groupsResponse = apiService.getMyGroups("Bearer $token")
                
                if (groupsResponse.isSuccessful && !groupsResponse.body().isNullOrEmpty()) {
                    val group = groupsResponse.body()!!.first()
                    val hasGoal = !group.goal_name.isNullOrBlank() && (group.goal_target_amount ?: 0.0) > 0.0
                    
                    if (hasGoal) {
                        try {
                            val membersResponse = apiService.getGroupMembers("Bearer $token", group.id)
                            val members = if (membersResponse.isSuccessful) membersResponse.body() ?: emptyList() else emptyList()
                            
                            val challengesResponse = apiService.getGroupChallenges("Bearer $token", group.id)
                            val challenges = if (challengesResponse.isSuccessful) challengesResponse.body() ?: emptyList() else emptyList()

                            _uiState.value = FriendsUiState.GroupWithGoal(group, members, challenges)
                        } catch (e: Exception) {
                            _uiState.value = FriendsUiState.GroupWithGoal(group, emptyList(), emptyList())
                        }
                    } else {
                        _uiState.value = FriendsUiState.GroupNoGoal(group)
                    }
                } else {
                    _uiState.value = FriendsUiState.NoGroup
                }
            } catch (e: Exception) {
                _uiState.value = FriendsUiState.NoGroup
            }
        }
    }

    fun addFriend(friendIdStr: String) {
        viewModelScope.launch {
            try {
                val token = tokenStorage.getToken().first() ?: return@launch
                
                val friendId = friendIdStr.toIntOrNull()
                if (friendId == null) {
                    _events.send(FriendEvent.ShowError("Некорректный ID"))
                    return@launch
                }
                
                val myIdStr = _currentUserId.value
                val myId = myIdStr.toIntOrNull()
                
                if (myId != null && friendId == myId) {
                     _events.send(FriendEvent.ShowError("Нельзя добавить самого себя"))
                     return@launch
                }

                val userCheck = apiService.getUser("Bearer $token", friendId)
                if (!userCheck.isSuccessful) {
                     _events.send(FriendEvent.ShowError("Пользователь не найден"))
                     return@launch
                }
                var targetGroupId: Int? = null
                val myGroupsRes = apiService.getMyGroups("Bearer $token")
                
                if (myGroupsRes.isSuccessful && !myGroupsRes.body().isNullOrEmpty()) {
                    targetGroupId = myGroupsRes.body()!!.first().id
                } else {
                    val createReq = CreateGroupRequest(name = "Team $myIdStr")
                    val createRes = apiService.createGroup("Bearer $token", createReq)
                    if (createRes.isSuccessful && createRes.body() != null) {
                        targetGroupId = createRes.body()!!.id
                    } else {
                        _events.send(FriendEvent.ShowError("Не удалось создать группу"))
                        return@launch
                    }
                }

                if (targetGroupId == null) {
                     _events.send(FriendEvent.ShowError("Ошибка группы"))
                     return@launch
                }

                val addResponse = apiService.addMemberToGroup("Bearer $token", targetGroupId, friendId)
                if (addResponse.isSuccessful) {
                    _events.send(FriendEvent.ShowSuccess("Пользователь добавлен!"))
                    checkGroupStatus()
                } else {
                    if (addResponse.code() == 409 || addResponse.code() == 400) {
                        _events.send(FriendEvent.ShowError("Пользователь уже в группе"))
                    } else {
                         _events.send(FriendEvent.ShowError("Ошибка добавления"))
                    }
                }

            } catch (e: Exception) {
                _events.send(FriendEvent.ShowError("Ошибка соединения: ${e.message}"))
            }
        }
    }

    fun createSharedGoal(goalName: String, goalAmount: Double, goalDate: String) {
        viewModelScope.launch {
            try {
                val token = tokenStorage.getToken().first() ?: return@launch
                val currentState = _uiState.value
                
                if (currentState !is FriendsUiState.GroupNoGoal) {
                    _events.send(FriendEvent.ShowError("Ошибка состояния группы"))
                    return@launch
                }
                
                val currentGroup = currentState.group
                val request = CreateGroupRequest(
                    name = currentGroup.name,
                    goal_name = goalName,
                    goal_target_amount = goalAmount,
                    goal_date = goalDate
                )

                val response = apiService.updateGroup("Bearer $token", currentGroup.id, request)
                if (response.isSuccessful) {
                    _events.send(FriendEvent.ShowSuccess("Цель создана!"))
                    checkGroupStatus()
                } else {
                    _events.send(FriendEvent.ShowError("Ошибка создания цели"))
                }
            } catch (e: Exception) {
                _events.send(FriendEvent.ShowError("Ошибка сети: ${e.message}"))
            }
        }
    }

    fun createGroupChallenge(challengeName: String, endDate: String) {
        viewModelScope.launch {
            try {
                val token = tokenStorage.getToken().first() ?: return@launch
                val currentState = _uiState.value
                
                if (currentState !is FriendsUiState.GroupWithGoal) {
                    _events.send(FriendEvent.ShowError("Ошибка состояния группы"))
                    return@launch
                }
                
                val currentGroup = currentState.group
                val isoDate = try {
                    val inputFormat = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
                    val date = inputFormat.parse(endDate) ?: java.util.Date()
                    
                    val calendar = java.util.Calendar.getInstance()
                    calendar.time = date
                    calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
                    calendar.set(java.util.Calendar.MINUTE, 59)
                    calendar.set(java.util.Calendar.SECOND, 59)
                    
                    val outputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
                    outputFormat.format(calendar.time)
                } catch (e: Exception) {
                    endDate
                }

                val request = com.vlaados.freeze.data.model.CreateChallengeRequest(
                    name = challengeName,
                    end_date = isoDate,
                    target_amount = 0.0
                )
                
                val response = apiService.createChallenge("Bearer $token", currentGroup.id, request)
                if (response.isSuccessful) {
                    _events.send(FriendEvent.ShowSuccess("Челендж создан!"))
                    checkGroupStatus()
                } else {
                    _events.send(FriendEvent.ShowError("Ошибка создания челенджа"))
                }
            } catch (e: Exception) {
                _events.send(FriendEvent.ShowError("Ошибка сети: ${e.message}"))
            }
        }
    }
    fun updateGoalName(newName: String) {
        viewModelScope.launch {
            try {
                val token = tokenStorage.getToken().first() ?: return@launch
                val currentState = _uiState.value
                
                if (currentState !is FriendsUiState.GroupWithGoal) {
                    _events.send(FriendEvent.ShowError("Ошибка состояния группы"))
                    return@launch
                }
                
                val currentGroup = currentState.group
                val request = CreateGroupRequest(
                    name = currentGroup.name,
                    goal_name = newName,
                    goal_target_amount = currentGroup.goal_target_amount ?: 0.0,
                    goal_date = currentGroup.goal_date ?: ""
                )

                val response = apiService.updateGroup("Bearer $token", currentGroup.id, request)
                if (response.isSuccessful) {
                    _events.send(FriendEvent.ShowSuccess("Имя цели обновлено!"))
                    checkGroupStatus()
                } else {
                    _events.send(FriendEvent.ShowError("Ошибка обновления"))
                }
            } catch (e: Exception) {
                _events.send(FriendEvent.ShowError("Ошибка сети: ${e.message}"))
            }
        }
    }

    fun updateGoalDate(dateMillis: Long) {
        viewModelScope.launch {
            try {
                val token = tokenStorage.getToken().first() ?: return@launch
                val currentState = _uiState.value
                
                if (currentState !is FriendsUiState.GroupWithGoal) {
                    _events.send(FriendEvent.ShowError("Ошибка состояния группы"))
                    return@launch
                }
                
                val currentGroup = currentState.group
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
                val newDate = sdf.format(java.util.Date(dateMillis))

                val request = CreateGroupRequest(
                    name = currentGroup.name,
                    goal_name = currentGroup.goal_name,
                    goal_target_amount = currentGroup.goal_target_amount ?: 0.0,
                    goal_date = newDate
                )

                val response = apiService.updateGroup("Bearer $token", currentGroup.id, request)
                if (response.isSuccessful) {
                    _events.send(FriendEvent.ShowSuccess("Дата цели обновлена!"))
                    checkGroupStatus()
                } else {
                    _events.send(FriendEvent.ShowError("Ошибка обновления"))
                }
            } catch (e: Exception) {
                _events.send(FriendEvent.ShowError("Ошибка сети: ${e.message}"))
            }
        }
    }

    fun resetGroupSavings() {
        viewModelScope.launch {
            try {
                val token = tokenStorage.getToken().first() ?: return@launch
                val currentState = _uiState.value
                
                if (currentState !is FriendsUiState.GroupWithGoal) {
                    _events.send(FriendEvent.ShowError("Ошибка состояния группы"))
                    return@launch
                }
                
                val currentGroup = currentState.group
                val members = currentState.members
                
                var successCount = 0
                members.forEach { member ->
                    val req = com.vlaados.freeze.data.remote.UpdateMemberRequest(saved_amount = 0.0)
                    val res = apiService.updateGroupMember("Bearer $token", currentGroup.id, member.user_id, req)
                    if (res.isSuccessful) {
                        successCount++
                    }
                }
                
                if (successCount > 0) {
                     _events.send(FriendEvent.ShowSuccess("Накопления сброшены!"))
                     checkGroupStatus()
                } else {
                     _events.send(FriendEvent.ShowError("Ошибка сброса"))
                }

            } catch (e: Exception) {
                _events.send(FriendEvent.ShowError("Ошибка сети: ${e.message}"))
            }
        }
    }
}
