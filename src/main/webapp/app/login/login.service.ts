import { Injectable, inject } from '@angular/core';

import { Observable, mergeMap } from 'rxjs';

import { Account } from 'app/core/auth/account.model';
import { AccountService } from 'app/core/auth/account.service';
import { AuthServerProvider } from 'app/core/auth/auth-jwt.service';

import { Login } from './login.model';

@Injectable({ providedIn: 'root' })
export class LoginService {
  private readonly accountService = inject(AccountService);
  private readonly authServerProvider = inject(AuthServerProvider);

  login(credentials: Login): Observable<Account | null> {
    return this.authServerProvider.login(credentials).pipe(mergeMap(() => this.accountService.identity(true)));
  }

  logout(): void {
    // SEC-06: logout is a server call now — only the server can clear an HttpOnly cookie. The local
    // identity is dropped either way: if the request fails, leaving the UI believing it is still
    // signed in would be worse than a cookie that outlives the click, and every subsequent request
    // will 401 anyway.
    this.authServerProvider.logout().subscribe({
      complete: () => this.accountService.authenticate(null),
      error: () => this.accountService.authenticate(null),
    });
  }
}
