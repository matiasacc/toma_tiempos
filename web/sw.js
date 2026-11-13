/* Service worker de la versión web (iOS / navegador).
   Red primero (así siempre se usa la última versión publicada) y caché como respaldo para trabajar sin conexión.
   No toca los datos: las tomas están en localStorage, no en esta caché. */
const CACHE = 'toma-tiempos';
const BASE = ['./', './toma_tiempos.html', './manifest.webmanifest', './icon-192.png', './icon-512.png', './apple-touch-icon.png'];

self.addEventListener('install', e => {
  e.waitUntil(caches.open(CACHE).then(c => c.addAll(BASE)).then(() => self.skipWaiting()));
});
self.addEventListener('activate', e => e.waitUntil(self.clients.claim()));

function conTiempo(p, ms) {
  return new Promise((ok, mal) => { const t = setTimeout(() => mal(new Error('timeout')), ms); p.then(r => { clearTimeout(t); ok(r); }, x => { clearTimeout(t); mal(x); }); });
}
self.addEventListener('fetch', e => {
  const req = e.request;
  if (req.method !== 'GET' || new URL(req.url).origin !== location.origin) return;
  e.respondWith(
    conTiempo(fetch(req), 4000)
      .then(r => { if (r && r.ok) { const cp = r.clone(); caches.open(CACHE).then(c => c.put(req, cp)); } return r; })
      .catch(() => caches.match(req, { ignoreSearch: true }).then(r => r || caches.match('./toma_tiempos.html')))
  );
});
