package caios.android.pictogram.fragment

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import caios.android.pictogram.R
import caios.android.pictogram.databinding.FragmentResultBinding
import caios.android.pictogram.utils.autoCleared
import com.google.android.material.transition.MaterialSharedAxis

class ResultFragment: Fragment(R.layout.fragment_result) {

    private var binding by autoCleared<FragmentResultBinding>()
    private val args by navArgs<ResultFragmentArgs>()

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

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentResultBinding.bind(view)

        binding.timeText.text = "${getString(R.string.clearTime)}: %.2f ${getString(R.string.second)}".format(args.time)
        binding.rankingText.text = if(args.rank != -1) "${getString(R.string.ranking)}: ${args.rank} ${getString(R.string.rank)}" else getString(R.string.failedToRegisterRanking)

        binding.endButton.setOnClickListener {
            findNavController().navigate(R.id.action_resultFragment_to_startingFragment)
        }
    }

    override fun onResume() {
        super.onResume()

        binding.confettiView.build().apply {
            addColors(Color.YELLOW, Color.GREEN, Color.MAGENTA, Color.BLUE)
            setDirection(0.0, 359.0)
            setSpeed(0.1f)
            setPosition(-50f, binding.confettiView.width + 50f, -50f, -50f)
            setFadeOutEnabled(true)
            streamFor(50, 5000)
        }
    }

    override fun onPause() {
        super.onPause()
        binding.confettiView.reset()
    }
}