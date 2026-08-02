import { Routes } from '@angular/router';

const portalRoutes: Routes = [
  {
    path: '',
    loadComponent: () => import('./portal'),
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'overview' },
      { path: 'overview', loadComponent: () => import('./overview/overview'), title: 'Delivery overview' },
    ],
  },
];

export default portalRoutes;
