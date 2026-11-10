# Toma de Tiempos

Cronómetro para estudio de tiempos con cortes por elemento, pausas, valoración y exportación a CSV.

Una sola fuente, dos formas de uso:

| Uso | Archivo |
|---|---|
| Navegador (PC, notebook, celular) | `web/toma_tiempos.html` |
| App Android | APK en **Releases** (se compila desde el mismo HTML) |

## Modos de toma

| Modo | Uso |
|---|---|
| **Secuencial** | Elementos fijos y en orden. Cada corte pasa al siguiente; la observación se cierra sola al cortar el último. |
| **Libre** | Sin desglose previo. Cada corte se asigna tocando la actividad que terminó; las nuevas se crean con "+ Nueva". Las repeticiones se miden por separado y se numeran solas (#1, #2…). La observación se cierra con "Fin de observación". |

En ambos modos, cada corte admite un **detalle** (texto libre; se puede dictar con el micrófono del teclado).

Teclado (notebook): Espacio/Enter = corte · P = pausa · Ctrl+Z = deshacer · en modo libre: 1–9 = actividad, N = nueva, F = fin de observación.

## Pantalla dividida

El botón **⧉** divide la pantalla en dos paneles (A y B), cada uno con una toma independiente: sirve para tomar dos puestos a la vez.
En horizontal quedan lado a lado; en vertical, uno arriba del otro.

- Historial y plantillas son compartidos; cada panel tiene su propia toma actual.
- Una misma toma no se puede abrir en dos pantallas a la vez.
- Con la pantalla dividida, la pantalla queda encendida todo el tiempo.
- Si la app se cierra dividida, vuelve dividida y cada panel sigue donde estaba.

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

Si esa VERSION todavía no tiene release, GitHub Actions compila, crea el tag `vx.y.z` y publica en **Releases** la APK y el HTML.

Un push sin cambiar VERSION también compila, pero no publica: la APK de prueba (`x.y.z-dev.N`) queda en **Actions** → la corrida → *Artifacts*.

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
