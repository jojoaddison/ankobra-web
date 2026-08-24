import { Injectable } from '@angular/core';

/**
 * Small pieces of client state that survive a navigation — the URL to return to after login, and the
 * chosen locale.
 *
 * <p>The authentication token used to live here too, in `localStorage` or `sessionStorage` depending
 * on "remember me". It does not any more (SEC-06): it is in an `HttpOnly` cookie the server sets, out
 * of reach of every script including this one. Nothing in this file is a credential, which is why it
 * can go on using web storage without further thought.
 */
@Injectable({ providedIn: 'root' })
export class StateStorageService {
  private readonly previousUrlKey = 'previousUrl';
  private readonly localeKey = 'locale';

  storeUrl(url: string): void {
    sessionStorage.setItem(this.previousUrlKey, JSON.stringify(url));
  }

  getUrl(): string | null {
    const previousUrl = sessionStorage.getItem(this.previousUrlKey);
    return previousUrl ? (JSON.parse(previousUrl) as string | null) : previousUrl;
  }

  clearUrl(): void {
    sessionStorage.removeItem(this.previousUrlKey);
  }

  storeLocale(locale: string): void {
    sessionStorage.setItem(this.localeKey, locale);
  }

  getLocale(): string | null {
    return sessionStorage.getItem(this.localeKey);
  }

  clearLocale(): void {
    sessionStorage.removeItem(this.localeKey);
  }
}
