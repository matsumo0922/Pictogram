package caios.android.pictogram.global

import android.app.Application
import caios.android.pictogram.game.RankingControl
import caios.android.pictogram.utils.ThemeUtils

lateinit var global: GlobalClass
lateinit var setting: SettingClass
lateinit var ranking: RankingControl

class GlobalClass: Application() {

    override fun onCreate() {
        super.onCreate()

        global = this
        setting = SettingClass(applicationContext)
        ranking = RankingControl(applicationContext)

        //ダークテーマは使用しない -> 自動選択は使用しない
        //ThemeUtils.setAppTheme()

        ThemeUtils.setTheme(ThemeUtils.Theme.Light)
    }

}