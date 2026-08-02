import { HttpResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';
import { Observable, finalize, map } from 'rxjs';

import { IQuote } from 'app/entities/quote/quote.model';
import { QuoteService } from 'app/entities/quote/service/quote.service';
import { ServiceItemService } from 'app/entities/service-item/service/service-item.service';
import { IServiceItem } from 'app/entities/service-item/service-item.model';
import { AlertError } from 'app/shared/alert/alert-error';
import { TranslateDirective } from 'app/shared/language';
import { IQuoteLine } from '../quote-line.model';
import { QuoteLineService } from '../service/quote-line.service';

import { QuoteLineFormGroup, QuoteLineFormService } from './quote-line-form.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-quote-line-update',
  templateUrl: './quote-line-update.html',
  imports: [TranslateDirective, TranslateModule, FontAwesomeModule, AlertError, ReactiveFormsModule],
})
export class QuoteLineUpdate implements OnInit {
  readonly isSaving = signal(false);
  quoteLine: IQuoteLine | null = null;

  serviceItemsSharedCollection = signal<IServiceItem[]>([]);
  quotesSharedCollection = signal<IQuote[]>([]);

  protected quoteLineService = inject(QuoteLineService);
  protected quoteLineFormService = inject(QuoteLineFormService);
  protected serviceItemService = inject(ServiceItemService);
  protected quoteService = inject(QuoteService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: QuoteLineFormGroup = this.quoteLineFormService.createQuoteLineFormGroup();

  compareServiceItem = (o1: IServiceItem | null, o2: IServiceItem | null): boolean => this.serviceItemService.compareServiceItem(o1, o2);

  compareQuote = (o1: IQuote | null, o2: IQuote | null): boolean => this.quoteService.compareQuote(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ quoteLine }) => {
      this.quoteLine = quoteLine;
      if (quoteLine) {
        this.updateForm(quoteLine);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    globalThis.history.back();
  }

  save(): void {
    this.isSaving.set(true);
    const quoteLine = this.quoteLineFormService.getQuoteLine(this.editForm);
    if (quoteLine.id === null) {
      this.subscribeToSaveResponse(this.quoteLineService.create(quoteLine));
    } else {
      this.subscribeToSaveResponse(this.quoteLineService.update(quoteLine));
    }
  }

  protected subscribeToSaveResponse(result: Observable<IQuoteLine | null>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving.set(false);
  }

  protected updateForm(quoteLine: IQuoteLine): void {
    this.quoteLine = quoteLine;
    this.quoteLineFormService.resetForm(this.editForm, quoteLine);

    this.serviceItemsSharedCollection.update(serviceItems =>
      this.serviceItemService.addServiceItemToCollectionIfMissing<IServiceItem>(serviceItems, quoteLine.item),
    );
    this.quotesSharedCollection.update(quotes => this.quoteService.addQuoteToCollectionIfMissing<IQuote>(quotes, quoteLine.quote));
  }

  protected loadRelationshipsOptions(): void {
    this.serviceItemService
      .query()
      .pipe(map((res: HttpResponse<IServiceItem[]>) => res.body ?? []))
      .pipe(
        map((serviceItems: IServiceItem[]) =>
          this.serviceItemService.addServiceItemToCollectionIfMissing<IServiceItem>(serviceItems, this.quoteLine?.item),
        ),
      )
      .subscribe((serviceItems: IServiceItem[]) => this.serviceItemsSharedCollection.set(serviceItems));

    this.quoteService
      .query()
      .pipe(map((res: HttpResponse<IQuote[]>) => res.body ?? []))
      .pipe(map((quotes: IQuote[]) => this.quoteService.addQuoteToCollectionIfMissing<IQuote>(quotes, this.quoteLine?.quote)))
      .subscribe((quotes: IQuote[]) => this.quotesSharedCollection.set(quotes));
  }
}
