import { Routes } from '@angular/router';

import { ASC } from 'app/config/navigation.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';

import ProjectResolve from './route/project-routing-resolve.service';

const projectRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/project').then(m => m.Project),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/project-detail').then(m => m.ProjectDetail),
    resolve: {
      project: ProjectResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/project-update').then(m => m.ProjectUpdate),
    resolve: {
      project: ProjectResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/project-update').then(m => m.ProjectUpdate),
    resolve: {
      project: ProjectResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default projectRoute;
