@echo off
cd /d "%~dp0"
echo ===================================================
echo     Generaciya Android Keystore i Base64
echo ===================================================
echo.
keytool -genkeypair -v -keystore keystore.jks -alias key0 -keyalg RSA -keysize 2048 -validity 10000 -storepass password123 -keypass password123 -dname "CN=Developer, OU=Mobile, O=Company, L=City, S=State, C=RU"
powershell -Command "[Convert]::ToBase64String([IO.File]::ReadAllBytes('keystore.jks')) | Out-File -Encoding ascii keystore_base64.txt"
echo.
echo Gotovo! Klyuch keystore.jks i stroka v keystore_base64.txt sozdany.
pause
