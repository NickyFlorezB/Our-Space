import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { firstValueFrom } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let authService: AuthService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    authService = TestBed.inject(AuthService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTestingController.verify());

  it('restores the in-memory access token using the refresh cookie', async () => {
    const restoredSession = firstValueFrom(authService.ensureAuthenticated());
    const refreshRequest = httpTestingController.expectOne(`${environment.apiUrl}/auth/refresh`);

    expect(refreshRequest.request.withCredentials).toBe(true);
    refreshRequest.flush({ accessToken: 'restored-access-token' });

    expect(await restoredSession).toBe(true);
    expect(authService.getAccessToken()).toBe('restored-access-token');
  });

  it('shares concurrent session restoration requests', async () => {
    const first = firstValueFrom(authService.ensureAuthenticated());
    const second = firstValueFrom(authService.ensureAuthenticated());
    const refreshRequest = httpTestingController.expectOne(`${environment.apiUrl}/auth/refresh`);
    refreshRequest.flush({ accessToken: 'restored-access-token' });

    expect(await Promise.all([first, second])).toEqual([true, true]);
  });

  it('reports an expired refresh session as unauthenticated', async () => {
    const restoredSession = firstValueFrom(authService.ensureAuthenticated());
    httpTestingController
      .expectOne(`${environment.apiUrl}/auth/refresh`)
      .flush('Refresh token has expired', { status: 401, statusText: 'Unauthorized' });

    expect(await restoredSession).toBe(false);
    expect(authService.getAccessToken()).toBeNull();
  });
});
