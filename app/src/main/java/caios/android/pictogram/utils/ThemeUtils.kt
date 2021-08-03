package caios.android.pictogram.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatDelegate
import caios.android.pictogram.R
import caios.android.pictogram.global.setting

object ThemeUtils {

    fun setAppTheme() {
        setTheme(setting.appTheme)
    }

    fun setTheme(theme: Theme) {
        when (theme) {
            Theme.Light -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            Theme.Dark -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            Theme.System -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    fun isDarkMode(context: Context): Boolean {
        return when (setting.appTheme) {
            Theme.Light -> false
            Theme.Dark -> true
            Theme.System -> when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_NO  -> false
                Configuration.UI_MODE_NIGHT_YES -> true
                else                            -> false
            }
        }
    }

    fun setFullScreen(window: Window, isHideStatusBar: Boolean = true, isHideNavigationBar: Boolean = true) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            window.insetsController?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            window.decorView.systemUiVisibility = when {
                isHideStatusBar && isHideNavigationBar -> View.SYSTEM_UI_FLAG_IMMERSIVE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                isHideStatusBar                        -> View.SYSTEM_UI_FLAG_IMMERSIVE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                isHideNavigationBar                    -> View.SYSTEM_UI_FLAG_IMMERSIVE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                else                                   -> 0
            }
        }
    }

    fun setThemeIcon(context: Context, window: Window) {
        if (isDarkMode(context)) setDarkThemeIcon(window)
        else setLightThemeIcon(window)
    }

    private fun setLightThemeIcon(window: Window) {
        window.decorView.systemUiVisibility = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            }
            else -> {
                window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
            /*else                                           -> {
                window.decorView.systemUiVisibility or 0
            }*/
        }
    }

    private fun setDarkThemeIcon(window: Window) {
        window.decorView.systemUiVisibility = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv() and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
            }
            else -> {
                window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }
            /*else                                           -> {
                window.decorView.systemUiVisibility or 0
            }*/
        }
    }

    fun getThemeString(context: Context, any: Any) = when (any) {
        Theme.Light        -> context.getString(R.string.lightTheme)
        Theme.Dark         -> context.getString(R.string.darkTheme)
        Theme.System       -> context.getString(R.string.systemTheme)
        else               -> context.getString(R.string.unknown)
    }

    enum class Theme {
        Light, Dark, System
    }
}