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

En modo libre las actividades se pueden borrar y reordenar aunque ya tengan cortes (los cortes de una actividad borrada quedan "sin nombre"). En la botonera, **mantener apretado un botón y arrastrarlo** sobre otro cambia el orden; una pulsación larga nunca registra un corte.

## Ajustes (⚙)

Compartidos por todas las pantallas y paneles: tema (automático / claro / oscuro), tamaño de texto, pantalla encendida (durante la toma / siempre / nunca), vibración y sonido al cortar, valores por defecto de tomas nuevas (observador, precisión de N', encadenar), unidad de tiempo para resultados y CSV (s / min / centésimas de minuto) y formato del CSV (Excel `;` y coma decimal, o estándar `,` y punto).

## Datos y respaldo

- Las tomas se guardan dentro de la app. **Desinstalar o "Borrar datos" las elimina.**
- **Respaldo automático (app Android):** al cerrar cada observación, finalizar, guardar o salir de la app se reescribe `Descargas/TomaTiempos/respaldo_auto_AAAA-MM-DD.json` (uno por día). Está fuera de la app: sobrevive a una desinstalación y se recupera con *Archivo → Restaurar respaldo*.

## Actualizaciones

Al abrir, la app consulta la última release (`api.github.com/repos/matiasacc/toma_tiempos/releases/latest`) y, si hay una versión nueva, muestra un aviso con *Descargar*. La APK se instala **encima** de la anterior (misma firma): no hace falta desinstalar.
Requiere que las releases sean accesibles sin credenciales (repositorio público); con el repositorio privado la consulta devuelve 404 y no se muestra nada.

## Pantalla dividida

El botón **⧉** divide la pantalla en dos paneles (A y B), cada uno con una toma independiente: sirve para tomar dos puestos a la vez.
En horizontal quedan lado a lado; en vertical, uno arriba del otro.

- Historial y plantillas son compartidos; cada panel tiene su propia toma actual.
- Una misma toma no se puede abrir en dos pantallas a la vez.
- Con la pantalla dividida, la pantalla queda encendida todo el tiempo.
- Si la app se cierra dividida, vuelve dividida y cada panel sigue donde estaba.

## iPhone / iPad (web app)

La misma página se publica en **https://matiasacc.github.io/toma_tiempos/** (workflow `pages.yml`, en cada push que cambie `web/`).
Requiere una sola vez: *Settings → Pages → Source: GitHub Actions*.

Instalar: abrir la dirección en **Safari** → *Compartir* → **Agregar a pantalla de inicio**. Queda como una app a pantalla completa.

- Funciona sin conexión (service worker); con conexión carga siempre la última versión publicada.
- Los datos quedan en el dispositivo. No hay respaldo automático: la app recuerda hacer *Respaldo completo* si pasan más de 7 días.
- Abrirla siempre desde el ícono de la pantalla de inicio (Safari y el ícono guardan datos por separado).

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
