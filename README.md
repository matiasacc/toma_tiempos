# Toma de Tiempos

Cronómetro para estudio de tiempos con cortes por elemento, pausas, valoración y exportación a CSV.

Una sola fuente, dos formas de uso:

| Uso | Archivo |
|---|---|
| Navegador (PC, notebook, celular) | `web/toma_tiempos.html` |
| App Android | APK en **Releases** (se compila desde el mismo HTML) |

## Estructura

```
web/toma_tiempos.html    ← fuente única de la app (toda la lógica)
android/                 ← contenedor Android (WebView) que carga ese HTML
.github/workflows/apk.yml← compila la APK en GitHub
```

La APK no tiene una copia del HTML: Gradle lo toma directo de `web/` al compilar, así ambos quedan siempre iguales.

## Publicar una versión

1. Cambiar `const VERSION='x.y.z'` en `web/toma_tiempos.html`.
2. Commit y push a `main`.
3. Crear el tag `vx.y.z` (igual a VERSION) y pushearlo.

GitHub Actions compila y publica en **Releases** la APK y el HTML de esa versión.
Si el tag no coincide con VERSION, la compilación falla a propósito.

Cada push a `main` (sin tag) también compila; la APK de prueba queda en la pestaña **Actions** → la corrida → *Artifacts*.

## Instalar en Android

1. Descargar `toma_tiempos-x.y.z.apk` desde Releases en la tablet.
2. Permitir "instalar apps de origen desconocido" para el navegador o el gestor de archivos.
3. Las versiones nuevas se instalan **encima** y conservan los datos (todas se firman con la misma llave, `android/keystore/`).

## Diferencias en la app Android

- **Exportar CSV** guarda en `Descargas/TomaTiempos/`.
- **Compartir CSV** abre el menú de Android (mail, Drive, WhatsApp…).
- La pantalla queda encendida mientras hay una toma en curso.
- El botón *Atrás* pide confirmación antes de salir.

## Datos

Las tomas se guardan dentro de la app o del navegador. Desinstalar la app o borrar los datos del navegador las elimina: exportar el CSV al cerrar cada toma.
