import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';

import { ITicket } from 'app/entities/ticket/ticket.model';
import { TicketService } from 'app/entities/ticket/service/ticket.service';
import { PortalFormat } from '../shared/portal-format';
import Pager from '../shared/pager';

@Component({
  selector: 'jhi-portal-support',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './support.html',
  imports: [Pager],
})
export default class Support implements OnInit {
  protected readonly fmt = PortalFormat;
  protected readonly tickets = signal<ITicket[]>([]);
  protected readonly loading = signal(true);
  protected readonly page = signal(1);
  protected readonly total = signal(0);
  protected readonly pageSize = 10;

  private readonly ticketService = inject(TicketService);

  ngOnInit(): void {
    this.load();
  }

  protected onPage(page: number): void {
    this.page.set(page);
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.ticketService.query({ page: this.page() - 1, size: this.pageSize, sort: ['openedAt,desc'] }).subscribe({
      next: res => {
        this.tickets.set(res.body ?? []);
        this.total.set(Number(res.headers.get('X-Total-Count') ?? 0));
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
