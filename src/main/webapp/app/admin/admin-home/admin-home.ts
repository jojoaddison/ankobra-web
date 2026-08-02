import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';

type AdminIcon = 'portal' | 'cms' | 'users' | 'shield' | 'gauge' | 'heart' | 'cogs' | 'logs' | 'book';

interface AdminCard {
  label: string;
  description: string;
  link: string;
  icon: AdminIcon;
}

@Component({
  selector: 'jhi-admin-home',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './admin-home.html',
  styleUrl: './admin-home.scss',
  imports: [RouterLink],
})
export default class AdminHome {
  /** Primary destinations: the two authenticated surfaces. */
  protected readonly workspaces: AdminCard[] = [
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

  /** Every Administration menu item, surfaced as cards instead of a dropdown. */
  protected readonly system: AdminCard[] = [
    {
      label: 'User management',
      description: 'Create accounts, assign roles and activate or deactivate users.',
      link: '/user-management',
      icon: 'users',
    },
    {
      label: 'Authorities',
      description: 'Review the security roles that gate access across the application.',
      link: '/authority',
      icon: 'shield',
    },
    {
      label: 'Metrics',
      description: 'JVM, HTTP and cache metrics for the running service.',
      link: '/admin/metrics',
      icon: 'gauge',
    },
    {
      label: 'Health',
      description: 'Liveness of the database and downstream dependencies.',
      link: '/admin/health',
      icon: 'heart',
    },
    {
      label: 'Configuration',
      description: 'Inspect the active Spring environment and property sources.',
      link: '/admin/configuration',
      icon: 'cogs',
    },
    {
      label: 'Logs',
      description: 'View and adjust log levels for the running loggers.',
      link: '/admin/logs',
      icon: 'logs',
    },
    {
      label: 'API docs',
      description: 'Browse and try the REST API through the OpenAPI explorer.',
      link: '/admin/docs',
      icon: 'book',
    },
  ];
}
