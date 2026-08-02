import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';

/** Compact prev/next pager for the portal list views. Emits 1-based page numbers. */
@Component({
  selector: 'jhi-pager',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (totalPages() > 1) {
      <div class="pager">
        <span class="pager-info">{{ from() }}–{{ to() }} of {{ totalItems() }}</span>
        <div class="pager-btns">
          <button type="button" class="pager-btn" [disabled]="page() <= 1" (click)="go(page() - 1)" aria-label="Previous page">‹</button>
          <span class="pager-cur">Page {{ page() }} / {{ totalPages() }}</span>
          <button type="button" class="pager-btn" [disabled]="page() >= totalPages()" (click)="go(page() + 1)" aria-label="Next page">
            ›
          </button>
        </div>
      </div>
    }
  `,
  styleUrl: './pager.scss',
})
export default class Pager {
  readonly page = input(1);
  readonly pageSize = input(10);
  readonly totalItems = input(0);
  readonly pageChange = output<number>();

  protected readonly totalPages = computed(() => Math.max(1, Math.ceil(this.totalItems() / this.pageSize())));
  protected readonly from = computed(() => (this.totalItems() === 0 ? 0 : (this.page() - 1) * this.pageSize() + 1));
  protected readonly to = computed(() => Math.min(this.page() * this.pageSize(), this.totalItems()));

  protected go(page: number): void {
    if (page >= 1 && page <= this.totalPages() && page !== this.page()) {
      this.pageChange.emit(page);
    }
  }
}
