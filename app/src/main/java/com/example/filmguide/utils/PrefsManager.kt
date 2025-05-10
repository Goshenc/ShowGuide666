import android.content.Context

// PrefsManager.kt
object PrefsManager {
    private const val PREFS_NAME = "city_prefs"
    private const val KEY_FIRST_SELECTION = "is_first_selection"
    private const val KEY_CITY_ID = "city_id"
    private const val KEY_CITY_NAME = "city_name"

    // 检查是否需要首次选择城市
    fun isFirstSelection(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_FIRST_SELECTION, true)
    }

    // 保存城市信息并标记已选择
    fun saveCityInfo(context: Context, cityId: Int, cityName: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_FIRST_SELECTION, false)
            .putInt(KEY_CITY_ID, cityId)
            .putString(KEY_CITY_NAME, cityName)
            .apply()
    }

    // 获取已保存的城市信息
    fun getCityId(context: Context): Int {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_CITY_ID, -1)
    }

    fun getCityName(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_CITY_NAME, "") ?: ""
    }
}