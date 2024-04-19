import androidx.compose.runtime.*
import api.NetworkApi
import global.AppTheme
import constant.RouteConstants
import moe.tlaster.precompose.PreComposeApp
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.rememberNavigator
import moe.tlaster.precompose.navigation.transition.NavTransition
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.lighthousegames.logging.logging
import ui.scene.HomeScene
import ui.scene.LoginScene
import store.AppStore
import ui.scene.DetailScene

val logger = logging("App")
val networkApi = NetworkApi()

@Composable
@Preview
fun App() {
    PreComposeApp {
        val navigator = rememberNavigator()
        val isLogin = AppStore.loginToken.isNotBlank()

        NavHost(
            navigator = navigator,
            navTransition = NavTransition(),
            initialRoute = if (isLogin) RouteConstants.ROUTE_HOME else RouteConstants.ROUTE_LOGIN
        ) {
            scene(
                route = RouteConstants.ROUTE_LOGIN,
                navTransition = NavTransition()
            ) {
                AppTheme {
                    LoginScene(navigator)
                }
            }

            scene(
                route = RouteConstants.ROUTE_HOME,
                navTransition = NavTransition()
            ) {
                AppTheme {
                    HomeScene(navigator)
                }
            }

            scene(
                route = RouteConstants.ROUTE_DETAILS,
                navTransition = NavTransition()
            ) {
                AppTheme {
                    DetailScene(navigator)
                }
            }
        }

        LaunchedEffect(Unit) {
            logger.i { "Welcome to Zhou Tools!" }
        }
    }
}