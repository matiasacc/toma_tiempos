# Toma de Tiempos — reglas del repo

- `web/toma_tiempos.html` es la **fuente única**: toda la lógica va ahí. La APK lo carga tal cual (assets.srcDirs = ../../web). No duplicar el HTML dentro de `android/`.
- El HTML tiene que seguir funcionando solo en un navegador. Todo lo específico de Android pasa por `window.Android` (objeto `AND`), con alternativa para navegador.
- Si se agrega un método al puente, implementarlo en `MainActivity.Puente` con `@JavascriptInterface`.
- Versión: `const VERSION` en el HTML. Para publicar: subir VERSION y push a main; el workflow crea el tag y la release si esa versión no existe. (Desde el entorno de Claude no se pueden pushear tags: el proxy lo bloquea.)
- No cambiar `android/keystore/toma_tiempos.jks`: con otra llave las actualizaciones no se instalan encima.
- No se puede compilar en el entorno de Claude (SDK/Gradle bloqueados): verificar por GitHub Actions.
- Estilo: sobrio, ingenieril. Interfaz y mensajes en español.
