import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';

import { ITicket } from 'app/entities/ticket/ticket.model';
import { TicketService } from 'app/entities/ticket/service/ticket.service';
import { PortalFormat } from '../shared/portal-format';

@Component({
  selector: 'jhi-portal-support',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './support.html',
  imports: [],
})
export default class Support implements OnInit {
  protected readonly fmt = PortalFormat;
  protected readonly tickets = signal<ITicket[]>([]);
  protected readonly loading = signal(true);

  protected readonly openCount = computed(() => this.tickets().filter(t => t.state === 'OPEN').length);

  private readonly ticketService = inject(TicketService);

  ngOnInit(): void {
    this.ticketService.query({ size: 100, sort: ['openedAt,desc'] }).subscribe({
      next: res => {
        this.tickets.set(res.body ?? []);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
