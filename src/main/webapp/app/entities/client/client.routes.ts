import { Routes } from '@angular/router';

import { ASC } from 'app/config/navigation.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';

import ClientResolve from './route/client-routing-resolve.service';

const clientRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/client').then(m => m.Client),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/client-detail').then(m => m.ClientDetail),
    resolve: {
      client: ClientResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/client-update').then(m => m.ClientUpdate),
    resolve: {
      client: ClientResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/client-update').then(m => m.ClientUpdate),
    resolve: {
      client: ClientResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default clientRoute;
