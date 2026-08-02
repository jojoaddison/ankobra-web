import { Routes } from '@angular/router';

import { ASC } from 'app/config/navigation.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';

import LeadResolve from './route/lead-routing-resolve.service';

const leadRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/lead').then(m => m.Lead),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/lead-detail').then(m => m.LeadDetail),
    resolve: {
      lead: LeadResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/lead-update').then(m => m.LeadUpdate),
    resolve: {
      lead: LeadResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/lead-update').then(m => m.LeadUpdate),
    resolve: {
      lead: LeadResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default leadRoute;
