package caios.android.pictogram.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import caios.android.pictogram.R
import caios.android.pictogram.databinding.ActivityMainBinding
import caios.android.pictogram.utils.ThemeUtils

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()

        ThemeUtils.setFullScreen(window)
        ThemeUtils.setThemeIcon(this, window)
    }
}