package caios.android.pictogram.global

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.preference.PreferenceManager
import caios.android.pictogram.utils.ThemeUtils

class SettingClass(private val context: Context) {

    val privilege get() = (developerMode || premiumMode)

    var developerMode: Boolean
        set(value) = setBoolean(NORMAL_SETTING, IS_ENABLE_DEVELOPER_MODE, value)
        get() = getBoolean(NORMAL_SETTING, IS_ENABLE_DEVELOPER_MODE, false)

    var premiumMode: Boolean
        set(value) = setBoolean(NORMAL_SETTING, IS_ENABLE_PREMIUM_MODE, value)
        get() = getBoolean(NORMAL_SETTING, IS_ENABLE_PREMIUM_MODE, false)

    var appTheme: ThemeUtils.Theme
        set(value) = setString(NORMAL_SETTING, THEME_APPLICATION, value.toString())
        get() = ThemeUtils.Theme.valueOf(
            getString(
                NORMAL_SETTING,
                THEME_APPLICATION,
                ThemeUtils.Theme.System.toString()
            )
        )

    fun setInt(preferenceName: String, key: String, value: Int, mode: Int = Context.MODE_PRIVATE) {
        val preference = context.getSharedPreferences(preferenceName, mode)
        preference.edit().putInt(key, value).apply()
    }

    fun setLong(preferenceName: String, key: String, value: Long, mode: Int = Context.MODE_PRIVATE) {
        val preference = context.getSharedPreferences(preferenceName, mode)
        preference.edit().putLong(key, value).apply()
    }

    fun setFloat(preferenceName: String, key: String, value: Float, mode: Int = Context.MODE_PRIVATE) {
        val preference = context.getSharedPreferences(preferenceName, mode)
        preference.edit().putFloat(key, value).apply()
    }

    fun setBoolean(preferenceName: String, key: String, value: Boolean, mode: Int = Context.MODE_PRIVATE) {
        val preference = context.getSharedPreferences(preferenceName, mode)
        preference.edit().putBoolean(key, value).apply()
    }

    fun setString(preferenceName: String, key: String, value: String?, mode: Int = Context.MODE_PRIVATE) {
        val preference = context.getSharedPreferences(preferenceName, mode)
        preference.edit().putString(key, value).apply()
    }

    fun setStringSet(preferenceName: String, key: String, value: Set<String>?, mode: Int = Context.MODE_PRIVATE) {
        val preference = context.getSharedPreferences(preferenceName, mode)
        preference.edit().putStringSet(key, value).apply()
    }

    fun getInt(preferenceName: String, key: String, defaultValue: Int, mode: Int = Context.MODE_PRIVATE): Int {
        val preference = context.getSharedPreferences(preferenceName, mode)
        return preference.getInt(key, defaultValue)
    }

    fun getLong(preferenceName: String, key: String, defaultValue: Long, mode: Int = Context.MODE_PRIVATE): Long {
        val preference = context.getSharedPreferences(preferenceName, mode)
        return preference.getLong(key, defaultValue)
    }

    fun getFloat(preferenceName: String, key: String, defaultValue: Float, mode: Int = Context.MODE_PRIVATE): Float {
        val preference = context.getSharedPreferences(preferenceName, mode)
        return preference.getFloat(key, defaultValue)
    }

    fun getBoolean(preferenceName: String, key: String, defaultValue: Boolean, mode: Int = Context.MODE_PRIVATE): Boolean {
        val preference = context.getSharedPreferences(preferenceName, mode)
        return preference.getBoolean(key, defaultValue)
    }

    fun getString(preferenceName: String, key: String, defaultValue: String, mode: Int = Context.MODE_PRIVATE): String {
        val preference = context.getSharedPreferences(preferenceName, mode)
        return preference.getString(key, defaultValue) ?: defaultValue
    }

    fun getStringOrNull(preferenceName: String, key: String, defaultValue: String? = null, mode: Int = Context.MODE_PRIVATE): String? {
        val preference = context.getSharedPreferences(preferenceName, mode)
        return preference.getString(key, defaultValue)
    }

    fun getStringSet(preferenceName: String, key: String, defaultValue: Set<String>, mode: Int = Context.MODE_PRIVATE): Set<String> {
        val preference = context.getSharedPreferences(preferenceName, mode)
        return preference.getStringSet(key, defaultValue) ?: defaultValue
    }

    fun getStringSetOrNull(preferenceName: String, key: String, defaultValue: Set<String>? = null, mode: Int = Context.MODE_PRIVATE): Set<String>? {
        val preference = context.getSharedPreferences(preferenceName, mode)
        return preference.getStringSet(key, defaultValue)
    }

    fun setInt(key: String, value: Int) {
        val preference = PreferenceManager.getDefaultSharedPreferences(context)
        preference.edit().putInt(key, value).apply()
    }

    fun setLong(key: String, value: Long) {
        val preference = PreferenceManager.getDefaultSharedPreferences(context)
        preference.edit().putLong(key, value).apply()
    }

