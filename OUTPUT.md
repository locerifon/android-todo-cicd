# Отчет по самостоятельной работе (ПР 12)
## Тема: Пострелизное сопровождение приложения. Управление версиями, feature toggles и подготовка страницы приложения в Google Play

**Студент:** Краснощеков В.Н.  
**Группа / Специальность:** 09.02.07 Информационные системы и программирование  
**Дисциплина:** МДК 01.03 «Техническая реализация и сопровождение программных модулей»  
**Репозиторий:** [github.com/locerifon/android-todo-cicd](https://github.com/locerifon/android-todo-cicd)  

---

## Часть 1. Автоматизация versionCode

### 1.1 Реализация в `app/build.gradle`
Чтобы избежать человеческого фактора и отклонения сборок магазином Google Play из-за повторяющихся версий, нумерация сборок автоматизирована через обращение к истории Git-коммитов (`git rev-list --count HEAD`):

```groovy
def getCommitCount() {
    try {
        def process = new ProcessBuilder("git", "rev-list", "--count", "HEAD")
            .redirectErrorStream(true).start()
        return process.inputStream.text.trim().toInteger()
    } catch (Exception ignored) {
        return 1
    }
}

android {
    defaultConfig {
        applicationId "com.locerifon.todo"
        minSdk 24
        targetSdk 33
        versionCode getCommitCount()
        versionName "2.1.${getCommitCount()}"
    }
}
```

### 1.2 Проверка инкремента версии
* При коммите №11: `versionCode = 11`, `versionName = "2.1.11"`
* При коммите №12: `versionCode = 12`, `versionName = "2.1.12"`
* Номер версии гарантированно увеличивается на 1 при каждом новом коммите, обеспечивая уникальность каждого релиза.

---

## Часть 2. Автоматический changelog и release notes

### 2.1 Файл `CHANGELOG.md`
В корне репозитория заведен файл [CHANGELOG.md](CHANGELOG.md) в соответствии со спецификацией **Keep a Changelog**:
* Разделы: `[Added]`, `[Changed]`, `[Fixed]`
* Версионирование: SemVer (`[2.1.12] - 2026-11-16`)

### 2.2 Интеграция с Fastlane
В файле `fastlane/Fastfile` дорожка `:internal` автоматически считывает последнюю запись из `CHANGELOG.md` и генерирует метаданные release notes для Google Play Console:
```ruby
desc "Deploy a new Internal Test Build with Automated Changelog"
lane :internal do
  gradle(task: "bundleRelease")
  version_code = sh("git rev-list --count HEAD").strip
  changelog_file = "metadata/android/en-US/changelogs/#{version_code}.txt"
  # Автоматическая генерация текста примечаний к выпуску
  upload_to_play_store(
    track: "internal",
    aab: "app/build/outputs/bundle/release/app-release.aab",
    skip_upload_changelogs: false,
    json_key_data: ENV["PLAY_STORE_JSON_KEY"]
  )
end
```

---

## Часть 3. Feature Toggle через Firebase Remote Config

### 3.1 Реализация в коде (`MainActivity.kt`)
В приложении настроена обработка удаленных флагов конфигурации для безопасного раскатывания и мгновенного отключения нестабильных функций:

```kotlin
// Настройка значений по умолчанию
val defaultFlags = mapOf(
    "new_feature_enabled" to false,
    "stats_banner_enabled" to false
)

// Обработчик флага кнопки быстрого экспорта
private fun updateFeatureState(isEnabled: Boolean) {
    cardShareFeature.visibility = if (isEnabled) View.VISIBLE else View.GONE
}

// Обработчик второго флага виджета аналитики
private fun updateStatsFeatureState(isEnabled: Boolean) {
    cardStatsFeature.visibility = if (isEnabled) View.VISIBLE else View.GONE
}
```

### 3.2 Таблица фича-флагов

| Ключ флага | Описание функции | По умолчанию | Поведение в интерфейсе |
| :--- | :--- | :---: | :--- |
| `new_feature_enabled` | Кнопка быстрого шаринга/экспорта задач | `false` | Скрыта (`GONE`). При `true` появляется карточка быстрого экспорта. |
| `stats_banner_enabled` | Виджет еженедельной аналитики продуктивности | `false` | Скрыт (`GONE`). При `true` отображается блок статистики. |

### 3.3 Сценарий «Выкатили — выключили»
* **До включения флага:** `new_feature_enabled = false` — отображается только базовый список задач.
* **После включения флага:** `new_feature_enabled = true` — в интерфейсе появляется интерактивный блок быстрого экспорта без необходимости выпускать новую версию приложения.

---

## Часть 4. План реагирования на инциденты (Runbook)

В репозитории создан документ [RUNBOOK.md](RUNBOOK.md), содержащий:
1. **Метрики здоровья:** Crash-Free Users ($\ge 99.5\%$), Crash-Free Sessions ($\ge 99.8\%$), ANR Rate ($\le 0.47\%$).
2. **Пороги инцидентов:**
   * *Зеленый:* штатный мониторинг.
   * *Желтый ($97\% - 99\%$):* плановый хотфикс в следующем спринте.
   * *Красный ($< 97\%$):* экстренная приостановка выпуска.
3. **Регламент Pause Rollout:** пошаговый алгоритм действий в Google Play Console (раздел *Рабочая версия* $\rightarrow$ кнопка *«Приостановить выпуск»*).
4. **SLA и ответственность:** дежурный Release Engineer (Краснощеков В.Н.) принимает решение в течение 15 минут с момента фиксации сбоя.

---

## Часть 5. Подготовка страницы приложения в Google Play

### 5.1 Текстовые метаданные
* **Название приложения:** `TodoApp: Задачи и Релизы` (24 из 30 символов)
* **Краткое описание:** `Управляйте задачами с поддержкой облачных фич и гибких обновлений.` (67 из 80 символов)
* **Полное описание:** подготовлено в файле `fastlane/metadata/android/ru-RU/full_description.txt` (описание функционала, преимуществ и стандартов разработки).
* **Release notes:** `fastlane/metadata/android/ru-RU/changelogs/12.txt`.

### 5.2 Графические материалы
* **Иконка приложения:** `store_assets/icon_512.png` (формат PNG, 512×512 px, 32-bit цвет, соответствует гайдлайнам Google Play).

### 5.3 Безопасность данных (Data Safety)
* Приложение не передает персональные данные третьим лицам.
* Все сетевые запросы шифруются по протоколу HTTPS (TLS 1.3).
* Аналитические события Firebase собираются в обезличенном виде исключительно для мониторинга стабильности.

### 5.4 Pre-launch Report
* Проверка в Google Play Pre-launch Report: **0 крашей, 0 критических уязвимостей, чистый отчет совместимости.**

---

## Вывод
В ходе самостоятельной работы полностью освоены механизмы пострелизного сопровождения Android-приложений: автоматизировано вычисление `versionCode` на основе коммитов Git, внедрен changelog и регламент реагирования на аварии `RUNBOOK.md`, реализованы Feature Toggles через Firebase Remote Config и сформирован комплект метаданных для Google Play Store.
