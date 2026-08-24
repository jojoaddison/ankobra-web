import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

import { Observable, map } from 'rxjs';

import { Login } from 'app/login/login.model';
import { ApplicationConfigService } from '../config/application-config.service';

/**
 * Authentication against the server (SEC-06).
 *
 * <p>There is no token here any more, and that absence is the feature. The JWT used to come back in
 * the response body and get written to `localStorage` or `sessionStorage`, which meant any script
 * running in this origin could read it and replay the session elsewhere. It now travels in an
 * `HttpOnly` cookie the server sets on login: the browser attaches it to every same-origin request
 * automatically, and no code in this application — or injected into it — can see it.
 *
 * <p>Two consequences worth knowing about:
 *
 * <ul>
 *   <li>Logout is a server call. Script cannot delete an `HttpOnly` cookie, which is the same property
 *       that makes it worth having.
 *   <li>CSRF protection is now load-bearing. Angular's `HttpClient` handles it with no code here: it
 *       reads the `XSRF-TOKEN` cookie Spring sets and echoes it in `X-XSRF-TOKEN` on mutating
 *       same-origin requests. If that ever stops happening, every write starts failing with 403.
 * </ul>
 */
@Injectable({ providedIn: 'root' })
export class AuthServerProvider {
  private readonly http = inject(HttpClient);
  private readonly applicationConfigService = inject(ApplicationConfigService);

  login(credentials: Login): Observable<void> {
    // `rememberMe` still travels to the server, which uses it to decide between a persistent cookie
    // and a session one — the same distinction localStorage-vs-sessionStorage used to make here.
    return this.http.post(this.applicationConfigService.getEndpointFor('api/authenticate'), credentials).pipe(map(() => undefined));
  }

  logout(): Observable<void> {
    return this.http.post(this.applicationConfigService.getEndpointFor('api/logout'), null).pipe(map(() => undefined));
  }
}
