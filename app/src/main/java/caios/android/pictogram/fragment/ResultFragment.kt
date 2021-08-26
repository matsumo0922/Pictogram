package caios.android.pictogram.fragment

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import caios.android.pictogram.R
import caios.android.pictogram.activity.MainActivity
import caios.android.pictogram.adapter.RankingAdapter
import caios.android.pictogram.data.getEventResource
import caios.android.pictogram.databinding.FragmentResultBinding
import caios.android.pictogram.dialog.EditNameDialog
import caios.android.pictogram.game.ClearData
import caios.android.pictogram.game.sortedRanking
import caios.android.pictogram.global.ranking
import caios.android.pictogram.utils.autoCleared
import com.google.android.material.transition.MaterialSharedAxis
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size
import kotlin.random.Random

class ResultFragment: Fragment(R.layout.fragment_result) {

    private var binding by autoCleared<FragmentResultBinding>()
    private val args by navArgs<ResultFragmentArgs>()

    private val handler = Handler(Looper.myLooper()!!)
    private val random = Random(System.currentTimeMillis())

    private var clearDataList = mutableListOf<Pair<ClearData, Long>>()
    private var rankingAdapter: RankingAdapter? = null

    private val confettiProcess = object : Runnable {
        override fun run() {
            val x = binding.confettiView.x.toInt() + (random.nextFloat() * binding.confettiView.width)
            val y = binding.confettiView.y.toInt() + (random.nextFloat() * binding.confettiView.height)
            confettiBurst(x, y)
            handler.postDelayed(this, 850)
        }
    }

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

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentResultBinding.bind(view)

        createDataList()

        ranking.getData(args.date)?.let { setClearInfo(it) }
        rankingAdapter = RankingAdapter(requireContext(), clearDataList)

        binding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = rankingAdapter
        }

        binding.editNameButton.setOnClickListener {
            EditNameDialog.build {
                ranking.setChallengerName(it, args.date)

                createDataList()
                rankingAdapter?.notifyDataSetChanged()

            }.show(childFragmentManager, null)
        }

        binding.endButton.setOnClickListener {
            findNavController().navigate(R.id.action_resultFragment_to_startingFragment)
        }

        (activity as MainActivity).onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_resultFragment_to_startingFragment)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        handler.post(confettiProcess)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(confettiProcess)
    }

    @SuppressLint("SetTextI18n")
    private fun setClearInfo(clearData: ClearData) {
        binding.clearTime.text = "${getString(R.string.clearTime)}: %.2f ${getString(R.string.second)}".format(clearData.eventData.sumOf { event -> event.time }.toFloat() / 1000)
        binding.ranking.text = if(args.rank != -1) "${getString(R.string.ranking)}: ${args.rank} ${getString(R.string.rank)}" else getString(R.string.failedToRegisterRanking)

        binding.eventImage1.setImageResource(getEventResource(clearData.eventData[0].event))
        binding.eventImage2.setImageResource(getEventResource(clearData.eventData[1].event))
        binding.eventImage3.setImageResource(getEventResource(clearData.eventData[2].event))
        binding.eventImage4.setImageResource(getEventResource(clearData.eventData[3].event))
        binding.eventImage5.setImageResource(getEventResource(clearData.eventData[4].event))

        binding.eventTime1.text = clearData.eventData[0].time.toSecond()
        binding.eventTime2.text = clearData.eventData[1].time.toSecond()
        binding.eventTime3.text = clearData.eventData[2].time.toSecond()
        binding.eventTime4.text = clearData.eventData[3].time.toSecond()
        binding.eventTime5.text = clearData.eventData[4].time.toSecond()
    }

    private fun createDataList() {
        val dataList = ranking.getAllData()
        val newList = dataList.sortedRanking().map { Pair(it, it.eventData.sumOf { event -> event.time }) }

        clearDataList.clear()
        clearDataList.addAll(newList)
    }

    private fun confettiBurst(x: Float, y: Float) {
        binding.confettiView.build().apply {
            addColors(Color.YELLOW, Color.GREEN, Color.MAGENTA, Color.BLUE)
            setDirection(0.0, 359.0)
            setSpeed(0.5f, 4f)
            setFadeOutEnabled(true)
            setTimeToLive(750L)
            addShapes(Shape.RECT, Shape.CIRCLE)
            addSizes(Size(10))
            setPosition(x, y)
        }.burst(100)
    }

    fun Long.toSecond(): String {
        return "%.2f ${requireContext().getString(R.string.second)}".format(this.toFloat() / 1000)
    }
}