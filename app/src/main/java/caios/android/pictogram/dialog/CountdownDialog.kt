package caios.android.pictogram.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewTreeObserver
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import caios.android.pictogram.databinding.DialogCountdownBinding
import caios.android.pictogram.utils.LogUtils.TAG
import caios.android.pictogram.utils.ThemeUtils
import caios.android.pictogram.utils.autoCleared
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.math.ceil

class CountdownDialog: DialogFragment() {

    private var binding by autoCleared<DialogCountdownBinding>()
    private val handler = Handler(Looper.myLooper()!!)

    private val countdown by lazy { requireArguments().getInt("countdown")}
    private var remainTime = 0.0f
    private var dismissListener: (() -> Unit)? = null

    private var countdownProcess = object : Runnable {
        override fun run() {
            if(remainTime >= 0) {
                binding.countdownText.text = ceil(remainTime).toInt().toString()
                binding.progressBar.progress = countdown - (remainTime * 100)
                remainTime -= 0.01f

                handler.postDelayed(this, 10)
            } else {
                dismiss()
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogCountdownBinding.inflate(layoutInflater)

        remainTime = countdown.toFloat()
        isCancelable = false

        binding.countdownText.text = remainTime.toInt().toString()
        binding.progressBar.progressMax = remainTime.toInt() * 100f

        handler.post(countdownProcess)

        return MaterialAlertDialogBuilder(requireContext()).apply {
            setView(binding.root)
            setCancelable(false)
        }.create().also {
            ThemeUtils.setFullScreen(it.window!!)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        handler.removeCallbacks(countdownProcess)
        dismissListener?.let { it() }

        super.onDismiss(dialog)
    }

    fun setOnDismissListener(f: () -> Unit) {
        dismissListener = f
    }

    companion object {
        fun build(countdown: Int, dismissListener: () -> (Unit)): CountdownDialog {
            return CountdownDialog().apply {
                setOnDismissListener(dismissListener)
                arguments = Bundle().apply {
                    putInt("countdown", countdown)
                }
            }
        }
    }
}