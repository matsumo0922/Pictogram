package caios.android.pictogram.fragment

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import caios.android.pictogram.BuildConfig
import caios.android.pictogram.R
import caios.android.pictogram.activity.SettingActivity
import caios.android.pictogram.analyze.Device
import caios.android.pictogram.global.SettingClass
import caios.android.pictogram.global.SettingClass.Companion.PROCESSING_METHOD
import caios.android.pictogram.global.setting
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialSharedAxis

class NormalSettingFragment: PreferenceFragmentCompat() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true).apply { duration = 300 }
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false).apply { duration = 300 }
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true).apply { duration = 300 }
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false).apply { duration = 300 }
    }

    override fun onResume() {
        super.onResume()
        (activity as SettingActivity).setToolbarTitle(R.string.setting)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.normal_setting, rootKey)

        val processingMethod = preferenceManager.findPreference<Preference>("ProcessingMethod")
        val openSourceLicense = preferenceManager.findPreference<Preference>("OpenSourceLicense")
        val version = preferenceManager.findPreference<Preference>("Version")

        version?.summary = "${BuildConfig.VERSION_NAME}:${BuildConfig.VERSION_CODE}"

        processingMethod?.setOnPreferenceClickListener {
            var checked = -1
            val selected = Device.valueOf(setting.getString(PROCESSING_METHOD, Device.CPU.name))
            val itemMap = mutableMapOf(Device.CPU to "CPU", Device.GPU to "GPU")

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                itemMap[Device.NNAPI] = "Android Neural Networks API"
            }

            MaterialAlertDialogBuilder(requireContext()).apply {
                setTitle(R.string.processingMethod)
                setSingleChoiceItems(itemMap.values.toTypedArray(), itemMap.keys.indexOf(selected)) { _, which -> checked = which }
                setPositiveButton("OK") { _, _ ->
                    if(checked != -1 && selected != itemMap.keys.toList()[checked]) {
                        setting.setString(PROCESSING_METHOD, itemMap.keys.toList()[checked].name)
                    }
                }
            }.show()

            return@setOnPreferenceClickListener true
        }

        openSourceLicense?.setOnPreferenceClickListener {
            startActivity(Intent(requireContext(), OssLicensesMenuActivity::class.java).apply {
                putExtra("title", getString(R.string.openSourceLicense))
            })
            return@setOnPreferenceClickListener true
        }
    }

}