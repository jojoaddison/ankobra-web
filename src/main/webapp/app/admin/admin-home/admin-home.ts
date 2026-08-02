import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';

interface AdminCard {
  label: string;
  description: string;
  link: string;
  icon: 'portal' | 'cms';
}

@Component({
  selector: 'jhi-admin-home',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './admin-home.html',
  styleUrl: './admin-home.scss',
  imports: [RouterLink],
})
export default class AdminHome {
  protected readonly cards: AdminCard[] = [
    {
      label: 'Client Portal',
      description: 'Delivery overview, projects, clients, support desk, quote builder and training.',
      link: '/portal',
      icon: 'portal',
    },
    {
      label: 'Content management',
      description: 'Create, edit and remove the model records behind the marketing site and portal.',
      link: '/cms',
      icon: 'cms',
    },
  ];

  protected readonly tools = [
    { label: 'User management', link: '/user-management' },
    { label: 'Health', link: '/admin/health' },
    { label: 'Metrics', link: '/admin/metrics' },
    { label: 'Logs', link: '/admin/logs' },
    { label: 'Configuration', link: '/admin/configuration' },
    { label: 'API docs', link: '/admin/docs' },
  ];
}
