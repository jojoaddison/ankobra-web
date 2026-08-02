import { Route } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { Authority } from 'app/shared/jhipster/constants';

import Register from './register';

const registerRoute: Route = {
  path: 'register',
  component: Register,
  title: 'register.title',
  // Registration is admin-only: an authenticated admin invites new users.
  data: {
    authorities: [Authority.ADMIN],
  },
  canActivate: [UserRouteAccessService],
};

export default registerRoute;
