package com.vlaados.freeze

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.platform.LocalFocusManager
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.vlaados.freeze.features.home.HomeScreen
import com.vlaados.freeze.features.login.AuthViewModel
import com.vlaados.freeze.features.login.EmailLoginScreen
import com.vlaados.freeze.features.login.LoginScreen
import com.vlaados.freeze.features.login.RegistrationScreen
import com.vlaados.freeze.features.onboarding.DreamOnboardingScreen
import com.vlaados.freeze.features.onboarding.GoalAmountOnboardingScreen
import com.vlaados.freeze.features.onboarding.GoalDateOnboardingScreen
import com.vlaados.freeze.features.onboarding.IncomeOnboardingScreen
import com.vlaados.freeze.features.onboarding.OnboardingScreen
import com.vlaados.freeze.features.onboarding.SavingsOnboardingScreen
import com.vlaados.freeze.features.onboarding.WeaknessOnboardingScreen
import com.vlaados.freeze.features.profile.ProfileScreen
import com.vlaados.freeze.features.settings.SettingsScreen
import com.vlaados.freeze.features.settings.SelfRestraintScreen
import com.vlaados.freeze.features.settings.PromptScreen
import com.vlaados.freeze.features.friends.FriendsScreen
import com.vlaados.freeze.features.friends.CreateSharedGoalScreen
import com.vlaados.freeze.features.friends.SharedGoalDetailScreen
import com.vlaados.freeze.features.freezer.FreezerScreen
import com.vlaados.freeze.features.purchase.PurchaseScreen
import com.vlaados.freeze.features.purchase.PurchaseViewModel
import com.vlaados.freeze.features.purchase.VerdictScreen
import com.vlaados.freeze.features.chat.ChatScreen
import com.vlaados.freeze.features.breathing.BreathingScreen
import com.vlaados.freeze.features.history.HistoryScreen
import com.vlaados.freeze.ui.BottomBar
import com.vlaados.freeze.features.splash.SplashScreen
import com.vlaados.freeze.ui.theme.FreezeTheme
import com.vlaados.freeze.R
import androidx.navigation.NavType
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FreezeTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation(
    viewModel: MainViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    purchaseViewModel: PurchaseViewModel = hiltViewModel()
) {
    val userProfile by authViewModel.userProfile.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(userProfile, isLoading) {
        if (isLoading) return@LaunchedEffect

        
        if (userProfile == null) {
            navController.navigate("auth_flow") {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
            return@LaunchedEffect
        }


        val isEditing = navBackStackEntry?.arguments?.getBoolean("edit") == true
        if (isEditing) return@LaunchedEffect

        userProfile?.let { user ->
            val nextRoute = when {
                user.name.isNullOrEmpty() -> Screen.Onboarding.route
                user.income == null || user.income == 0.0 -> Screen.IncomeOnboarding.route
                user.monthly_savings == null || user.monthly_savings == 0.0 -> Screen.SavingsOnboarding.route
                user.weakness.isNullOrEmpty() -> Screen.WeaknessOnboarding.route
                user.goal_name.isNullOrEmpty() -> Screen.DreamOnboarding.route
                user.goal_amount == null || user.goal_amount == 0.0 -> Screen.GoalAmountOnboarding.route
                user.goal_date.isNullOrEmpty() -> Screen.GoalDateOnboarding.route
                else -> "main_flow"
            }

            if (nextRoute != currentRoute) {
                val isAlreadyInMainFlow = nextRoute == "main_flow" && navBackStackEntry?.destination?.parent?.route == "main_flow"
                
                if (!isAlreadyInMainFlow) {
                    navController.navigate(nextRoute) {
                        if (nextRoute == "main_flow") {
                             popUpTo(navController.graph.id) { inclusive = true }
                        }
                    }
                }
            }
        }
    }

    val focusManager = LocalFocusManager.current
    Box(modifier = Modifier.fillMaxSize().pointerInput(Unit) {
        detectTapGestures(onTap = {
            focusManager.clearFocus()
        })
    }) {
        NavHost(navController = navController, startDestination = Screen.Splash.route, modifier = Modifier.fillMaxSize()) {
            composable(Screen.Splash.route) {
                SplashScreen()
            }
            navigation(startDestination = Screen.Login.route, route = "auth_flow") {
                composable(Screen.Login.route) {
                    LoginScreen(
                        onLoginFinished = { navController.navigate(Screen.EmailLogin.route) },
                        onSkipRegistration = { navController.navigate(Screen.Onboarding.route) },
                        authViewModel = authViewModel
                    )
                }
                composable(Screen.EmailLogin.route) {
                    EmailLoginScreen(
                        onBackClick = { navController.popBackStack() },
                        onLoginClick = {
                            // ViewModel handles login, LaunchedEffect will handle navigation
                        },
                        onRegisterClick = { navController.navigate(Screen.Registration.route) },
                        viewModel = authViewModel
                    )
                }
                composable(Screen.Registration.route) {
                    RegistrationScreen(
                        onBackClick = { navController.popBackStack() },
                        onRegisterClick = { 
                            // Auto-login logic in ViewModel and Main LaunchedEffect will handle navigation.
                            // We do NOT want to navigate to EmailLogin here.
                        },
                        viewModel = authViewModel
                    )
                }
            }


            composable(
                route = Screen.Onboarding.route + "?edit={edit}",
                arguments = listOf(navArgument("edit") { defaultValue = false; type = NavType.BoolType })
            ) { backStackEntry ->
                val isEdit = backStackEntry.arguments?.getBoolean("edit") ?: false
                OnboardingScreen(
                    authViewModel = authViewModel,
                    onBackClick = if (isEdit) { { navController.popBackStack() } } else null,
                    onSave = if (isEdit) { { navController.popBackStack() } } else null
                )
            }

            composable(
                route = Screen.IncomeOnboarding.route + "?edit={edit}",
                arguments = listOf(navArgument("edit") { defaultValue = false; type = NavType.BoolType })
            ) { backStackEntry ->
                val isEdit = backStackEntry.arguments?.getBoolean("edit") ?: false
                IncomeOnboardingScreen(
                    authViewModel = authViewModel,
                    onBackClick = if (isEdit) { { navController.popBackStack() } } else null,
                    onSave = if (isEdit) { { navController.popBackStack() } } else null,
                    showSkipButton = !isEdit
                )
            }

            composable(
                route = Screen.SavingsOnboarding.route + "?edit={edit}",
                arguments = listOf(navArgument("edit") { defaultValue = false; type = NavType.BoolType })
            ) { backStackEntry ->
                val isEdit = backStackEntry.arguments?.getBoolean("edit") ?: false
                SavingsOnboardingScreen(
                    authViewModel = authViewModel,
                    onBackClick = if (isEdit) { { navController.popBackStack() } } else null,
                    onSave = if (isEdit) { { navController.popBackStack() } } else null,
                    showSkipButton = !isEdit
                )
            }

            composable(
                route = Screen.WeaknessOnboarding.route + "?edit={edit}",
                arguments = listOf(navArgument("edit") { defaultValue = false; type = NavType.BoolType })
            ) { backStackEntry ->
                val isEdit = backStackEntry.arguments?.getBoolean("edit") ?: false
                WeaknessOnboardingScreen(
                    authViewModel = authViewModel,
                    onBackClick = if (isEdit) { { navController.popBackStack() } } else null,
                    onSave = if (isEdit) { { navController.popBackStack() } } else null,
                    showSkipButton = !isEdit
                )
            }

            composable(
                route = Screen.DreamOnboarding.route + "?edit={edit}",
                arguments = listOf(navArgument("edit") { defaultValue = false; type = NavType.BoolType })
            ) { backStackEntry ->
                 val isEdit = backStackEntry.arguments?.getBoolean("edit") ?: false
                 DreamOnboardingScreen(
                     authViewModel = authViewModel,
                     onBackClick = if (isEdit) { { navController.popBackStack() } } else null,
                     onNextClick = if (isEdit) { { navController.navigate(Screen.GoalAmountOnboarding.route + "?edit=true") } } else null
                 )
            }

            composable(
                route = Screen.GoalAmountOnboarding.route + "?edit={edit}",
                arguments = listOf(navArgument("edit") { defaultValue = false; type = NavType.BoolType })
            ) { backStackEntry ->
                val isEdit = backStackEntry.arguments?.getBoolean("edit") ?: false
                GoalAmountOnboardingScreen(
                    authViewModel = authViewModel,
                    onBackClick = if (isEdit) { { navController.popBackStack() } } else null,
                    onNextClick = if (isEdit) { { navController.navigate(Screen.GoalDateOnboarding.route + "?edit=true") } } else null
                )
            }

            composable(
                route = Screen.GoalDateOnboarding.route + "?edit={edit}",
                arguments = listOf(navArgument("edit") { defaultValue = false; type = NavType.BoolType })
            ) { backStackEntry ->
                val isEdit = backStackEntry.arguments?.getBoolean("edit") ?: false
                GoalDateOnboardingScreen(
                    authViewModel = authViewModel,
                    onBackClick = if (isEdit) { { navController.popBackStack() } } else null,
                    onNextClick = if (isEdit) { { navController.popBackStack(Screen.Settings.route, false) } } else null
                )
            }

            navigation(startDestination = Screen.Home.route, route = "main_flow") {
                composable(
                    route = Screen.Home.route,
                    arguments = listOf(navArgument("message") { defaultValue = "" })
                ) { backStackEntry ->
                    val message = backStackEntry.arguments?.getString("message")
                    HomeScreen(
                        onPurchaseClick = { navController.navigate(Screen.Purchase.route) },
                        successMessage = if (message.isNullOrBlank()) null else message
                    )
                }
                composable(Screen.Friends.route) { 
                    FriendsScreen(onCreateSharedGoal = { navController.navigate(Screen.CreateSharedGoal.route) }) 
                }

                composable(Screen.Freezer.route) { 
                    FreezerScreen(
                        onPurchaseSuccess = { navController.popBackStack() },
                        onPurchaseFailed = { navController.popBackStack() }
                    ) 
                }
                composable(Screen.Profile.route) {
                    ProfileScreen(
                        onSettingsClick = { navController.navigate(Screen.Settings.route) }
                    )
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        onBackClick = { navController.popBackStack() },
                        onChangeNameClick = { navController.navigate(Screen.Onboarding.route + "?edit=true") },
                        onChangeIncomeClick = { navController.navigate(Screen.IncomeOnboarding.route + "?edit=true") },
                        onChangeWeaknessesClick = { navController.navigate(Screen.WeaknessOnboarding.route + "?edit=true") },
                        onHistoryClick = { navController.navigate(Screen.History.route) },
                        onChangeGoalClick = { navController.navigate(Screen.DreamOnboarding.route + "?edit=true") },
                        onResetSavingsClick = { authViewModel.resetSavedForGoal() },
                        onAddSelfRestraintClick = { navController.navigate(Screen.SelfRestraintOnboarding.route) },
                        onAddPromptClick = { navController.navigate(Screen.PromptOnboarding.route) },
                        onChangeSavingsClick = { navController.navigate(Screen.SavingsOnboarding.route + "?edit=true") },
                        onLogoutClick = { authViewModel.logout() },
                        analyticsEnabled = userProfile?.analytics_enabled ?: true,
                        onAnalyticsChanged = { enabled ->
                            userProfile?.let { user ->
                                authViewModel.updateUserProfile(user.copy(analytics_enabled = enabled))
                            }
                        }
                    )
                }
                composable(Screen.Purchase.route) {
                    PurchaseScreen(
                        onBackClick = { navController.popBackStack() },
                        onVerdictClick = { navController.navigate(Screen.Verdict.route) },
                        viewModel = purchaseViewModel
                    )
                }
                composable(Screen.Verdict.route) {
                    VerdictScreen(
                        onBackClick = { navController.popBackStack() },
                        onFreezeClick = { navController.navigate(Screen.Freezer.route) },
                        onDiscussClick = {
                            val context = purchaseViewModel.currentPurchase.value
                            val verdict = purchaseViewModel.verdict.value
                            if (context != null) {
                                navController.navigate(
                                    Screen.Chat.createRoute(
                                        context.name,
                                        context.price,
                                        context.emotions,
                                        verdict?.chat_starter
                                    )
                                )
                            }
                        },
                        onBreathingClick = { duration, tp, tr -> navController.navigate(Screen.Breathing.createRoute(duration, tp, tr)) },
                        onRightChoiceClick = { text ->
                            purchaseViewModel.confirmRefusal { 
                                navController.navigate(Screen.Home.createRoute(text)) { 
                                    popUpTo(Screen.Home.route) { inclusive = true } 
                                } 
                            } 
                        },
                        viewModel = purchaseViewModel
                    )
                }
                composable(
                    route = Screen.Chat.route,
                    arguments = listOf(
                        navArgument("product") { defaultValue = "" },
                        navArgument("price") { defaultValue = "" },
                        navArgument("emotions") { defaultValue = "" },
                        navArgument("starter") { defaultValue = "" }
                    )
                ) { backStackEntry ->
                    val product = backStackEntry.arguments?.getString("product") ?: ""
                    val price = backStackEntry.arguments?.getString("price") ?: ""
                    val emotions = backStackEntry.arguments?.getString("emotions") ?: ""
                    val starterArg = backStackEntry.arguments?.getString("starter")
                    val starter = if (starterArg.isNullOrBlank()) null else starterArg

                    ChatScreen(
                        onBackClick = { navController.popBackStack() },
                        productName = product,
                        price = price,
                        emotions = emotions,
                        starter = starter
                    )
                }
                composable(
                    route = Screen.Breathing.route,
                    arguments = listOf(
                        navArgument("duration") { type = NavType.IntType },
                        navArgument("textPurchased") { defaultValue = "" },
                        navArgument("textRejected") { defaultValue = "" }
                    )
                ) { backStackEntry ->
                    val duration = backStackEntry.arguments?.getInt("duration") ?: 60
                    val textPurchasedArg = backStackEntry.arguments?.getString("textPurchased")
                    val textRejectedArg = backStackEntry.arguments?.getString("textRejected")
                    
                    val textPurchased = if (textPurchasedArg.isNullOrBlank()) null else textPurchasedArg
                    val textRejected = if (textRejectedArg.isNullOrBlank()) null else textRejectedArg

                    val userProfile by authViewModel.userProfile.collectAsState()
                    val currentPurchase by purchaseViewModel.currentPurchase.collectAsState()
                    val itemPrice = currentPurchase?.price?.filter { it.isDigit() }?.toDoubleOrNull() ?: 0.0

                    BreathingScreen(
                        durationSeconds = duration,
                        textPurchased = textPurchased,
                        textRejected = textRejected,
                        monthlySavings = userProfile?.monthly_savings,
                        itemPrice = itemPrice,
                        onIChangedMyMind = { text ->
                            purchaseViewModel.confirmRefusal {
                                navController.navigate(Screen.Home.createRoute(text)) { 
                                    popUpTo(Screen.Home.route) { inclusive = true } 
                                } 
                            }
                        },
                        onPurchaseConfirmed = { text ->
                            purchaseViewModel.confirmBreakdown {
                                navController.navigate(Screen.Home.createRoute(text)) { 
                                    popUpTo(Screen.Home.route) { inclusive = true } 
                                } 
                            }
                        }
                    )
                }
                composable(Screen.History.route) {
                    HistoryScreen(onBackClick = { navController.popBackStack() })
                }
                composable(Screen.SelfRestraintOnboarding.route) {
                    SelfRestraintScreen(
                         onBackClick = { navController.popBackStack() }
                    )
                }
                composable(Screen.PromptOnboarding.route) {
                    PromptScreen(
                         onBackClick = { navController.popBackStack() }
                    )
                }
            }
        }

        if (shouldShowBottomBar(currentRoute)) {
            Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                BottomBar(navController = navController)
            }
        }
    }
}

