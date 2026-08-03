import { Routes } from '@angular/router';

/**
 * Top-level entity routes. The 10 domain entities (client…lead) are hosted by the CMS shell
 * instead (see app/cms/cms.routes.ts → /cms/<entity>) so its sidebar + top bar stay mounted
 * during CRUD; only the JHipster admin managers (authorities, users) remain top-level here.
 *
 * NOTE (regeneration): re-running JDL/entity generation re-adds the domain entities below. Move
 * them back into cms.routes.ts children and keep only authority + user-management here.
 */
const routes: Routes = [
  {
    path: 'authority',
    data: { pageTitle: 'jojoaddisonApp.adminAuthority.home.title' },
    loadChildren: () => import('./admin/authority/authority.routes'),
  },
  {
    path: 'user-management',
    data: { pageTitle: 'userManagement.home.title' },
    loadChildren: () => import('./admin/user-management/user-management.routes'),
  },
  /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
];

export default routes;
