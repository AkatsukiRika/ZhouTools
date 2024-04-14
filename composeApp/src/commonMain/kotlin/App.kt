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

@Composable
@Preview
fun App() {
    PreComposeApp {
        val navigator = rememberNavigator()
        NavHost(
            navigator = navigator,
            navTransition = NavTransition(),
            initialRoute = RouteConstants.ROUTE_HOME
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