fun shouldShowBottomBar(route: String?): Boolean {
    return route in listOf(
        Screen.Home.route,
        Screen.Friends.route,
        Screen.Freezer.route,
        Screen.Profile.route,
        Screen.Purchase.route,
        Screen.Verdict.route,
        Screen.Chat.route,
        Screen.History.route,
        Screen.Breathing.route,
        Screen.CreateSharedGoal.route,
        Screen.SharedGoalDetail.route
    )
}

sealed class Screen(val route: String, val resourceId: Int? = null) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object EmailLogin : Screen("email_login")
    object Registration : Screen("registration")
    object Onboarding : Screen("onboarding")
    object IncomeOnboarding : Screen("income_onboarding")
    object SavingsOnboarding : Screen("savings_onboarding")
    object WeaknessOnboarding : Screen("weakness_onboarding")
    object DreamOnboarding : Screen("dream_onboarding")
    object GoalAmountOnboarding : Screen("goal_amount_onboarding")
    object GoalDateOnboarding : Screen("goal_date_onboarding")
    object Home : Screen("home?message={message}", R.string.home) {
        fun createRoute(message: String?): String {
            return if (message.isNullOrBlank()) "home" else "home?message=${android.net.Uri.encode(message)}"
        }
    }
    object Friends : Screen("friends", R.string.friends)
    object Freezer : Screen("freezer", R.string.freezer)
    object Profile : Screen("profile", R.string.profile)
    object Settings : Screen("settings")
    object Purchase : Screen("purchase")
    object Verdict : Screen("verdict")
    object Chat : Screen("chat?product={product}&price={price}&emotions={emotions}&starter={starter}") {
        fun createRoute(product: String, price: String, emotions: String, starter: String?): String {
            val p = android.net.Uri.encode(product)
            val pr = android.net.Uri.encode(price)
            val e = android.net.Uri.encode(emotions)
            val s = if (starter != null) android.net.Uri.encode(starter) else ""
            return "chat?product=$p&price=$pr&emotions=$e&starter=$s"
        }
    }
    object Breathing : Screen("breathing/{duration}?textPurchased={textPurchased}&textRejected={textRejected}") {
        fun createRoute(duration: Int, textPurchased: String?, textRejected: String?): String {
            val tp = if (textPurchased != null) android.net.Uri.encode(textPurchased) else ""
            val tr = if (textRejected != null) android.net.Uri.encode(textRejected) else ""
            return "breathing/$duration?textPurchased=$tp&textRejected=$tr"
        }
    }
    object History : Screen("history")
    object CreateSharedGoal : Screen("create_shared_goal")
    object SharedGoalDetail : Screen("shared_goal_detail")
    object SelfRestraintOnboarding : Screen("self_restraint_onboarding")
    object PromptOnboarding : Screen("prompt_onboarding")
}
