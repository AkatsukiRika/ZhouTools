import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import constant.RouteConstants
import global.AppTheme
import helper.NetworkHelper
import model.request.LoginRequest
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.lighthousegames.logging.logging
import store.AppStore
import ui.scene.AddScheduleScene
import ui.scene.DepositStatsScene
import ui.scene.ExportDataScene
import ui.scene.HomeScene
import ui.scene.LoginScene
import ui.scene.SignUpScene
import ui.scene.SyncScene
import ui.scene.WriteMemoScene
import ui.scene.detail.DetailScene

val logger = logging("App")

@Composable
@Preview
fun App() {
    val navController = rememberNavController()
    val isLogin = AppStore.loginToken.isNotBlank()
    val animationSpec = remember { tween<IntOffset>() }

    NavHost(
        navController = navController,
        startDestination = if (isLogin) RouteConstants.ROUTE_HOME else RouteConstants.ROUTE_LOGIN,
        enterTransition = { slideInHorizontally(animationSpec = animationSpec) { it } },
        exitTransition = { slideOutHorizontally(animationSpec = animationSpec) { -it / 4 } },
        popEnterTransition = { slideInHorizontally(animationSpec = animationSpec) { -it / 4 } },
        popExitTransition = { slideOutHorizontally(animationSpec = animationSpec) { it } },
    ) {
        composable(
            route = RouteConstants.ROUTE_LOGIN,
        ) {
            AppTheme {
                LoginScene(navController)
            }
        }

        composable(
            route = RouteConstants.ROUTE_SIGN_UP,
        ) {
            AppTheme {
                SignUpScene(navController)
            }
        }

        composable(
            route = RouteConstants.ROUTE_HOME,
        ) {
            AppTheme {
                HomeScene(navController)
            }
        }

        composable(
            route = RouteConstants.ROUTE_DETAILS,
        ) {
            AppTheme {
                DetailScene(navController)
            }
        }

        composable(
            route = RouteConstants.ROUTE_WRITE_MEMO,
            arguments = listOf(navArgument("edit") {
                type = NavType.BoolType
                defaultValue = false
            })
        ) {
            val isEdit = it.arguments?.getBoolean("edit") ?: false

            AppTheme {
                WriteMemoScene(navController, isEdit)
            }
        }

        composable(
            route = RouteConstants.ROUTE_ADD_SCHEDULE,
        ) {
            AppTheme {
                AddScheduleScene(navController)
            }
        }

        composable(
            route = RouteConstants.ROUTE_SYNC,
            arguments = listOf(navArgument("mode") {
                type = NavType.StringType
            })
        ) {
            val mode = it.arguments?.getString("mode") ?: ""

            AppTheme {
                SyncScene(navController, mode)
            }
        }

        composable(
            route = RouteConstants.ROUTE_EXPORT,
        ) {
            AppTheme {
                ExportDataScene(navController)
            }
        }

        composable(
            route = RouteConstants.ROUTE_DEPOSIT_STATS,
        ) {
            AppTheme {
                DepositStatsScene(navController)
            }
        }
    }

    LaunchedEffect(Unit) {
        logger.i { "Welcome to Zhou Tools!" }
        checkLoginValidity()
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