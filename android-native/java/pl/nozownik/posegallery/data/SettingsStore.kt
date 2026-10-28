package pl.nozownik.posegallery.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppLanguage { PL, EN }

enum class AppThemeId { DARK, AMOLED, LIGHT, PURPLE, CYAN }

data class AppSettings(
    val language: AppLanguage = AppLanguage.PL,
    val theme: AppThemeId = AppThemeId.DARK
)

class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    private val _settings = MutableStateFlow(load())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun load(): AppSettings {
        val lang = runCatching {
            AppLanguage.valueOf(prefs.getString("language", AppLanguage.PL.name)!!)
        }.getOrDefault(AppLanguage.PL)
        val theme = runCatching {
            AppThemeId.valueOf(prefs.getString("theme", AppThemeId.DARK.name)!!)
        }.getOrDefault(AppThemeId.DARK)
        return AppSettings(lang, theme)
    }

    fun setLanguage(lang: AppLanguage) {
        prefs.edit().putString("language", lang.name).apply()
        _settings.value = _settings.value.copy(language = lang)
    }

    fun setTheme(theme: AppThemeId) {
        prefs.edit().putString("theme", theme.name).apply()
        _settings.value = _settings.value.copy(theme = theme)
    }
}
