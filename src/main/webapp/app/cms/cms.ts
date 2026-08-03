import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { AccountService } from 'app/core/auth/account.service';
import { LoginService } from 'app/login/login.service';
import { ThemeService } from 'app/core/theme/theme.service';

interface CmsNavItem {
  id: string;
  label: string;
  link: string;
  icon: string;
  exact?: boolean;
}
interface CmsNavGroup {
  group: string;
  items: CmsNavItem[];
}

/**
 * Content-management shell — the same top-bar + collapsible-sidebar chrome as the portal,
 * but scoped to the entity CRUD surface. The sidebar links to each entity manager; the
 * dashboard (entity cards) is the index child. The return button goes back to /admin.
 */
@Component({
  selector: 'jhi-cms',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './cms.html',
  styleUrl: './cms.scss',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
})
export default class Cms {
  protected readonly theme = inject(ThemeService);
  protected readonly account = inject(AccountService).account;
  protected readonly sidebarCollapsed = signal(false);

  protected readonly displayName = computed(() => {
    const a = this.account();
    if (!a) {
      return 'Guest';
    }
    return [a.firstName, a.lastName].filter(Boolean).join(' ') || a.login;
  });

  protected readonly initials = computed(() =>
    this.displayName()
      .split(/\s+/)
      .map(p => p.charAt(0))
      .join('')
      .slice(0, 2)
      .toUpperCase(),
  );

  protected readonly nav: CmsNavGroup[] = [
    {
      group: 'Manage',
      items: [{ id: 'dashboard', label: 'Dashboard', link: '/cms', icon: 'grid', exact: true }],
    },
    {
      group: 'Delivery',
      items: [
        { id: 'client', label: 'Clients', link: '/cms/client', icon: 'users' },
        { id: 'project', label: 'Projects', link: '/cms/project', icon: 'folder' },
        { id: 'milestone', label: 'Milestones', link: '/cms/milestone', icon: 'flag' },
        { id: 'ticket', label: 'Tickets', link: '/cms/ticket', icon: 'headset' },
      ],
    },
    {
      group: 'Commercial',
      items: [
        { id: 'service-item', label: 'Service catalogue', link: '/cms/service-item', icon: 'grid2' },
        { id: 'quote', label: 'Quotes', link: '/cms/quote', icon: 'file' },
        { id: 'quote-line', label: 'Quote lines', link: '/cms/quote-line', icon: 'list' },
      ],
    },
    {
      group: 'Operations',
      items: [
        { id: 'course', label: 'Courses', link: '/cms/course', icon: 'cap' },
        { id: 'team-member', label: 'Team', link: '/cms/team-member', icon: 'user' },
      ],
    },
    {
      group: 'Leads',
      items: [{ id: 'lead', label: 'Leads', link: '/cms/lead', icon: 'inbox' }],
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
