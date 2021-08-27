package caios.android.pictogram.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import caios.android.pictogram.R
import caios.android.pictogram.activity.SettingActivity
import caios.android.pictogram.data.PictogramEvent
import caios.android.pictogram.data.getEventResource
import caios.android.pictogram.databinding.FragmentStartBinding
import caios.android.pictogram.dialog.RuleDialog
import caios.android.pictogram.utils.PermissionUtils
import caios.android.pictogram.utils.ToastUtils
import caios.android.pictogram.utils.autoCleared
import com.google.android.material.transition.MaterialSharedAxis

class StartFragment: Fragment(R.layout.fragment_start) {

    private var binding by autoCleared<FragmentStartBinding>()
    private var permissionRequestCount = 0

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

        binding = FragmentStartBinding.bind(view)

        binding.startButton.setOnClickListener {
            if(requirePermission()) {
                findNavController().navigate(R.id.action_startingFragment_to_gameFragment)
            }
        }

        binding.ruleButton.setOnClickListener {
            RuleDialog.build().show(childFragmentManager, null)
        }

        binding.settingButton.setOnClickListener {
            startActivity(Intent(requireContext(), SettingActivity::class.java))
        }

        // 非公開
        binding.rankingButton.setOnClickListener {
            findNavController().navigate(R.id.action_startingFragment_to_rankingFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        setWelcomePictogram()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == PERMISSION_REQUEST_ID) {
            if(PermissionUtils.isAllowed(requireContext(), PermissionUtils.requestPermissions)) {
                findNavController().navigate(R.id.action_startingFragment_to_gameFragment)
            } else {
                ToastUtils.show(requireContext(), R.string.permissionReject)
            }
        }
    }

    private fun requirePermission(): Boolean {
        if(!PermissionUtils.isAllowed(requireContext(), PermissionUtils.requestPermissions)){
            if(PermissionUtils.isShouldRequest(this, PermissionUtils.requestPermissions) || permissionRequestCount == 0) {
                PermissionUtils.requestPermission(this, PermissionUtils.requestPermissions, PERMISSION_REQUEST_ID)
                permissionRequestCount++
            } else {
                ToastUtils.show(requireContext(), R.string.requirePermission)
                PermissionUtils.startAppInfoActivity(requireContext())
            }
            return false
        } else {
            return  true
        }
    }

    private fun setWelcomePictogram() {
        enumValues<PictogramEvent>().toMutableList().shuffled().also {
            binding.pictogramMainImage.setImageResource(getEventResource(it.elementAtOrNull(0) ?: return))
            binding.pictogramSubImage.setImageResource(getEventResource(it.elementAtOrNull(1) ?: return))
        }
    }

    companion object {
        const val PERMISSION_REQUEST_ID = 992
    }
}