import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { AuthGuard } from './auth.guard';
import { KeycloakService } from './keycloak.service';

describe('AuthGuard', () => {
  let guard: AuthGuard;
  let mockKeycloak: Partial<KeycloakService> & {
    isLoggedIn: jasmine.Spy,
    hasAnyRole: jasmine.Spy,
    login: jasmine.Spy
  };

  beforeEach(() => {
    mockKeycloak = {
      isLoggedIn: jasmine.createSpy('isLoggedIn'),
      hasAnyRole: jasmine.createSpy('hasAnyRole'),
      login: jasmine.createSpy('login')
    } as any;

    TestBed.configureTestingModule({
      providers: [AuthGuard, { provide: KeycloakService, useValue: mockKeycloak }]
    });

    guard = TestBed.inject(AuthGuard);
  });

  function makeRouteWithRoles(roles?: string[]): ActivatedRouteSnapshot {
    return ({ data: roles ? { roles } : {} } as unknown) as ActivatedRouteSnapshot;
  }

  it('should call login and return false when user is not logged in', () => {
    mockKeycloak.isLoggedIn.and.returnValue(false);

    const result = guard.canActivate(makeRouteWithRoles(['manager', 'accountant']), {} as RouterStateSnapshot);

    expect(result).toBeFalse();
    expect(mockKeycloak.login).toHaveBeenCalled();
  });

  it('should allow access when logged in and has one of the required roles', () => {
    mockKeycloak.isLoggedIn.and.returnValue(true);
    mockKeycloak.hasAnyRole.and.returnValue(true);

    const result = guard.canActivate(makeRouteWithRoles(['manager', 'accountant']), {} as RouterStateSnapshot);

    expect(result).toBeTrue();
    expect(mockKeycloak.login).not.toHaveBeenCalled();
  });

  it('should deny access when logged in but missing required roles', () => {
    mockKeycloak.isLoggedIn.and.returnValue(true);
    mockKeycloak.hasAnyRole.and.returnValue(false);

    const result = guard.canActivate(makeRouteWithRoles(['manager', 'accountant']), {} as RouterStateSnapshot);

    expect(result).toBeFalse();
    expect(mockKeycloak.login).not.toHaveBeenCalled();
  });

  it('should allow access when logged in and route has no roles defined', () => {
    mockKeycloak.isLoggedIn.and.returnValue(true);

    const result = guard.canActivate(makeRouteWithRoles(), {} as RouterStateSnapshot);

    expect(result).toBeTrue();
  });
});

