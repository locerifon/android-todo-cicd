package com.locerifon.todo

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings

class MainActivity : AppCompatActivity() {

    private lateinit var cardShareFeature: MaterialCardView
    private lateinit var cardStatsFeature: MaterialCardView
    private lateinit var btnShare: MaterialButton
    private lateinit var btnSyncFirebase: MaterialButton
    private lateinit var tvVersionBadge: TextView
    private lateinit var tvRemoteConfigStatus: TextView
    private lateinit var remoteConfig: FirebaseRemoteConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cardShareFeature = findViewById(R.id.cardShareFeature)
        cardStatsFeature = findViewById(R.id.cardStatsFeature)
        btnShare = findViewById(R.id.btnShare)
        btnSyncFirebase = findViewById(R.id.btnSyncFirebase)
        tvVersionBadge = findViewById(R.id.tvVersionBadge)
        tvRemoteConfigStatus = findViewById(R.id.tvRemoteConfigStatus)

        // Отображение актуальной версии сборки (автоматически из Git)
        try {
            val pInfo = packageManager.getPackageInfo(packageName, 0)
            @Suppress("DEPRECATION")
            tvVersionBadge.text = "v${pInfo.versionName} (${pInfo.versionCode})"
        } catch (e: Exception) {
            tvVersionBadge.text = "v2.1.14"
        }

        // Действие при клике на фичу шаринга
        btnShare.setOnClickListener {
            Toast.makeText(this, "Список задач скопирован для отправки!", Toast.LENGTH_SHORT).show()
        }

        // Задание 3.1 & 3.3: Настройка Firebase Remote Config
        remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            // 0 сек для мгновенной проверки сценария «выкатили — выключили» (в проде: 3600)
            minimumFetchIntervalInSeconds = 0
        }
        remoteConfig.setConfigSettingsAsync(configSettings)

        // Значения флагов по умолчанию
        remoteConfig.setDefaultsAsync(
            mapOf(
                "new_feature_enabled" to false,
                "stats_banner_enabled" to false
            )
        )

        // Первичное получение конфигурации из Firebase
        fetchRemoteConfig()

        // Кнопка принудительной синхронизации флагов с Firebase
        btnSyncFirebase.setOnClickListener {
            tvRemoteConfigStatus.text = "Firebase Remote Config: синхронизация..."
            fetchRemoteConfig()
        }
    }

    private fun fetchRemoteConfig() {
        remoteConfig.fetchAndActivate().addOnCompleteListener(this) { task ->
            val isNewFeatureEnabled = remoteConfig.getBoolean("new_feature_enabled")
            val isStatsBannerEnabled = remoteConfig.getBoolean("stats_banner_enabled")

            updateFeatureState(isNewFeatureEnabled)
            updateStatsFeatureState(isStatsBannerEnabled)

            if (task.isSuccessful) {
                tvRemoteConfigStatus.text =
                    "Firebase синхронизирован ✅\nnew_feature_enabled = $isNewFeatureEnabled | stats_banner_enabled = $isStatsBannerEnabled"
            } else {
                val err = task.exception?.localizedMessage ?: "нет сети"
                tvRemoteConfigStatus.text =
                    "Ошибка Firebase ($err). Используются кэш/дефолтные флаги."
            }
        }
    }

    /**
     * Задание 3.1: Переключение отображения кнопки шаринга по флагу new_feature_enabled
     */
    private fun updateFeatureState(isEnabled: Boolean) {
        cardShareFeature.visibility = if (isEnabled) View.VISIBLE else View.GONE
    }

    /**
     * Задание 3.3: Второй флаг stats_banner_enabled для отображения блока аналитики
     */
    private fun updateStatsFeatureState(isEnabled: Boolean) {
        cardStatsFeature.visibility = if (isEnabled) View.VISIBLE else View.GONE
    }
}
