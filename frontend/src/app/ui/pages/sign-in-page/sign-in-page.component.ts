import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { LucideHeart } from '@lucide/angular';
import { AuthService } from '../../../core/auth/services/auth.service';
import { ToastService } from '../../../core/notifications/toast.service';

@Component({
  imports: [ReactiveFormsModule, LucideHeart, RouterLink],
  selector: 'app-sign-in-page.component',
  styleUrl: './sign-in-page.component.scss',
  templateUrl: './sign-in-page.component.html',
})
export default class SignInPageComponent {
  private formBuilder = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly toastService = inject(ToastService);
  private readonly router = inject(Router);

  readonly loading = signal(false);

  profileForm = this.formBuilder.group({
    username: ['', Validators.required],
    password: ['', Validators.required],
  });

  onSubmit() {
    if (this.profileForm.invalid || this.loading()) {
      this.profileForm.markAllAsTouched();
      return;
    }

    this.loading.set(true);

    this.authService
      .login({
        username: this.profileForm.get('username')?.value ?? '',
        password: this.profileForm.get('password')?.value ?? '',
      })
      .subscribe({
        next: () => {
          this.loading.set(false);
          void this.router.navigate(['/news']);
        },
        error: (error: unknown) => {
          this.loading.set(false);

          if (error instanceof HttpErrorResponse && error.status === 401) {
            this.toastService.showError('Usuario o contraseña incorrectos.');
          } else if (
            error instanceof HttpErrorResponse &&
            error.status === 403 &&
            typeof error.error === 'string' &&
            error.error.includes('origin')
          ) {
            this.toastService.showError(
              'El origen de esta página no está autorizado. Configura FRONTEND_ORIGINS en el backend.',
            );
          } else {
            this.toastService.showError('No fue posible iniciar sesión. Inténtalo de nuevo.');
          }
        },
      });
  }
}
