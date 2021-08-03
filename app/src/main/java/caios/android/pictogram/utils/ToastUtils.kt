package caios.android.pictogram.utils

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

object ToastUtils {

    private var currentToast: Toast? = null

    fun show(context: Context, text: String, duration: Int = Toast.LENGTH_SHORT, doCancel: Boolean = true) {
        if (doCancel) cancel()
        currentToast = Toast.makeText(context, text, duration)
        currentToast?.show()
    }

    fun show(context: Context, @StringRes textId: Int, duration: Int = Toast.LENGTH_SHORT, doCancel: Boolean = true) {
        if (doCancel) cancel()
        currentToast = Toast.makeText(context, textId, duration)
        currentToast?.show()
    }

    fun cancel() {
        currentToast?.cancel()
    }

}