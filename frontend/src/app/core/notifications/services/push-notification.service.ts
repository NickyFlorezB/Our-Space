import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, firstValueFrom, from, of, switchMap, throwError } from 'rxjs';
import { environment } from '../../../../environments/environment';

interface PushSettings {
  enabled: boolean;
  publicKey: string | null;
}

interface PushSubscriptionRequest {
  endpoint: string;
  keys: {
    p256dh: string;
    auth: string;
  };
}

@Injectable({ providedIn: 'root' })
export class PushNotificationService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;

  isSupported(): boolean {
    return (
      typeof window !== 'undefined' &&
      'serviceWorker' in navigator &&
      'PushManager' in window &&
      'Notification' in window
    );
  }

  hasSubscription(): Observable<boolean> {
    if (!this.isSupported()) {
      return of(false);
    }

    return from(
      navigator.serviceWorker.getRegistration('/').then(async (registration) => {
        const subscription = await registration?.pushManager.getSubscription();
        return subscription !== null && subscription !== undefined;
      }),
    );
  }

  getSettings(): Observable<PushSettings> {
    return this.http.get<PushSettings>(`${this.baseUrl}/push/public-key`);
  }

  enable(): Observable<void> {
    if (!this.isSupported()) {
      return throwError(() => new Error('Este navegador no admite notificaciones push.'));
    }

    return this.getSettings().pipe(
      switchMap((settings) => {
        if (!settings?.enabled || !settings.publicKey) {
          return throwError(
            () => new Error('Las notificaciones push no están configuradas en el servidor.'),
          );
        }
        return from(this.subscribe(settings.publicKey));
      }),
    );
  }

  disable(): Observable<void> {
    if (!this.isSupported()) {
      return of(undefined);
    }

    return from(this.unsubscribe());
  }

  private async subscribe(publicKey: string): Promise<void> {
    const registration = await navigator.serviceWorker.register('/service-worker.js');
    const existing = await registration.pushManager.getSubscription();
    const permission = await Notification.requestPermission();
    if (permission !== 'granted') {
      throw new Error('Permite las notificaciones en tu navegador para activarlas.');
    }

    const subscription =
      existing ??
      (await registration.pushManager.subscribe({
        userVisibleOnly: true,
        applicationServerKey: this.decodeApplicationServerKey(publicKey),
      }));
    try {
      await firstValueFrom(
        this.http.post<void>(
          `${this.baseUrl}/push/subscriptions`,
          this.toRequest(subscription),
        ),
      );
    } catch (error: unknown) {
      if (!existing) {
        await subscription.unsubscribe();
      }
      throw error;
    }
  }

  private async unsubscribe(): Promise<void> {
    const registration = await navigator.serviceWorker.getRegistration('/');
    const subscription = await registration?.pushManager.getSubscription();
    if (!subscription) {
      return;
    }

    await firstValueFrom(
      this.http.delete<void>(`${this.baseUrl}/push/subscriptions`, {
        body: { endpoint: subscription.endpoint },
      }),
    );
    await subscription.unsubscribe();
  }

  private toRequest(subscription: PushSubscription): PushSubscriptionRequest {
    const serialized = subscription.toJSON();
    const keys = serialized.keys;
    if (!serialized.endpoint || !keys?.['p256dh'] || !keys['auth']) {
      throw new Error('El navegador devolvió una suscripción push incompleta.');
    }
    return {
      endpoint: serialized.endpoint,
      keys: { p256dh: keys['p256dh'], auth: keys['auth'] },
    };
  }

  private decodeApplicationServerKey(value: string): Uint8Array<ArrayBuffer> {
    const padded = value + '='.repeat((4 - (value.length % 4)) % 4);
    const binary = atob(padded.replace(/-/g, '+').replace(/_/g, '/'));
    const bytes = new Uint8Array(binary.length);
    for (let index = 0; index < binary.length; index += 1) {
      bytes[index] = binary.charCodeAt(index);
    }
    return bytes;
  }
}
