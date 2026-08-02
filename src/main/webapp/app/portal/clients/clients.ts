import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';

import { IClient } from 'app/entities/client/client.model';
import { ClientService } from 'app/entities/client/service/client.service';
import { PortalFormat } from '../shared/portal-format';
import Pager from '../shared/pager';

@Component({
  selector: 'jhi-portal-clients',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './clients.html',
  imports: [Pager],
})
export default class Clients implements OnInit {
  protected readonly fmt = PortalFormat;
  protected readonly clients = signal<IClient[]>([]);
  protected readonly loading = signal(true);
  protected readonly page = signal(1);
  protected readonly total = signal(0);
  protected readonly pageSize = 10;

  private readonly clientService = inject(ClientService);

  ngOnInit(): void {
    this.load();
  }

  protected onPage(page: number): void {
    this.page.set(page);
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.clientService.query({ page: this.page() - 1, size: this.pageSize, sort: ['name,asc'] }).subscribe({
      next: res => {
        this.clients.set(res.body ?? []);
        this.total.set(Number(res.headers.get('X-Total-Count') ?? 0));
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
