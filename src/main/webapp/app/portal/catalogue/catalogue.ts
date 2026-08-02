import { LowerCasePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';

import { IServiceItem } from 'app/entities/service-item/service-item.model';
import { ServiceItemService } from 'app/entities/service-item/service/service-item.service';
import { PortalFormat } from '../shared/portal-format';

interface Group {
  key: string;
  label: string;
  items: IServiceItem[];
}

const GROUP_ORDER: { key: string; label: string }[] = [
  { key: 'CONSULTANCY', label: 'Consultancy' },
  { key: 'SOLUTIONS', label: 'Solutions' },
  { key: 'SERVICES', label: 'Services' },
  { key: 'TRAINING', label: 'Training' },
];

@Component({
  selector: 'jhi-portal-catalogue',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './catalogue.html',
  imports: [LowerCasePipe],
})
export default class Catalogue implements OnInit {
  protected readonly fmt = PortalFormat;
  protected readonly loading = signal(true);
  protected readonly items = signal<IServiceItem[]>([]);

  protected readonly groups = computed<Group[]>(() =>
    GROUP_ORDER.map(g => ({ ...g, items: this.items().filter(i => i.serviceGroup === g.key) })).filter(g => g.items.length > 0),
  );

  private readonly serviceItemService = inject(ServiceItemService);

  ngOnInit(): void {
    this.serviceItemService.query({ size: 100, sort: ['code,asc'] }).subscribe({
      next: res => {
        this.items.set(res.body ?? []);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
