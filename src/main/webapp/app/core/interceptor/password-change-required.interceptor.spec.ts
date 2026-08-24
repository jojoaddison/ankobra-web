import { HttpErrorResponse, HttpRequest } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { throwError } from 'rxjs';
import { beforeEach, describe, expect, it, vitest } from 'vitest';

import { PASSWORD_CHANGE_REQUIRED_TYPE } from 'app/shared/jhipster/error.constants';

import { passwordChangeRequiredInterceptor } from './password-change-required.interceptor';

describe('passwordChangeRequiredInterceptor', () => {
  let navigate: ReturnType<typeof vitest.fn>;
  let router: { url: string; navigate: typeof navigate };

  beforeEach(() => {
    navigate = vitest.fn();
    router = { url: '/portal/overview', navigate };
    TestBed.configureTestingModule({ providers: [{ provide: Router, useValue: router }] });
  });

  /** Drives the interceptor with a failing request and resolves once the error has propagated. */
  const runWith = (error: HttpErrorResponse): Promise<void> =>
    new Promise(resolve => {
      TestBed.runInInjectionContext(() => {
        passwordChangeRequiredInterceptor(new HttpRequest('GET', '/api/projects'), () => throwError(() => error)).subscribe({
          error: () => resolve(),
        });
      });
    });

  const problem = (type: string, status = 403): HttpErrorResponse =>
    new HttpErrorResponse({ status, error: { type }, url: '/api/projects' });

  it('sends the user to the password form when the server demands a password change', async () => {
    await runWith(problem(PASSWORD_CHANGE_REQUIRED_TYPE));

    expect(navigate).toHaveBeenCalledWith(['/account/password']);
  });

  it('leaves an ordinary 403 alone', async () => {
    // A client hitting a staff-only endpoint gets a 403 too. Redirecting them to the password form
    // would be nonsense, and would hide the real reason they were refused.
    await runWith(problem('https://www.jhipster.tech/problem/problem-with-message'));

    expect(navigate).not.toHaveBeenCalled();
  });

  it('ignores a 401', async () => {
    await runWith(problem(PASSWORD_CHANGE_REQUIRED_TYPE, 401));

    expect(navigate).not.toHaveBeenCalled();
  });

  it('does not navigate when the user is already on the password form', async () => {
    // Otherwise a background request failing mid-typing resets the form the user is filling in.
    router.url = '/account/password';

    await runWith(problem(PASSWORD_CHANGE_REQUIRED_TYPE));

    expect(navigate).not.toHaveBeenCalled();
  });

  it('tolerates an error with no body', async () => {
    await runWith(new HttpErrorResponse({ status: 403, url: '/api/projects' }));

    expect(navigate).not.toHaveBeenCalled();
  });
});
