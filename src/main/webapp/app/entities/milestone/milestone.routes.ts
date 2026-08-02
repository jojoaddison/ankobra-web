import { Routes } from '@angular/router';

import { ASC } from 'app/config/navigation.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';

import MilestoneResolve from './route/milestone-routing-resolve.service';

const milestoneRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/milestone').then(m => m.Milestone),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/milestone-detail').then(m => m.MilestoneDetail),
    resolve: {
      milestone: MilestoneResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/milestone-update').then(m => m.MilestoneUpdate),
    resolve: {
      milestone: MilestoneResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/milestone-update').then(m => m.MilestoneUpdate),
    resolve: {
      milestone: MilestoneResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default milestoneRoute;
