import { Routes } from '@angular/router';

const routes: Routes = [
  {
    path: 'authority',
    data: { pageTitle: 'jojoaddisonApp.adminAuthority.home.title' },
    loadChildren: () => import('./admin/authority/authority.routes'),
  },
  {
    path: 'client',
    data: { pageTitle: 'jojoaddisonApp.client.home.title' },
    loadChildren: () => import('./client/client.routes'),
  },
  {
    path: 'project',
    data: { pageTitle: 'jojoaddisonApp.project.home.title' },
    loadChildren: () => import('./project/project.routes'),
  },
  {
    path: 'milestone',
    data: { pageTitle: 'jojoaddisonApp.milestone.home.title' },
    loadChildren: () => import('./milestone/milestone.routes'),
  },
  {
    path: 'ticket',
    data: { pageTitle: 'jojoaddisonApp.ticket.home.title' },
    loadChildren: () => import('./ticket/ticket.routes'),
  },
  {
    path: 'service-item',
    data: { pageTitle: 'jojoaddisonApp.serviceItem.home.title' },
    loadChildren: () => import('./service-item/service-item.routes'),
  },
  {
    path: 'quote',
    data: { pageTitle: 'jojoaddisonApp.quote.home.title' },
    loadChildren: () => import('./quote/quote.routes'),
  },
  {
    path: 'quote-line',
    data: { pageTitle: 'jojoaddisonApp.quoteLine.home.title' },
    loadChildren: () => import('./quote-line/quote-line.routes'),
  },
  {
    path: 'course',
    data: { pageTitle: 'jojoaddisonApp.course.home.title' },
    loadChildren: () => import('./course/course.routes'),
  },
  {
    path: 'team-member',
    data: { pageTitle: 'jojoaddisonApp.teamMember.home.title' },
    loadChildren: () => import('./team-member/team-member.routes'),
  },
  {
    path: 'lead',
    data: { pageTitle: 'jojoaddisonApp.lead.home.title' },
    loadChildren: () => import('./lead/lead.routes'),
  },
  {
    path: 'user-management',
    data: { pageTitle: 'userManagement.home.title' },
    loadChildren: () => import('./admin/user-management/user-management.routes'),
  },
  /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
];

export default routes;
