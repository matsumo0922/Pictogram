package caios.android.pictogram.dialog

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import caios.android.pictogram.R
import caios.android.pictogram.databinding.DialogRuleBinding
import caios.android.pictogram.utils.autoCleared
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialSharedAxis

class RuleDialog: DialogFragment() {

    private var binding by autoCleared<DialogRuleBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val animDuration = resources.getInteger(R.integer.anim_duration_fragment_transition).toLong()
        val enterSharedAxisTransition = MaterialSharedAxis(MaterialSharedAxis.X, true).apply { duration = animDuration }
        val returnSharedAxisTransition = MaterialSharedAxis(MaterialSharedAxis.X, false).apply { duration = animDuration }

        enterTransition = enterSharedAxisTransition
        returnTransition = returnSharedAxisTransition

        exitTransition = enterSharedAxisTransition
        reenterTransition = returnSharedAxisTransition
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        binding = DialogRuleBinding.inflate(layoutInflater)

        return MaterialAlertDialogBuilder(requireContext()).apply {
            setView(binding.root)
            setPositiveButton("OK", null)
            setNeutralButton(R.string.seePictogramPerformance) { _, _ ->
                startActivity(Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://www.youtube.com/watch?v=Y-q7URCY7vY")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }
        }.create()
    }

    companion object {
        fun build(): RuleDialog {
            return RuleDialog()
        }
    }
}