package caios.android.pictogram.utils

import android.util.Log

object LogUtils {

    const val TAG = "<LOG>"
    const val STARTUP_TAG = "<START-UP>"
    const val EXCEPTION_TAG = "<EXCEPTION>"

    fun tryCatch(isTakeLog: Boolean = true, f: () -> (Unit)) {
        try {
            f()
        } catch (e: Throwable) {
            if (isTakeLog) Log.e(TAG, "tryCatch: ${e.message}", e)
        }
    }
}