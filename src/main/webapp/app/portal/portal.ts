import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { AccountService } from 'app/core/auth/account.service';
import { LoginService } from 'app/login/login.service';
import { ThemeService } from 'app/core/theme/theme.service';

interface NavItem {
  id: string;
  label: string;
  link: string;
  icon: string;
}
interface NavGroup {
  group: string;
  items: NavItem[];
}

@Component({
  selector: 'jhi-portal',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './portal.html',
  styleUrl: './portal.scss',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
})
export default class Portal {
  protected readonly theme = inject(ThemeService);
  protected readonly account = inject(AccountService).account;

  protected readonly sidebarCollapsed = signal(false);
  protected readonly search = signal('');

  protected readonly displayName = computed(() => {
    const a = this.account();
    if (!a) {
      return 'Guest';
    }
    return [a.firstName, a.lastName].filter(Boolean).join(' ') || a.login;
  });

  protected readonly initials = computed(() => {
    const name = this.displayName();
    return name
      .split(/\s+/)
      .map(p => p.charAt(0))
      .join('')
      .slice(0, 2)
      .toUpperCase();
  });

  protected readonly roleLabel = computed(() => {
    const a = this.account();
    if (!a) {
      return '';
    }
    return a.authorities.includes('ROLE_ADMIN') ? 'Consultant — Jojo Addison team' : 'Client';
  });

  protected readonly nav: NavGroup[] = [
    {
      group: 'Delivery',
      items: [
        { id: 'overview', label: 'Overview', link: '/portal/overview', icon: 'grid' },
        { id: 'projects', label: 'Projects', link: '/project', icon: 'folder' },
        { id: 'clients', label: 'Clients', link: '/client', icon: 'users' },
      ],
    },
    {
      group: 'Commercial',
      items: [
        { id: 'catalogue', label: 'Service catalogue', link: '/service-item', icon: 'grid2' },
        { id: 'quotes', label: 'Quote builder', link: '/quote', icon: 'file' },
      ],
    },
    {
      group: 'Operations',
      items: [
        { id: 'support', label: 'Support desk', link: '/ticket', icon: 'headset' },
        { id: 'training', label: 'Training', link: '/course', icon: 'cap' },
        { id: 'team', label: 'Team', link: '/team-member', icon: 'user' },
      ],
    },
  ];

  private readonly loginService = inject(LoginService);

  protected toggleSidebar(): void {
    this.sidebarCollapsed.update(v => !v);
  }

  protected toggleTheme(): void {
    this.theme.toggle();
  }

  protected logout(): void {
    this.loginService.logout();
  }
}
