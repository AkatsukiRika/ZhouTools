package global

import androidx.compose.material3.FilterChipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object AppColors {
    val Background = Color(0xFFF4F4F4)
    val DarkBackground = Color(0xFF202124)
    val Theme = Color(0xFFD685A9)
    val LightTheme = Color(0xFFE9A6B3)
    val SlightTheme = Color(0xFFFFEAE3)
    val Divider = Color(0x26333333)
    val LightGreen = Color(0xFF8DECB4)
    val DarkGreen = Color(0xFF41B06E)
    val Red = Color(0xFFC40C0C)
    val LightGold = Color(0xFFE1B84C)
    val Yellow = Color(0xFFFFD95F)

    @Composable
    fun getChipColors() = FilterChipDefaults.elevatedFilterChipColors(
        containerColor = SlightTheme,
        selectedContainerColor = Theme,
        selectedLabelColor = Color.White
    )
}