import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.*
import global.AppTheme
import constant.RouteConstants
import helper.NetworkHelper
import model.request.LoginRequest
import moe.tlaster.precompose.PreComposeApp
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.SwipeProperties
import moe.tlaster.precompose.navigation.path
import moe.tlaster.precompose.navigation.rememberNavigator
import moe.tlaster.precompose.navigation.transition.NavTransition
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.lighthousegames.logging.logging
import ui.scene.HomeScene
import ui.scene.LoginScene
import store.AppStore
import ui.scene.AddScheduleScene
import ui.scene.DepositStatsScene
import ui.scene.ExportDataScene
import ui.scene.SignUpScene
import ui.scene.SyncScene
import ui.scene.WriteMemoScene
import ui.scene.detail.DetailScene

val logger = logging("App")

@Composable
@Preview
fun App() {
    PreComposeApp {
        val navigator = rememberNavigator()
        val currentEntry = navigator.currentEntry.collectAsState(initial = null).value
        var swipeProperties: SwipeProperties? = remember { SwipeProperties() }
        val isLogin = AppStore.loginToken.isNotBlank()
        val navTransition = remember {
            NavTransition(
                createTransition = slideInHorizontally(animationSpec = tween(easing = LinearEasing)) { it },
                destroyTransition = slideOutHorizontally(animationSpec = tween(easing = LinearEasing)) { it },
                pauseTransition = slideOutHorizontally { -it / 4 },
                resumeTransition = slideInHorizontally { -it / 4 },
                exitTargetContentZIndex = 1f
            )
        }

        LaunchedEffect(currentEntry) {
            val currentRoute = currentEntry?.route?.route
            swipeProperties = if (currentRoute == RouteConstants.ROUTE_HOME) {
                null
            } else {
                SwipeProperties()
            }
        }

        NavHost(
            navigator = navigator,
            swipeProperties = swipeProperties,
            navTransition = navTransition,
            initialRoute = if (isLogin) RouteConstants.ROUTE_HOME else RouteConstants.ROUTE_LOGIN
        ) {
            scene(
                route = RouteConstants.ROUTE_LOGIN,
                navTransition = navTransition
            ) {
                AppTheme {
                    LoginScene(navigator)
                }
            }

            scene(
                route = RouteConstants.ROUTE_SIGN_UP,
                navTransition = navTransition
            ) {
                AppTheme {
                    SignUpScene(navigator)
                }
            }

            scene(
                route = RouteConstants.ROUTE_HOME,
                navTransition = navTransition
            ) {
                AppTheme {
                    HomeScene(navigator)
                }
            }

            scene(
                route = RouteConstants.ROUTE_DETAILS,
                navTransition = navTransition
            ) {
                AppTheme {
                    DetailScene(navigator)
                }
            }

            scene(
                route = RouteConstants.ROUTE_WRITE_MEMO,
                navTransition = navTransition
            ) {
                val isEdit = it.path<Boolean>("edit") ?: false

                AppTheme {
                    WriteMemoScene(navigator, isEdit)
                }
            }

            scene(
                route = RouteConstants.ROUTE_ADD_SCHEDULE,
                navTransition = navTransition
            ) {
                AppTheme {
                    AddScheduleScene(navigator)
                }
            }

            scene(
                route = RouteConstants.ROUTE_SYNC,
                navTransition = navTransition
            ) {
                val mode = it.path<String>("mode") ?: ""

                AppTheme {
                    SyncScene(navigator, mode)
                }
            }

            scene(
                route = RouteConstants.ROUTE_EXPORT,
                navTransition = navTransition
            ) {
                AppTheme {
                    ExportDataScene(navigator)
                }
            }

            scene(
                route = RouteConstants.ROUTE_DEPOSIT_STATS,
                navTransition = navTransition
            ) {
                AppTheme {
                    DepositStatsScene(navigator)
                }
            }
        }

        LaunchedEffect(Unit) {
            logger.i { "Welcome to Zhou Tools!" }
            checkLoginValidity()
        }
    }
}

/**
 * Check login token validity and login again when token is invalid.
 */
private suspend fun checkLoginValidity() {
    logger.i { "checkLoginValidity: loginToken=${AppStore.loginToken}, loginUsername=${AppStore.loginUsername}" }
    if (AppStore.loginToken.isNotEmpty() && AppStore.loginUsername.isNotEmpty() && AppStore.loginPassword.isNotEmpty()) {
        val isValid = NetworkHelper.checkTokenValidity(AppStore.loginToken)
        logger.i { "checkLoginValidity: isValid=$isValid" }
        if (!isValid) {
            // login again
            val loginRequest = LoginRequest(username = AppStore.loginUsername, password = AppStore.loginPassword)
            val loginResponse = NetworkHelper.login(loginRequest)
            val isSuccess = loginResponse.first
            logger.i { "checkLoginValidity: login isSuccess=$isSuccess" }
            if (isSuccess) {
                val token = loginResponse.second
                logger.i { "checkLoginValidity: new token=$token" }
                if (token != null) {
                    AppStore.loginToken = token
                }
            }
        }
    }
}