    fun setFloat(key: String, value: Float) {
        val preference = PreferenceManager.getDefaultSharedPreferences(context)
        preference.edit().putFloat(key, value).apply()
    }

    fun setBoolean(key: String, value: Boolean) {
        val preference = PreferenceManager.getDefaultSharedPreferences(context)
        preference.edit().putBoolean(key, value).apply()
    }

    fun setString(key: String, value: String?) {
        val preference = PreferenceManager.getDefaultSharedPreferences(context)
        preference.edit().putString(key, value).apply()
    }

    fun setStringSet(key: String, value: Set<String>?) {
        val preference = PreferenceManager.getDefaultSharedPreferences(context)
        preference.edit().putStringSet(key, value).apply()
    }

    fun getInt(key: String, defaultValue: Int): Int {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(key, defaultValue)
    }

    fun getLong(key: String, defaultValue: Long): Long {
        return PreferenceManager.getDefaultSharedPreferences(context).getLong(key, defaultValue)
    }

    fun getFloat(key: String, defaultValue: Float): Float {
        return PreferenceManager.getDefaultSharedPreferences(context).getFloat(key, defaultValue)
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key, defaultValue)
    }

    fun getString(key: String, defaultValue: String): String {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key, defaultValue)
            ?: defaultValue
    }

    fun getStringOrNull(key: String, defaultValue: String? = null): String? {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key, defaultValue)
    }

    fun getStringSet(key: String, defaultValue: Set<String>): Set<String> {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getStringSet(key, defaultValue) ?: defaultValue
    }

    fun getStringSetOrNull(key: String, defaultValue: Set<String>? = null): Set<String>? {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getStringSet(key, defaultValue)
    }

    fun getAllPreferenceItem(
        preferenceName: String,
        mode: Int = Context.MODE_PRIVATE
    ): Map<String, *> {
        return context.getSharedPreferences(preferenceName, mode).all
    }

    fun <T> getAllPreferenceSpecificItem(
        preferenceName: String,
        mode: Int = Context.MODE_PRIVATE
    ): Map<String, T> {
        return getAllPreferenceItem(preferenceName, mode).toList()
            .filterIsInstance<Pair<String, T>>().toMap()
    }

    fun clearAllPreferenceItem(preferenceName: String, mode: Int = Context.MODE_PRIVATE) {
        val preference = context.getSharedPreferences(preferenceName, mode)
        preference.edit().clear().apply()
    }

    fun deletePreferenceItem(
        preferenceName: String,
        key: String,
        mode: Int = Context.MODE_PRIVATE
    ) {
        val preference = context.getSharedPreferences(preferenceName, mode)
        preference.edit().remove(key).apply()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun deletePreference(preferenceName: String): Boolean {
        return context.deleteSharedPreferences(preferenceName)
    }

    fun setListener(
        preferenceName: String,
        listener: SharedPreferences.OnSharedPreferenceChangeListener,
        mode: Int = Context.MODE_PRIVATE
    ) {
        val preference = context.getSharedPreferences(preferenceName, mode)
        preference.registerOnSharedPreferenceChangeListener(listener)
    }

    fun removeListener(preferenceName: String, listener: SharedPreferences.OnSharedPreferenceChangeListener, mode: Int = Context.MODE_PRIVATE) {
        val preference = context.getSharedPreferences(preferenceName, mode)
        preference.unregisterOnSharedPreferenceChangeListener(listener)
    }

    companion object {
        const val NORMAL_SETTING = "CAIOS-NormalSetting"
        const val CAIOS_ID = "CAIOS-ID"
        const val THEME_APPLICATION = "ThemeApplication"
        const val THEME_MAIN_PLAYER = "ThemeMainPlayer"
        const val THEME_NOTIFY_PLAYER = "ThemeNotifyPlayer"
        const val COLOR_ACCENT = "AccentColor"
        const val COLOR_MAIN_SEEKBAR = "MainSeekBarColor"
        const val EXTERNAL_STORAGE_ACCESS_URI = "ExternalStorageAccessUri"
        const val IS_FIRST_BOOT = "isFirstBoot"
        const val IS_REVIEW_REQUEST_FLAG = "isReviewRequestFlag"
        const val IS_REVIEW_REQUESTABLE = "isReviewRequestable"
        const val IS_OMIT_DEVELOPER_PIN = "isOmitDeveloperPin"
        const val IS_ENABLE_DEVELOPER_MODE = "isEnableDeveloperMode"
        const val IS_ENABLE_PREMIUM_MODE = "isEnablePremiumMode"
        const val IS_ENABLE_DYNAMIC_NORMALIZER = "isEnableDynamicNormalizer"
        const val IS_ENABLE_EQUALIZER = "isEnableEqualizer"
        const val IS_ENABLE_QUEUE_FULLSCREEN_MODE = "isQueueFullScreenMode"
    }
}