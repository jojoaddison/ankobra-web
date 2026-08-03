import { Routes } from '@angular/router';

/**
 * The content-management shell hosts the domain entity managers as children, so the CMS
 * top bar + sidebar stay mounted while creating/viewing/editing records (URLs are /cms/<entity>).
 * Admin gating comes from the parent `/cms` route in app.routes.ts.
 */
const cmsRoutes: Routes = [
  {
    path: '',
    loadComponent: () => import('./cms'),
    children: [
      { path: '', loadComponent: () => import('./overview/cms-overview'), title: 'Content management' },
      {
        path: 'client',
        data: { pageTitle: 'jojoaddisonApp.client.home.title' },
        loadChildren: () => import('app/entities/client/client.routes'),
      },
      {
        path: 'project',
        data: { pageTitle: 'jojoaddisonApp.project.home.title' },
        loadChildren: () => import('app/entities/project/project.routes'),
      },
      {
        path: 'milestone',
        data: { pageTitle: 'jojoaddisonApp.milestone.home.title' },
        loadChildren: () => import('app/entities/milestone/milestone.routes'),
      },
      {
        path: 'ticket',
        data: { pageTitle: 'jojoaddisonApp.ticket.home.title' },
        loadChildren: () => import('app/entities/ticket/ticket.routes'),
      },
      {
        path: 'service-item',
        data: { pageTitle: 'jojoaddisonApp.serviceItem.home.title' },
        loadChildren: () => import('app/entities/service-item/service-item.routes'),
      },
      {
        path: 'quote',
        data: { pageTitle: 'jojoaddisonApp.quote.home.title' },
        loadChildren: () => import('app/entities/quote/quote.routes'),
      },
      {
        path: 'quote-line',
        data: { pageTitle: 'jojoaddisonApp.quoteLine.home.title' },
        loadChildren: () => import('app/entities/quote-line/quote-line.routes'),
      },
      {
        path: 'course',
        data: { pageTitle: 'jojoaddisonApp.course.home.title' },
        loadChildren: () => import('app/entities/course/course.routes'),
      },
      {
        path: 'team-member',
        data: { pageTitle: 'jojoaddisonApp.teamMember.home.title' },
        loadChildren: () => import('app/entities/team-member/team-member.routes'),
      },
      {
        path: 'lead',
        data: { pageTitle: 'jojoaddisonApp.lead.home.title' },
        loadChildren: () => import('app/entities/lead/lead.routes'),
      },
    ],
  },
];

export default cmsRoutes;
