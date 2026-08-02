import { DOCUMENT } from '@angular/common';
import { Injectable, signal, inject } from '@angular/core';

export type Theme = 'light' | 'dark';
type ThemeMode = 'auto' | Theme;

const STORAGE_KEY = 'jojoaddison-theme-mode';
const DAY_START_HOUR = 6; // 06:00 → light
const NIGHT_START_HOUR = 18; // 18:00 → dark

/**
 * Drives the `data-theme` attribute on <html>.
 *
 * Per the brief the default is chosen by time of day — 06:00–18:00 light, 18:00–06:00 dark —
 * and re-evaluated when the app is left open across a boundary. A manual toggle overrides the
 * clock (persisted); toggling from an overridden state returns to automatic.
 */
@Injectable({ providedIn: 'root' })
export class ThemeService {
  /** The currently applied theme. */
  readonly theme = signal<Theme>('light');
  /** Whether the theme currently follows the clock. */
  readonly auto = signal<boolean>(true);

  private readonly document = inject(DOCUMENT);
  private boundaryTimer?: ReturnType<typeof setTimeout>;

  /** Call once at startup. */
  init(): void {
    const mode = this.readMode();
    this.auto.set(mode === 'auto');
    this.apply(mode === 'auto' ? this.themeForNow() : mode);
    this.scheduleBoundary();
  }

  /** Flip the theme; if currently automatic this pins an override, otherwise returns to automatic. */
  toggle(): void {
    if (this.auto()) {
      this.setMode(this.theme() === 'light' ? 'dark' : 'light');
    } else {
      // Was pinned — flipping back to the clock's choice returns control to automatic.
      const clock = this.themeForNow();
      this.setMode(this.theme() === clock ? (clock === 'light' ? 'dark' : 'light') : 'auto');
    }
  }

  /** Explicitly return to time-of-day behaviour. */
  useAuto(): void {
    this.setMode('auto');
  }

  private setMode(mode: ThemeMode): void {
    if (mode === 'auto') {
      localStorage.removeItem(STORAGE_KEY);
    } else {
      localStorage.setItem(STORAGE_KEY, mode);
    }
    this.auto.set(mode === 'auto');
    this.apply(mode === 'auto' ? this.themeForNow() : mode);
    this.scheduleBoundary();
  }

  private readMode(): ThemeMode {
    const stored = localStorage.getItem(STORAGE_KEY);
    return stored === 'light' || stored === 'dark' ? stored : 'auto';
  }

  private themeForNow(): Theme {
    const hour = new Date().getHours();
    return hour >= DAY_START_HOUR && hour < NIGHT_START_HOUR ? 'light' : 'dark';
  }

  private apply(theme: Theme): void {
    this.theme.set(theme);
    const root = this.document.documentElement;
    root.setAttribute('data-theme', theme);
    // Drive Bootstrap 5.3 colour modes too, so the JHipster admin/entity pages
    // (plain Bootstrap tables, forms, cards) follow the same light/dark switch.
    root.setAttribute('data-bs-theme', theme);
  }

  /** In automatic mode, re-evaluate exactly when the clock next crosses 06:00 or 18:00. */
  private scheduleBoundary(): void {
    clearTimeout(this.boundaryTimer);
    if (!this.auto()) {
      return;
    }
    const now = new Date();
    const next = new Date(now);
    const hour = now.getHours();
    if (hour < DAY_START_HOUR) {
      next.setHours(DAY_START_HOUR, 0, 0, 0);
    } else if (hour < NIGHT_START_HOUR) {
      next.setHours(NIGHT_START_HOUR, 0, 0, 0);
    } else {
      next.setDate(next.getDate() + 1);
      next.setHours(DAY_START_HOUR, 0, 0, 0);
    }
    this.boundaryTimer = setTimeout(() => {
      this.apply(this.themeForNow());
      this.scheduleBoundary();
    }, next.getTime() - now.getTime());
  }
}
