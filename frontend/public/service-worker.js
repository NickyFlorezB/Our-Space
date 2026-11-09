self.addEventListener('push', (event) => {
  let payload = {};
  try {
    payload = event.data ? event.data.json() : {};
  } catch {
    payload = { body: event.data?.text() ?? '' };
  }

  const title = payload.title || 'Our Space';
  const options = {
    body: payload.body || 'Hay novedades en una de tus habitaciones.',
    data: { url: payload.url || '/news' },
    icon: '/our-space.svg',
    tag: payload.tag || 'our-space-room-activity',
  };
  event.waitUntil(self.registration.showNotification(title, options));
});

self.addEventListener('notificationclick', (event) => {
  event.notification.close();
  const requestedUrl = event.notification.data?.url || '/news';
  let destination = '/news';
  try {
    const url = new URL(requestedUrl, self.location.origin);
    if (url.origin === self.location.origin) {
      destination = url.href;
    }
  } catch {
    destination = '/news';
  }

  event.waitUntil(
    self.clients.matchAll({ type: 'window', includeUncontrolled: true }).then((clients) => {
      const existing = clients.find((client) => new URL(client.url).origin === self.location.origin);
      if (existing) {
        return existing.navigate(destination).then((client) => client?.focus());
      }
      return self.clients.openWindow(destination);
    }),
  );
});
