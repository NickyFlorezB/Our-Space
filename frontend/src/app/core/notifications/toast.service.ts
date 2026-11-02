import { DestroyRef, Injectable, inject, signal } from '@angular/core';

export interface ToastMessage {
  id: number;
  message: string;
}

const TOAST_DURATION_MS = 5000;

@Injectable({
  providedIn: 'root',
})
export class ToastService {
  private readonly destroyRef = inject(DestroyRef);
  private readonly timers = new Map<number, ReturnType<typeof setTimeout>>();
  private nextId = 0;

  readonly messages = signal<ToastMessage[]>([]);

  constructor() {
    this.destroyRef.onDestroy(() => {
      this.timers.forEach((timer) => clearTimeout(timer));
      this.timers.clear();
    });
  }

  showError(message: string): void {
    const id = this.nextId++;
    this.messages.update((messages) => [...messages, { id, message }]);
    this.timers.set(
      id,
      setTimeout(() => this.dismiss(id), TOAST_DURATION_MS),
    );
  }

  dismiss(id: number): void {
    const timer = this.timers.get(id);
    if (timer !== undefined) {
      clearTimeout(timer);
      this.timers.delete(id);
    }

    this.messages.update((messages) => messages.filter((message) => message.id !== id));
  }
}
