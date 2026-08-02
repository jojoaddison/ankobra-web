import { Routes } from '@angular/router';

import { ASC } from 'app/config/navigation.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';

import TicketResolve from './route/ticket-routing-resolve.service';

const ticketRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/ticket').then(m => m.Ticket),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/ticket-detail').then(m => m.TicketDetail),
    resolve: {
      ticket: TicketResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/ticket-update').then(m => m.TicketUpdate),
    resolve: {
      ticket: TicketResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/ticket-update').then(m => m.TicketUpdate),
    resolve: {
      ticket: TicketResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default ticketRoute;
