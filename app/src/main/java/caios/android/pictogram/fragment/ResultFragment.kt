package caios.android.pictogram.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import caios.android.pictogram.R
import caios.android.pictogram.databinding.FragmentResultBinding
import caios.android.pictogram.utils.autoCleared
import com.google.android.material.transition.MaterialSharedAxis
import nl.dionsegijn.konfetti.emitters.StreamEmitter

class ResultFragment: Fragment(R.layout.fragment_result) {

    private var binding by autoCleared<FragmentResultBinding>()

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

    override fun onResume() {
        super.onResume()

        binding.confettiView.build().apply {
            addColors(Color.YELLOW, Color.GREEN, Color.MAGENTA, Color.BLUE)
            setDirection(0.0, 359.0)
            setSpeed(0.1f, 1f)
            setPosition(-50f, binding.confettiView.width + 50f, -50f, -50f)
            setFadeOutEnabled(true)
            streamFor(300, StreamEmitter.INDEFINITE)
        }
    }

    override fun onPause() {
        super.onPause()

        binding.confettiView.reset()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding = FragmentResultBinding.bind(view)

    }
}