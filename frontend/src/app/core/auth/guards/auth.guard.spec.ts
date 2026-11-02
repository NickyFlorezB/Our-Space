import { TestBed } from '@angular/core/testing';
import {
  ActivatedRouteSnapshot,
  Router,
  RouterStateSnapshot,
  UrlTree,
  provideRouter,
} from '@angular/router';
import { Observable, firstValueFrom, of } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { authGuard } from './auth.guard';

describe('authGuard', () => {
  let accessToken: string | null;

  beforeEach(() => {
    accessToken = null;
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        {
          provide: AuthService,
          useValue: { ensureAuthenticated: () => of(accessToken !== null) },
        },
      ],
    });
  });

  it('allows navigation when the session can be restored', async () => {
    accessToken = 'valid-token';

    const result = TestBed.runInInjectionContext(() =>
      authGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot),
    );

    expect(await firstValueFrom(result as Observable<boolean | UrlTree>)).toBe(true);
  });

  it('redirects to login when the session cannot be restored', async () => {
    const result = TestBed.runInInjectionContext(() =>
      authGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot),
    );

    expect(await firstValueFrom(result as Observable<boolean | UrlTree>)).toEqual(
      TestBed.inject(Router).parseUrl('/login'),
    );
  });
});
