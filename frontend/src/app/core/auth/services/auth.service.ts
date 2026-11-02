import { DestroyRef, Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {
  EMPTY,
  Observable,
  Subscription,
  catchError,
  exhaustMap,
  finalize,
  map,
  of,
  shareReplay,
  tap,
  throwError,
  timer,
} from 'rxjs';
import { LoginRequest, LoginResponse } from '../models/auth.models';
import { environment } from '../../../../environments/environment';

const SESSION_CHECK_INTERVAL_MS = 10 * 60 * 1000;

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly destroyRef = inject(DestroyRef);

  private readonly baseUrl = environment.apiUrl;

  private readonly accessToken = signal<string | null>(null);
  private healthCheckSubscription?: Subscription;
  private refreshRequest?: Observable<LoginResponse>;

  constructor() {
    this.destroyRef.onDestroy(() => this.stopHealthCheck());
  }

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${this.baseUrl}/auth/login`, credentials, { withCredentials: true })
      .pipe(
        tap((response) => {
          this.accessToken.set(response.accessToken);
          this.startHealthCheck();
        }),
      );
  }

  refresh(): Observable<LoginResponse> {
    if (!this.refreshRequest) {
      this.refreshRequest = this.http
        .post<LoginResponse>(`${this.baseUrl}/auth/refresh`, null, { withCredentials: true })
        .pipe(
          tap((response) => {
            this.accessToken.set(response.accessToken);
            this.startHealthCheck();
          }),
          catchError((error: unknown) => {
            this.accessToken.set(null);
            this.stopHealthCheck();
            return throwError(() => error);
          }),
          finalize(() => {
            this.refreshRequest = undefined;
          }),
          shareReplay({ bufferSize: 1, refCount: false }),
        );
    }

    return this.refreshRequest;
  }

  ensureAuthenticated(): Observable<boolean> {
    if (this.accessToken()) {
      return of(true);
    }

    return this.refresh().pipe(
      map(() => true),
      catchError(() => of(false)),
    );
  }

  getAccessToken(): string | null {
    return this.accessToken();
  }

  logout(): Observable<void> {
    this.accessToken.set(null);
    this.stopHealthCheck();
    return this.http.post<void>(`${this.baseUrl}/auth/logout`, null, {
      withCredentials: true,
    });
  }

  private startHealthCheck(): void {
    if (this.healthCheckSubscription && !this.healthCheckSubscription.closed) {
      return;
    }

    this.healthCheckSubscription = timer(SESSION_CHECK_INTERVAL_MS, SESSION_CHECK_INTERVAL_MS)
      .pipe(exhaustMap(() => this.checkSession()))
      .subscribe();
  }

  private stopHealthCheck(): void {
    this.healthCheckSubscription?.unsubscribe();
    this.healthCheckSubscription = undefined;
  }

  private checkSession(): Observable<void> {
    if (!this.accessToken()) {
      return EMPTY;
    }

    return this.http.get<void>(`${this.baseUrl}/auth/me`).pipe(
      map(() => undefined),
      catchError(() => {
        this.accessToken.set(null);
        this.stopHealthCheck();
        return EMPTY;
      }),
    );
  }
}
