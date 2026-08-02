import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';

import { IClient } from 'app/entities/client/client.model';
import { ClientService } from 'app/entities/client/service/client.service';
import { PortalFormat } from '../shared/portal-format';

@Component({
  selector: 'jhi-portal-clients',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './clients.html',
  imports: [],
})
export default class Clients implements OnInit {
  protected readonly fmt = PortalFormat;
  protected readonly clients = signal<IClient[]>([]);
  protected readonly loading = signal(true);

  private readonly clientService = inject(ClientService);

  ngOnInit(): void {
    this.clientService.query({ size: 100, sort: ['name,asc'] }).subscribe({
      next: res => {
        this.clients.set(res.body ?? []);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
