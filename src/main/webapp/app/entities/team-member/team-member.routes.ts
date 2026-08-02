import { Routes } from '@angular/router';

import { ASC } from 'app/config/navigation.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';

import TeamMemberResolve from './route/team-member-routing-resolve.service';

const teamMemberRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/team-member').then(m => m.TeamMember),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/team-member-detail').then(m => m.TeamMemberDetail),
    resolve: {
      teamMember: TeamMemberResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/team-member-update').then(m => m.TeamMemberUpdate),
    resolve: {
      teamMember: TeamMemberResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/team-member-update').then(m => m.TeamMemberUpdate),
    resolve: {
      teamMember: TeamMemberResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default teamMemberRoute;
