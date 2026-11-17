package com.locerifon.todo

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.switchmaterial.SwitchMaterial

class MainActivity : AppCompatActivity() {

    private lateinit var cardShareFeature: MaterialCardView
    private lateinit var cardStatsFeature: MaterialCardView
    private lateinit var btnShare: MaterialButton
    private lateinit var switchNewFeature: SwitchMaterial
    private lateinit var switchStatsFeature: SwitchMaterial
    private lateinit var tvVersionBadge: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cardShareFeature = findViewById(R.id.cardShareFeature)
        cardStatsFeature = findViewById(R.id.cardStatsFeature)
        btnShare = findViewById(R.id.btnShare)
        switchNewFeature = findViewById(R.id.switchNewFeature)
        switchStatsFeature = findViewById(R.id.switchStatsFeature)
        tvVersionBadge = findViewById(R.id.tvVersionBadge)

        // Отображение актуальной версии сборки
        try {
            val pInfo = packageManager.getPackageInfo(packageName, 0)
            tvVersionBadge.text = "v${pInfo.versionName} (${pInfo.versionCode})"
        } catch (e: Exception) {
            tvVersionBadge.text = "v2.1.12"
        }

        // Действие при клике на фичу шаринга
        btnShare.setOnClickListener {
            Toast.makeText(this, "Список задач скопирован!", Toast.LENGTH_SHORT).show()
        }

        // Задание 3.1 & 3.3: Инициализация и обработка флагов Remote Config
        setupRemoteConfig()

        // Интерактивное переключение флагов для тестирования сценария выкатили/выключили
        switchNewFeature.setOnCheckedChangeListener { _, isChecked ->
            updateFeatureState(isChecked)
        }

        switchStatsFeature.setOnCheckedChangeListener { _, isChecked ->
            updateStatsFeatureState(isChecked)
        }
    }

    private fun setupRemoteConfig() {
        // Значения по умолчанию
        val defaultFlags = mapOf(
            "new_feature_enabled" to false,
            "stats_banner_enabled" to false
        )

        updateFeatureState(defaultFlags["new_feature_enabled"] ?: false)
        updateStatsFeatureState(defaultFlags["stats_banner_enabled"] ?: false)

        switchNewFeature.isChecked = defaultFlags["new_feature_enabled"] ?: false
        switchStatsFeature.isChecked = defaultFlags["stats_banner_enabled"] ?: false
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
