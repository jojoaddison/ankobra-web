import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';

import { AuthServerProvider } from 'app/core/auth/auth-jwt.service';

/**
 * Rewritten for SEC-06. The old version of this suite asserted where in web storage the token was
 * written; there is no token here any more, so every assertion below is about the absence of one and
 * about logout being a server call.
 */
describe('Auth JWT', () => {
  let service: AuthServerProvider;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClientTesting()] });
    httpMock = TestBed.inject(HttpTestingController);
    service = TestBed.inject(AuthServerProvider);
    localStorage.clear();
    sessionStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('login', () => {
    it('should post the credentials and store nothing', () => {
      service.login({ username: 'kojo', password: 'a-perfectly-fine-passphrase', rememberMe: false }).subscribe();

      const request = httpMock.expectOne({ method: 'POST', url: 'api/authenticate' });
      expect(request.request.body).toEqual({ username: 'kojo', password: 'a-perfectly-fine-passphrase', rememberMe: false });
      request.flush(null);

      // The session lives in an HttpOnly cookie the server sets. Anything written here would be
      // readable by script, which is the exact thing the migration removed.
      expect(localStorage.length).toBe(0);
      expect(sessionStorage.length).toBe(0);
    });

    it('should pass rememberMe through, since the server decides the cookie lifetime', () => {
      service.login({ username: 'kojo', password: 'a-perfectly-fine-passphrase', rememberMe: true }).subscribe();

      const request = httpMock.expectOne({ method: 'POST', url: 'api/authenticate' });
      expect(request.request.body.rememberMe).toBe(true);
      request.flush(null);
    });
  });

  describe('logout', () => {
    it('should call the server, because script cannot delete an HttpOnly cookie', () => {
      let completed = false;
      service.logout().subscribe({ complete: () => (completed = true) });

      httpMock.expectOne({ method: 'POST', url: 'api/logout' }).flush(null);

      expect(completed).toBe(true);
    });
  });
});
