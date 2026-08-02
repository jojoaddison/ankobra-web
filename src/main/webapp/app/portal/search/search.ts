import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';

import { IProject } from 'app/entities/project/project.model';
import { ProjectService } from 'app/entities/project/service/project.service';
import { IClient } from 'app/entities/client/client.model';
import { ClientService } from 'app/entities/client/service/client.service';
import { ITicket } from 'app/entities/ticket/ticket.model';
import { TicketService } from 'app/entities/ticket/service/ticket.service';
import { PortalFormat } from '../shared/portal-format';

@Component({
  selector: 'jhi-portal-search',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './search.html',
  styleUrl: './search.scss',
  imports: [RouterLink],
})
export default class Search implements OnInit {
  protected readonly fmt = PortalFormat;
  protected readonly term = signal('');
  protected readonly loading = signal(true);

  protected readonly projects = signal<IProject[]>([]);
  protected readonly clients = signal<IClient[]>([]);
  protected readonly tickets = signal<ITicket[]>([]);

  protected readonly matchedProjects = computed(() => {
    const q = this.term().toLowerCase().trim();
    if (!q) {
      return [];
    }
    return this.projects().filter(p =>
      [p.name, p.reference, p.client?.name, this.fmt.humanize(p.pillar)].some(v => (v ?? '').toLowerCase().includes(q)),
    );
  });

  protected readonly matchedClients = computed(() => {
    const q = this.term().toLowerCase().trim();
    if (!q) {
      return [];
    }
    return this.clients().filter(c => [c.name, this.fmt.humanize(c.sector)].some(v => (v ?? '').toLowerCase().includes(q)));
  });

  protected readonly matchedTickets = computed(() => {
    const q = this.term().toLowerCase().trim();
    if (!q) {
      return [];
    }
    return this.tickets().filter(t => [t.reference, t.subject, t.client?.name].some(v => (v ?? '').toLowerCase().includes(q)));
  });

  protected readonly totalMatches = computed(
    () => this.matchedProjects().length + this.matchedClients().length + this.matchedTickets().length,
  );

  private readonly route = inject(ActivatedRoute);
  private readonly projectService = inject(ProjectService);
  private readonly clientService = inject(ClientService);
  private readonly ticketService = inject(TicketService);

  ngOnInit(): void {
    this.route.queryParamMap.subscribe(params => this.term.set(params.get('q') ?? ''));

    forkJoin({
      projects: this.projectService.query({ size: 200 }),
      clients: this.clientService.query({ size: 200 }),
      tickets: this.ticketService.query({ size: 200 }),
    }).subscribe({
      next: ({ projects, clients, tickets }) => {
        this.projects.set(projects.body ?? []);
        this.clients.set(clients.body ?? []);
        this.tickets.set(tickets.body ?? []);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
