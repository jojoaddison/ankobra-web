import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';

import { tap } from 'rxjs';

import { PASSWORD_CHANGE_REQUIRED_TYPE } from 'app/shared/jhipster/error.constants';

const PASSWORD_ROUTE = '/account/password';

/**
 * Sends a user whose password predates the current policy to the password form (SEC-04).
 *
 * <p>The server has already narrowed what the session can reach — `PasswordChangeRequiredFilter`
 * answers 403 to everything but `/api/account` and the change-password endpoint. Without this the user
 * would see a generic "forbidden" on every screen and have no idea what to do about it, which is how a
 * security requirement reads as a broken application.
 *
 * <p>The route check prevents a loop: once the user is on the password form, a later 403 from a
 * lingering background request must not re-navigate and reset the form they are typing into.
 */
export const passwordChangeRequiredInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);

  return next(req).pipe(
    tap({
      error(err: HttpErrorResponse) {
        if (err.status === 403 && err.error?.type === PASSWORD_CHANGE_REQUIRED_TYPE && !router.url.startsWith(PASSWORD_ROUTE)) {
          void router.navigate([PASSWORD_ROUTE]);
        }
      },
    }),
  );
};
