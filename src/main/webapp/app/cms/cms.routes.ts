import { Routes } from '@angular/router';

const cmsRoutes: Routes = [
  {
    path: '',
    loadComponent: () => import('./cms'),
    children: [{ path: '', loadComponent: () => import('./overview/cms-overview'), title: 'Content management' }],
  },
];

export default cmsRoutes;
