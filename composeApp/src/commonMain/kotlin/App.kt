import androidx.compose.runtime.*
import global.AppTheme
import constant.RouteConstants
import moe.tlaster.precompose.PreComposeApp
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.rememberNavigator
import moe.tlaster.precompose.navigation.transition.NavTransition
import org.jetbrains.compose.ui.tooling.preview.Preview
import scene.HomeScene
import scene.LoginScene
import store.AppStore

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
                    HomeScene()
                }
            }
        }
    }
}