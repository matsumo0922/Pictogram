package caios.android.pictogram.fragment

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import caios.android.pictogram.R
import caios.android.pictogram.adapter.RankingAdapter
import caios.android.pictogram.databinding.FragmentRankingBinding
import caios.android.pictogram.game.ClearData
import caios.android.pictogram.global.ranking
import caios.android.pictogram.utils.ThemeUtils
import caios.android.pictogram.utils.autoCleared
import com.google.android.material.transition.MaterialSharedAxis

class RankingFragment: Fragment(R.layout.fragment_ranking) {

    private var binding by autoCleared<FragmentRankingBinding>()

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding = FragmentRankingBinding.bind(view)

        binding.toolbar.title = getString(R.string.ranking)
        binding.toolbar.setNavigationIcon(R.drawable.vec_navigation_arrow)

        val iconColor = if (ThemeUtils.isDarkMode(requireContext())) android.R.color.white else android.R.color.black
        binding.toolbar.navigationIcon?.setTint(ContextCompat.getColor(requireContext(), R.color.colorIcon))
        binding.toolbar.overflowIcon?.setTint(ContextCompat.getColor(requireContext(), iconColor))

        val dataList = createDataList()

        if(dataList.isNotEmpty()) {
            binding.recyclerView.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(requireContext())
                adapter = RankingAdapter(requireContext(), dataList)
            }
        } else {
            binding.recyclerView.visibility = View.GONE
            binding.noRankingText.visibility = View.VISIBLE
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun createDataList(): List<Pair<ClearData, Long>> {
        val clearDataList = ranking.getAllData()
        return clearDataList.map { Pair(it, it.eventData.sumOf { event -> event.time }) }
    }
}