package com.vlaados.freeze.ui

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.vlaados.freeze.R
import com.vlaados.freeze.Screen

val BottomBarHeight = 80.dp

@Composable
fun BottomBar(navController: NavController) {
    val items = listOf(
        Screen.Home,
        Screen.Friends,
        Screen.Freezer,
        Screen.Profile,
    )
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentDestination = navBackStackEntry?.destination

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(BottomBarHeight)
            .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
            .background(Color(186, 218, 243, 255))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { screen ->
                val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                BottomBarItem(
                    screen = screen,
                    isSelected = selected,
                    onClick = {
                        if (screen == Screen.Home) {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        } else {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun BottomBarItem(screen: Screen, isSelected: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 1.1f else 1.0f,
        label = "scale",
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 500f)
    )

    data class IconResource(val resourceId: Int?, val imageVector: ImageVector? = null)

    val icon = when (screen) {
        Screen.Home -> if (isSelected) IconResource(R.drawable.home_on) else IconResource(R.drawable.home)
        Screen.Friends -> if (isSelected) IconResource(R.drawable.friend_on) else IconResource(R.drawable.friend)
        Screen.Freezer -> if (isSelected) IconResource(R.drawable.snowflay_on) else IconResource(R.drawable.snowflay)
        Screen.Profile -> if (isSelected) IconResource(R.drawable.user_on) else IconResource(R.drawable.user)
        else -> IconResource(R.drawable.home) // Should not happen
    }
    val textColor = if (isSelected) Color(15, 111, 185) else Color(80, 98, 112)
    val text = when (screen) {
        Screen.Home -> stringResource(id = R.string.home)
        Screen.Friends -> stringResource(id = R.string.friends)
        Screen.Freezer -> stringResource(id = R.string.freezer)
        Screen.Profile -> stringResource(id = R.string.profile)
        else -> ""
    }

    Column(
        modifier = Modifier
            .scale(animatedScale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(width = 80.dp, height = 40.dp),
            contentAlignment = Alignment.Center
        ) {
            icon.resourceId?.let {
                Image(
                    painter = painterResource(id = it),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            } ?: icon.imageVector?.let {
                Image(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Text(text = text, color = textColor, fontSize = 12.sp)
    }
}
