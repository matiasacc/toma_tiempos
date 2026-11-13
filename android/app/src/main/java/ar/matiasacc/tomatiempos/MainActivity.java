package ar.matiasacc.tomatiempos;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.webkit.WebViewAssetLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Contenedor de web/toma_tiempos.html.
 * El HTML se sirve desde un origen https local (appassets.androidplatform.net) para que
 * localStorage funcione igual que en el navegador. La lógica está toda en el HTML;
 * acá solo está el "puente" (window.Android) para lo que el navegador no puede hacer dentro de una app.
 */
public class MainActivity extends Activity {

    private static final String URL_APP = "https://appassets.androidplatform.net/assets/toma_tiempos.html";
    private static final String SUBCARPETA = "TomaTiempos";

    private static final int REQ_ARCHIVO = 1001;

    private WebView web;
    private ValueCallback<Uri[]> callbackArchivo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        web = new WebView(this);
        setContentView(web);

        final WebViewAssetLoader loader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                .build();

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(false);
        s.setTextZoom(100);

        web.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return loader.shouldInterceptRequest(request.getUrl());
            }
        });
        // Con un WebChromeClient asignado, alert/confirm/prompt del HTML se muestran como diálogos nativos.
        // onShowFileChooser hace funcionar <input type="file"> (Restaurar respaldo).
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> cb, FileChooserParams params) {
                if (callbackArchivo != null) callbackArchivo.onReceiveValue(null);
                callbackArchivo = cb;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                try {
                    startActivityForResult(Intent.createChooser(i, "Elegir respaldo"), REQ_ARCHIVO);
                } catch (Exception e) {
                    callbackArchivo = null;
                    return false;
                }
                return true;
            }
        });
        web.addJavascriptInterface(new Puente(), "Android");

        web.loadUrl(URL_APP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_ARCHIVO) {
            if (callbackArchivo != null) {
                Uri[] res = null;
                if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                    res = new Uri[]{data.getData()};
                }
                callbackArchivo.onReceiveValue(res);
                callbackArchivo = null;
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("¿Salir de Toma de Tiempos?\nLa toma en curso queda guardada.")
                .setPositiveButton("Salir", (d, w) -> finish())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        if (web != null) web.destroy();
        super.onDestroy();
    }

    /** Métodos accesibles desde el HTML como window.Android.xxx() */
    private class Puente {

        @JavascriptInterface
        public String guardarCSV(String nombre, String contenido) {
            return guardarArchivo(nombre, contenido, "text/csv");
        }

        /** Guarda un archivo (CSV, respaldo .json) en Descargas/TomaTiempos. Devuelve la ubicación o "" si falla. */
        @JavascriptInterface
        public String guardarArchivo(String nombre, String contenido, String mime) {
            byte[] datos = contenido.getBytes(StandardCharsets.UTF_8);
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContentResolver cr = getContentResolver();
                    ContentValues v = new ContentValues();
                    v.put(MediaStore.MediaColumns.DISPLAY_NAME, nombre);
                    v.put(MediaStore.MediaColumns.MIME_TYPE, (mime == null || mime.isEmpty()) ? "text/plain" : mime);
                    v.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/" + SUBCARPETA);
                    Uri uri = cr.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, v);
                    if (uri == null) return "";
                    try (OutputStream os = cr.openOutputStream(uri)) {
                        if (os == null) return "";
                        os.write(datos);
                    }
                    return "Descargas/" + SUBCARPETA + "/" + nombre;
                } else {
                    // Android 8–9: carpeta de la app (no requiere permisos)
                    File dir = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), SUBCARPETA);
                    if (!dir.exists() && !dir.mkdirs()) return "";
                    File f = new File(dir, nombre);
                    try (FileOutputStream fos = new FileOutputStream(f)) {
                        fos.write(datos);
                    }
                    return f.getAbsolutePath();
                }
            } catch (Exception e) {
                return "";
            }
        }

        /**
         * Respaldo automático: escribe (o reescribe) Descargas/TomaTiempos/<nombre>.
         * Está fuera de los datos de la app, así que sobrevive a desinstalar o "Borrar datos".
         * Si el archivo existe y es de esta instalación, se sobrescribe; si no, se crea uno nuevo.
         */
        @JavascriptInterface
        public String guardarRespaldoAuto(String nombre, String contenido) {
            byte[] datos = contenido.getBytes(StandardCharsets.UTF_8);
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContentResolver cr = getContentResolver();
                    String rel = Environment.DIRECTORY_DOWNLOADS + "/" + SUBCARPETA + "/";
                    Uri existente = null;
                    try (Cursor c = cr.query(MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                            new String[]{MediaStore.MediaColumns._ID},
                            MediaStore.MediaColumns.DISPLAY_NAME + "=? AND " + MediaStore.MediaColumns.RELATIVE_PATH + "=?",
                            new String[]{nombre, rel}, null)) {
                        if (c != null && c.moveToFirst()) {
                            existente = Uri.withAppendedPath(MediaStore.Downloads.EXTERNAL_CONTENT_URI, String.valueOf(c.getLong(0)));
                        }
                    }
                    if (existente != null) {
                        try (OutputStream os = cr.openOutputStream(existente, "wt")) {
                            if (os != null) {
                                os.write(datos);
                                return "Descargas/" + SUBCARPETA + "/" + nombre;
                            }
                        } catch (Exception ignored) {
                            // no es de esta instalación: se crea uno nuevo abajo
                        }
                    }
                    return guardarArchivo(nombre, contenido, "application/json");
                } else {
                    File dir = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), SUBCARPETA);
                    if (!dir.exists() && !dir.mkdirs()) return "";
                    File f = new File(dir, nombre);
                    try (FileOutputStream fos = new FileOutputStream(f, false)) {
                        fos.write(datos);
                    }
                    return f.getAbsolutePath();
                }
            } catch (Exception e) {
                return "";
            }
        }

        /** Abre un enlace https en el navegador del sistema (descarga de una versión nueva). */
        @JavascriptInterface
        public void abrirUrl(String url) {
            if (url == null || !url.startsWith("https://")) return;
            runOnUiThread(() -> {
                try {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "No se pudo abrir el enlace", Toast.LENGTH_SHORT).show();
                }
            });
        }

        /** Abre el menú de compartir de Android (mail, Drive, WhatsApp, etc.) con el CSV. */
        @JavascriptInterface
        public void compartirCSV(String nombre, String contenido) {
            try {
                File dir = new File(getCacheDir(), "compartir");
                if (!dir.exists()) dir.mkdirs();
                File f = new File(dir, nombre);
                try (FileOutputStream fos = new FileOutputStream(f)) {
                    fos.write(contenido.getBytes(StandardCharsets.UTF_8));
                }
                final Uri uri = FileProvider.getUriForFile(MainActivity.this,
                        getPackageName() + ".fileprovider", f);
                runOnUiThread(() -> {
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/csv");
                    i.putExtra(Intent.EXTRA_STREAM, uri);
                    i.putExtra(Intent.EXTRA_SUBJECT, nombre);
                    i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(i, "Compartir CSV"));
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this,
                        "No se pudo compartir", Toast.LENGTH_SHORT).show());
            }
        }

        @JavascriptInterface
        public void vibrar(int ms) {
            try {
                Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                if (vib != null && vib.hasVibrator()) {
                    vib.vibrate(VibrationEffect.createOneShot(Math.max(1, ms), VibrationEffect.DEFAULT_AMPLITUDE));
                }
            } catch (Exception ignored) { }
        }

        /** Mantiene la pantalla encendida mientras hay una toma en curso. */
        @JavascriptInterface
        public void pantalla(boolean encendida) {
            runOnUiThread(() -> {
                if (encendida) getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                else getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            });
        }
    }
}
