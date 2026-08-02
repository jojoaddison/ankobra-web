import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

import Home from './home';

describe('Home Component (marketing)', () => {
  let comp: Home;
  let fixture: ComponentFixture<Home>;
  let router: Router;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });

    fixture = TestBed.createComponent(Home);
    comp = fixture.componentInstance;
    router = TestBed.inject(Router);
    vitest.spyOn(router, 'navigate').mockResolvedValue(true);
  });

  it('should render the marketing page', () => {
    fixture.detectChanges();
    expect(comp).toBeTruthy();
  });

  it('should navigate to /login when launching the portal', () => {
    (comp as any).launchPortal();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('should switch the active service tab', () => {
    (comp as any).selectTab('training');
    expect((comp as any).activeTab()).toBe('training');
    expect((comp as any).activeItems().length).toBeGreaterThan(0);
  });
});
