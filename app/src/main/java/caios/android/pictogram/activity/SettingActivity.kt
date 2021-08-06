package caios.android.pictogram.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import caios.android.pictogram.R
import caios.android.pictogram.databinding.ActivitySettingBinding
import caios.android.pictogram.utils.ThemeUtils

class SettingActivity : AppCompatActivity() {

    private val binding by lazy { ActivitySettingBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.toolbar.title = getString(R.string.setting)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onResume() {
        super.onResume()

        ThemeUtils.setFullScreen(window)
        ThemeUtils.setThemeIcon(this, window)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if(hasFocus) {
            ThemeUtils.setFullScreen(window)
            ThemeUtils.setThemeIcon(this, window)
        }
    }

    fun setToolbarTitle(@StringRes titleRes: Int) {
        binding.toolbar.title = getString(titleRes)
    }
}