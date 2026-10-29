import { Injectable, signal } from '@angular/core';

export type Theme = 'light' | 'dark';

@Injectable({
  providedIn: 'root',
})
export class ThemeService {
  private readonly storageKey = 'app-theme';

  readonly theme = signal<Theme>('light');

  constructor() {
    const savedTheme = localStorage.getItem(this.storageKey);
    const initialTheme: Theme =
      savedTheme === 'light' || savedTheme === 'dark' ? savedTheme : 'light';

    this.setTheme(initialTheme);
  }

  setTheme(theme: Theme): void {
    this.theme.set(theme);

    document.documentElement.dataset['theme'] = theme;

    localStorage.setItem(this.storageKey, theme);
  }

  toggleTheme(): void {
    this.setTheme(this.theme() === 'light' ? 'dark' : 'light');
  }
}
