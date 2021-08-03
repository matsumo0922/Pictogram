package caios.android.pictogram.utils

import android.content.Context
import android.graphics.Point
import android.os.Build
import android.util.Size
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager

object SystemUtils {

    fun convertDpToPx(context: Context, dp: Float): Float {
        val metrics = context.resources.displayMetrics
        return dp * metrics.density
    }

    fun convertPxToDp(context: Context, px: Int): Float {
        val metrics = context.resources.displayMetrics
        return px / metrics.density
    }

    fun getDisplaySize(windowManager: WindowManager): Size {
        val width: Int
        val height: Int

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMatrix = windowManager.currentWindowMetrics
            width = windowMatrix.bounds.width()
            height = windowMatrix.bounds.height()
        } else {
            val display = windowManager.defaultDisplay
            val point = Point()

            display.getRealSize(point)

            width = point.x
            height = point.y
        }

        return Size(width, height)
    }

    fun getDisplaySizeDp(context: Context, windowManager: WindowManager): Size {
        val displayPx = getDisplaySize(windowManager)
        return Size(convertPxToDp(context, displayPx.width).toInt(), convertPxToDp(context, displayPx.height).toInt())
    }
}