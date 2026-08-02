import { Routes } from '@angular/router';

const portalRoutes: Routes = [
  {
    path: '',
    loadComponent: () => import('./portal'),
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'overview' },
      { path: 'overview', loadComponent: () => import('./overview/overview'), title: 'Delivery overview' },
      { path: 'projects', loadComponent: () => import('./projects/projects'), title: 'Projects' },
      { path: 'clients', loadComponent: () => import('./clients/clients'), title: 'Clients' },
      { path: 'catalogue', loadComponent: () => import('./catalogue/catalogue'), title: 'Service catalogue' },
      { path: 'quotes', loadComponent: () => import('./quotes/quotes'), title: 'Quote builder' },
      { path: 'support', loadComponent: () => import('./support/support'), title: 'Support desk' },
      { path: 'training', loadComponent: () => import('./training/training'), title: 'Training' },
      { path: 'team', loadComponent: () => import('./team/team'), title: 'Team' },
    ],
  },
];

export default portalRoutes;
