# Firma de publicación Google Play - APPNettel

Fecha: 2026-08-12  
Aplicación: Nettel Marítimo  
Versión: `1.0`  
Version code: `100`  
Package release: `com.nettel.maritimo.next`

## Artefacto firmado generado

El Android App Bundle firmado para subir a Google Play quedó en:

`C:\Users\espin\Documents\APP Nettel\Nettel Maritimo v1.0-release-signed.aab`

También fue copiado al paquete de publicación:

`C:\Users\espin\Documents\APP Nettel\Paquete_Google_Play_Nettel_Maritimo_20260803_101642\01_APK\Nettel Maritimo v1.0-release-signed.aab`

## Keystore local

La clave de firma/upload key fue generada y archivada localmente en:

`C:\Users\espin\Documents\APP Nettel\.signing\APPNettel\appnettel-upload-key.p12`

El archivo local usado por Gradle para firmar release es:

`C:\Users\espin\Documents\APP Nettel\NettelMaritimoNext\keystore.properties`

Estos archivos están excluidos de GitHub por `.gitignore`.

## Certificado público para Google Play

Certificado público exportado:

`C:\Users\espin\Documents\APP Nettel\.signing\APPNettel\appnettel-upload-certificate.pem`

También fue copiado al paquete de publicación:

`C:\Users\espin\Documents\APP Nettel\Paquete_Google_Play_Nettel_Maritimo_20260803_101642\01_APK\appnettel-upload-certificate.pem`

Huella SHA-256 del certificado:

`79:B7:6A:0E:D9:3A:23:3E:C9:8B:BB:FF:80:8F:53:3C:1E:AD:1D:1D:45:FF:78:8E:1C:78:44:62:65:D2:07:B1`

## Configuración aplicada en Gradle

`NettelMaritimoNext/app/build.gradle` fue actualizado para leer `keystore.properties`, configurar `signingConfigs.release` y firmar el build type `release`.

`NettelMaritimoNext/app/proguard-rules.pro` contiene reglas adicionales para evitar fallos R8 por anotaciones opcionales de Tink:

- `-dontwarn javax.annotation.Nullable`
- `-dontwarn javax.annotation.concurrent.GuardedBy`

## Comando de generación

Desde:

`C:\Users\espin\Documents\APP Nettel\NettelMaritimoNext`

Ejecutar:

```powershell
$env:JAVA_HOME='C:\Users\espin\Documents\APP Nettel\.tools\jdk17\jdk-17.0.19+10'
$env:ANDROID_HOME='C:\Users\espin\Documents\APP Nettel\.tools\android-sdk'
.\gradlew.bat --no-daemon bundleRelease --stacktrace
```

Salida esperada:

`NettelMaritimoNext\app\build\outputs\bundle\release\app-release.aab`

## Recomendación de seguridad

Hacer respaldo externo y seguro de:

- `appnettel-upload-key.p12`
- `keystore.properties`
- `README_IMPORTANTE.txt`

Sin esta clave no se podrán firmar futuras actualizaciones con la misma upload key.
