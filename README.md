# Практическая работа №10: CI/CD и автоматизация публикации Android-приложений

## Структура проекта
В папке `C:\Users\ganil\OneDrive\Desktop\vlad\10\` подготовлен полноценный чистый Android-проект с настроенным облачным CI/CD конвейером:
- **`app/`** — исходный код Android-приложения (`MainActivity.kt`, разметка, ресурсы, `AndroidManifest.xml`).
- **`app/build.gradle`** — скрипт сборки с разделением на `debug` и `release`, подключением подписи `signingConfigs`.
- **`.github/workflows/android.yml`** — конвейер GitHub Actions (сборка APK, AAB, авто-инкремент версий, подпись, выгрузка артефактов).
- **`fastlane/`** — конфигурация Fastlane (`Fastfile`, `Appfile`, `Gemfile`) для публикации в Firebase App Distribution.
- **`keystore.jks` & `keystore_base64.txt`** — готовый ключ подписи приложения и его закодированная строка для GitHub Secrets.
- **`.gitignore`** — защита от утечки паролей и ключей (`local.properties` и `*.jks` не попадут в репозиторий).

---

## Как запустить на GitHub:
1. Создайте репозиторий на [GitHub.com](https://github.com) (например, `android-todo-cicd`).
2. Загрузите файлы из папки `10` в репозиторий.
3. Откройте **Settings ➔ Secrets and variables ➔ Actions** и добавьте секрет:
   - Имя: `ANDROID_KEYSTORE_BASE64`
   - Значение: скопируйте текст из файла `keystore_base64.txt`
4. Перейдите во вкладку **Actions** на GitHub: сборка запустится автоматически, соберет релизный APK и AAB, и прикрепит их в разделе Artifacts!
