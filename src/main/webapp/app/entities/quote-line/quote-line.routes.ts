import { Routes } from '@angular/router';

import { ASC } from 'app/config/navigation.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';

import QuoteLineResolve from './route/quote-line-routing-resolve.service';

const quoteLineRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/quote-line').then(m => m.QuoteLine),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/quote-line-detail').then(m => m.QuoteLineDetail),
    resolve: {
      quoteLine: QuoteLineResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/quote-line-update').then(m => m.QuoteLineUpdate),
    resolve: {
      quoteLine: QuoteLineResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/quote-line-update').then(m => m.QuoteLineUpdate),
    resolve: {
      quoteLine: QuoteLineResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default quoteLineRoute;
