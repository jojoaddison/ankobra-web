import { Routes } from '@angular/router';

import { ASC } from 'app/config/navigation.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';

import ServiceItemResolve from './route/service-item-routing-resolve.service';

const serviceItemRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/service-item').then(m => m.ServiceItem),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/service-item-detail').then(m => m.ServiceItemDetail),
    resolve: {
      serviceItem: ServiceItemResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/service-item-update').then(m => m.ServiceItemUpdate),
    resolve: {
      serviceItem: ServiceItemResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/service-item-update').then(m => m.ServiceItemUpdate),
    resolve: {
      serviceItem: ServiceItemResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default serviceItemRoute;
