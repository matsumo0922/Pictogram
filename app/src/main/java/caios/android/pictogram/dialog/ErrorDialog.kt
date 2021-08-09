package caios.android.pictogram.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import caios.android.pictogram.R
import caios.android.pictogram.analyze.Device
import caios.android.pictogram.databinding.DialogErrorBinding
import caios.android.pictogram.global.SettingClass
import caios.android.pictogram.global.setting
import caios.android.pictogram.utils.autoCleared
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ErrorDialog: DialogFragment() {

    private var binding by autoCleared<DialogErrorBinding>()
    private val stackTrace by lazy { requireArguments().getStringArray("stacktrace")!! }

    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogErrorBinding.inflate(layoutInflater)

        binding.stackTraceText.text = stackTrace.joinToString("\n")

        if (setting.getString(SettingClass.PROCESSING_METHOD, Device.CPU.name) != Device.CPU.name) {
            binding.summaryText.text = getString(R.string.criticalErrorSum) + getString(R.string.adviceProcessingMethod)
        }

        return MaterialAlertDialogBuilder(requireContext()).apply {
            setView(binding.root)
            setPositiveButton("OK") { _, _ ->
                dismiss()
            }
        }.create()
    }

    companion object {
        fun build(e: Throwable): ErrorDialog {
            return ErrorDialog().apply {
                arguments = Bundle().apply {
                    putStringArray("stacktrace", mutableListOf<String>().apply {
                        add(e.javaClass.name + ": " + e.message)
                        addAll(e.stackTrace.map { it.toString() }.toList())
                    }.toTypedArray())
                }
            }
        }
    }
}