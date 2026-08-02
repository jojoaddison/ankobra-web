import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { HttpResponse } from '@angular/common/http';
import { Observable, forkJoin, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

import { ClientService } from 'app/entities/client/service/client.service';
import { ProjectService } from 'app/entities/project/service/project.service';
import { MilestoneService } from 'app/entities/milestone/service/milestone.service';
import { TicketService } from 'app/entities/ticket/service/ticket.service';
import { ServiceItemService } from 'app/entities/service-item/service/service-item.service';
import { QuoteService } from 'app/entities/quote/service/quote.service';
import { QuoteLineService } from 'app/entities/quote-line/service/quote-line.service';
import { CourseService } from 'app/entities/course/service/course.service';
import { TeamMemberService } from 'app/entities/team-member/service/team-member.service';
import { LeadService } from 'app/entities/lead/service/lead.service';

interface EntityCard {
  key: string;
  label: string;
  description: string;
  link: string;
  icon: string;
  count: (() => Observable<HttpResponse<unknown[]>>) | null;
}
interface EntityGroup {
  group: string;
  items: EntityCard[];
}

@Component({
  selector: 'jhi-cms-overview',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './cms-overview.html',
  styleUrl: './cms-overview.scss',
  imports: [RouterLink],
})
export default class CmsOverview implements OnInit {
  protected readonly counts = signal<Record<string, number | null>>({});

  protected readonly groups: EntityGroup[] = [
    {
      group: 'Delivery',
      items: [
        {
          key: 'client',
          label: 'Clients',
          description: 'Accounts we deliver for',
          link: '/client',
          icon: 'users',
          count: () => this.clientService.query({ size: 1 }),
        },
        {
          key: 'project',
          label: 'Projects',
          description: 'Delivery engagements',
          link: '/project',
          icon: 'folder',
          count: () => this.projectService.query({ size: 1 }),
        },
        {
          key: 'milestone',
          label: 'Milestones',
          description: 'Project timeline steps',
          link: '/milestone',
          icon: 'flag',
          count: () => this.milestoneService.query({ size: 1 }),
        },
        {
          key: 'ticket',
          label: 'Tickets',
          description: 'Support desk requests',
          link: '/ticket',
          icon: 'headset',
          count: () => this.ticketService.query({ size: 1 }),
        },
      ],
    },
    {
      group: 'Commercial',
      items: [
        {
          key: 'serviceItem',
          label: 'Service catalogue',
          description: 'Rate-card line items',
          link: '/service-item',
          icon: 'grid',
          count: () => this.serviceItemService.query({ size: 1 }),
        },
        {
          key: 'quote',
          label: 'Quotes',
          description: 'Estimates for clients',
          link: '/quote',
          icon: 'file',
          count: () => this.quoteService.query({ size: 1 }),
        },
        {
          key: 'quoteLine',
          label: 'Quote lines',
          description: 'Line items on quotes',
          link: '/quote-line',
          icon: 'list',
          count: () => this.quoteLineService.query({ size: 1 }),
        },
      ],
    },
    {
      group: 'Operations',
      items: [
        {
          key: 'course',
          label: 'Courses',
          description: 'Training curriculum',
          link: '/course',
          icon: 'cap',
          count: () => this.courseService.query({ size: 1 }),
        },
        {
          key: 'teamMember',
          label: 'Team',
          description: 'Consultants and staff',
          link: '/team-member',
          icon: 'user',
          count: () => this.teamMemberService.query({ size: 1 }),
        },
      ],
    },
    {
      group: 'Leads',
      items: [
        {
          key: 'lead',
          label: 'Leads',
          description: 'Enquiries from the contact form',
          link: '/lead',
          icon: 'inbox',
          count: () => this.leadService.query({ size: 1 }),
        },
      ],
    },
  ];

  private readonly clientService = inject(ClientService);
  private readonly projectService = inject(ProjectService);
  private readonly milestoneService = inject(MilestoneService);
  private readonly ticketService = inject(TicketService);
  private readonly serviceItemService = inject(ServiceItemService);
  private readonly quoteService = inject(QuoteService);
  private readonly quoteLineService = inject(QuoteLineService);
  private readonly courseService = inject(CourseService);
  private readonly teamMemberService = inject(TeamMemberService);
  private readonly leadService = inject(LeadService);

  ngOnInit(): void {
    const cards = this.groups.flatMap(g => g.items).filter(c => c.count);
    forkJoin(
      cards.map(card =>
        card.count!().pipe(
          map(res => ({ key: card.key, count: Number(res.headers.get('X-Total-Count') ?? 0) })),
          catchError(() => of({ key: card.key, count: null as number | null })),
        ),
      ),
    ).subscribe(results => {
      const next: Record<string, number | null> = {};
      for (const r of results) {
        next[r.key] = r.count;
      }
      this.counts.set(next);
    });
  }

  protected countOf(key: string): number | null | undefined {
    return this.counts()[key];
  }
}
