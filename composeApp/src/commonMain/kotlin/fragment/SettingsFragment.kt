package fragment

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import moe.tlaster.precompose.navigation.Navigator
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import store.AppStore
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.ic_logout
import zhoutools.composeapp.generated.resources.logout

@OptIn(ExperimentalResourceApi::class)
@Composable
fun SettingsFragment(modifier: Modifier = Modifier, navigator: Navigator) {
    fun logout() {
        AppStore.loginToken = ""
        navigator.popBackStack()
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    logout()
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_logout),
                contentDescription = null,
                modifier = Modifier
                    .padding(vertical = 8.dp, horizontal = 16.dp)
                    .size(26.dp),
                tint = Color.Unspecified
            )

            Text(
                text = stringResource(Res.string.logout),
                fontSize = 16.sp
            )
        }
    }
}