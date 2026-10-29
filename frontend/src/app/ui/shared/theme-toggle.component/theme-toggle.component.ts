import { Component, inject } from '@angular/core';
import { ThemeService } from '../../../shared/services/theme.service';

@Component({
  selector: 'app-theme-toggle',
  standalone: true,
  template: `
    <button type="button" (click)="themeService.toggleTheme()">
      {{ themeService.theme() === 'light' ? 'Activar modo oscuro' : 'Activar modo claro' }}
    </button>
  `,
})
export class ThemeToggleComponent {
  readonly themeService = inject(ThemeService);
}
