import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, provideRouter, Router } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { roleGuard } from './role.guard';
import { AuthService } from '../services/auth.service';
import { routes } from '../app.routes';

describe('roleGuard', () => {
  let authService: AuthService;
  let router: Router;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter(routes)]
    });
    authService = TestBed.inject(AuthService);
    router = TestBed.inject(Router);
  });

  function routeWithRoles(roles: string[]): ActivatedRouteSnapshot {
    return { data: { roles } } as unknown as ActivatedRouteSnapshot;
  }

  it('allows navigation when the role is in the allowed list', () => {
    spyOn(authService, 'getRole').and.returnValue('PATIENT');

    const result = TestBed.runInInjectionContext(() =>
      roleGuard(routeWithRoles(['PATIENT', 'SUPPORT']), {} as any)
    );

    expect(result).toBeTrue();
  });

  it('redirects to /dashboard when the role is not allowed', () => {
    spyOn(authService, 'getRole').and.returnValue('DOCTOR');

    const result = TestBed.runInInjectionContext(() =>
      roleGuard(routeWithRoles(['PATIENT', 'SUPPORT']), {} as any)
    );

    expect(result).toEqual(router.parseUrl('/dashboard'));
  });

  it('allows navigation when the route declares no role restriction', () => {
    spyOn(authService, 'getRole').and.returnValue('DOCTOR');

    const result = TestBed.runInInjectionContext(() =>
      roleGuard({ data: {} } as unknown as ActivatedRouteSnapshot, {} as any)
    );

    expect(result).toBeTrue();
  });
});
