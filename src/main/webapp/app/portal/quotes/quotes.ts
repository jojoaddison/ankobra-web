import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { forkJoin } from 'rxjs';
import dayjs from 'dayjs/esm';

import { IServiceItem } from 'app/entities/service-item/service-item.model';
import { ServiceItemService } from 'app/entities/service-item/service/service-item.service';
import { IClient } from 'app/entities/client/client.model';
import { ClientService } from 'app/entities/client/service/client.service';
import { NewQuote } from 'app/entities/quote/quote.model';
import { QuoteService } from 'app/entities/quote/service/quote.service';
import { NewQuoteLine } from 'app/entities/quote-line/quote-line.model';
import { QuoteLineService } from 'app/entities/quote-line/service/quote-line.service';
import { PortalFormat } from '../shared/portal-format';

interface Line {
  item: IServiceItem;
  quantity: number;
}

@Component({
  selector: 'jhi-portal-quotes',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './quotes.html',
  styleUrl: './quotes.scss',
  imports: [],
})
export default class Quotes implements OnInit {
  protected readonly fmt = PortalFormat;
  protected readonly catalogue = signal<IServiceItem[]>([]);
  protected readonly clients = signal<IClient[]>([]);
  protected readonly lines = signal<Line[]>([]);
  protected readonly title = signal('');
  protected readonly clientId = signal<number | null>(null);
  protected readonly saving = signal(false);
  protected readonly savedRef = signal<string | null>(null);

  protected readonly total = computed(() => this.lines().reduce((sum, l) => sum + (l.item.rate ?? 0) * l.quantity, 0));

  private readonly serviceItemService = inject(ServiceItemService);
  private readonly clientService = inject(ClientService);
  private readonly quoteService = inject(QuoteService);
  private readonly quoteLineService = inject(QuoteLineService);

  ngOnInit(): void {
    this.serviceItemService.query({ size: 100, sort: ['code,asc'] }).subscribe(res => this.catalogue.set(res.body ?? []));
    this.clientService.query({ size: 100, sort: ['name,asc'] }).subscribe(res => this.clients.set(res.body ?? []));
  }

  protected addItem(id: string): void {
    const itemId = Number(id);
    if (!itemId) {
      return;
    }
    const item = this.catalogue().find(i => i.id === itemId);
    if (!item || this.lines().some(l => l.item.id === itemId)) {
      return;
    }
    this.lines.update(lines => [...lines, { item, quantity: 1 }]);
    this.savedRef.set(null);
  }

  protected setQuantity(index: number, value: string): void {
    const qty = Math.max(1, Math.floor(Number(value) || 1));
    this.lines.update(lines => lines.map((l, i) => (i === index ? { ...l, quantity: qty } : l)));
  }

  protected remove(index: number): void {
    this.lines.update(lines => lines.filter((_, i) => i !== index));
  }

  protected lineTotal(line: Line): number {
    return (line.item.rate ?? 0) * line.quantity;
  }

  protected save(): void {
    if (this.lines().length === 0 || this.saving()) {
      return;
    }
    this.saving.set(true);
    const selected = this.clients().find(c => c.id === this.clientId());
    const newQuote: NewQuote = {
      id: null,
      reference: `QT-${Date.now().toString().slice(-6)}`,
      title: this.title().trim() || 'Draft estimate',
      createdDate: dayjs(),
      status: 'DRAFT',
      client: selected ? { id: selected.id, name: selected.name } : null,
    };
    this.quoteService.create(newQuote).subscribe({
      next: quote => {
        const lineRequests = this.lines().map(l => {
          const newLine: NewQuoteLine = {
            id: null,
            quantity: l.quantity,
            rate: l.item.rate ?? 0,
            item: { id: l.item.id, name: l.item.name },
            quote: { id: quote.id, reference: quote.reference },
          };
          return this.quoteLineService.create(newLine);
        });
        forkJoin(lineRequests).subscribe({
          next: () => {
            this.saving.set(false);
            this.savedRef.set(quote.reference ?? null);
            this.lines.set([]);
            this.title.set('');
          },
          error: () => this.saving.set(false),
        });
      },
      error: () => this.saving.set(false),
    });
  }
}
