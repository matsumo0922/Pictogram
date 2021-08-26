package caios.android.pictogram.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import caios.android.pictogram.R
import caios.android.pictogram.databinding.DialogEditNameBinding
import caios.android.pictogram.utils.autoCleared
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class EditNameDialog: DialogFragment() {

    private var binding by autoCleared<DialogEditNameBinding>()
    private var positiveListener: ((String) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        binding = DialogEditNameBinding.inflate(layoutInflater)

        return MaterialAlertDialogBuilder(requireContext()).apply {
            setView(binding.root)
            setPositiveButton("OK") { _, _ ->
                positiveListener?.let { it(binding.textInputEditText.text.toString()) }
            }
            setNegativeButton(R.string.cancel, null)
        }.create()
    }

    fun setPositiveListener(f: (String) -> Unit) {
        positiveListener = f
    }

    companion object {
        fun build(f: (String) -> Unit): EditNameDialog {
            return EditNameDialog().apply {
                setPositiveListener(f)
            }
        }
    }
}