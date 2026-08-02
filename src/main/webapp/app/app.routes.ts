import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { Authority } from 'app/shared/jhipster/constants';

import { errorRoute } from './layouts/error/error.route';

const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./home/home'),
    title: 'home.title',
  },
  {
    path: '',
    loadComponent: () => import('./layouts/navbar/navbar'),
    outlet: 'navbar',
  },
  {
    path: 'admin',
    data: {
      authorities: [Authority.ADMIN],
    },
    canActivate: [UserRouteAccessService],
    loadChildren: () => import('./admin/admin.routes'),
  },
  {
    path: 'account',
    loadChildren: () => import('./account/account.route'),
  },
  {
    path: 'login',
    loadComponent: () => import('./login/login'),
    title: 'login.title',
  },
  {
    path: 'portal',
    canActivate: [UserRouteAccessService],
    loadChildren: () => import('./portal/portal.routes'),
  },
  {
    path: 'cms',
    data: {
      authorities: [Authority.ADMIN],
    },
    canActivate: [UserRouteAccessService],
    loadComponent: () => import('./cms/cms'),
    title: 'Content management',
  },
  {
    // The entity CRUD pages are the content-management surface: admin only.
    path: '',
    data: {
      authorities: [Authority.ADMIN],
    },
    canActivate: [UserRouteAccessService],
    loadChildren: () => import('./entities/entity.routes'),
  },
  ...errorRoute,
];

export default routes;
