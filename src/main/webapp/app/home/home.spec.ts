import { beforeEach, describe, expect, it } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

import Home from './home';

describe('Home Component (marketing)', () => {
  let comp: Home;
  let fixture: ComponentFixture<Home>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });

    fixture = TestBed.createComponent(Home);
    comp = fixture.componentInstance;
  });

  it('should render the marketing page', () => {
    fixture.detectChanges();
    expect(comp).toBeTruthy();
  });

  it('should switch the active service tab', () => {
    (comp as any).selectTab('training');
    expect((comp as any).activeTab()).toBe('training');
    expect((comp as any).activeItems().length).toBeGreaterThan(0);
  });
});
