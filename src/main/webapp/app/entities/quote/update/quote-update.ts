import { HttpResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';
import { Observable, finalize, map } from 'rxjs';

import { IClient } from 'app/entities/client/client.model';
import { ClientService } from 'app/entities/client/service/client.service';
import { QuoteStatus } from 'app/entities/enumerations/quote-status.model';
import { AlertError } from 'app/shared/alert/alert-error';
import { TranslateDirective } from 'app/shared/language';
import { IQuote } from '../quote.model';
import { QuoteService } from '../service/quote.service';

import { QuoteFormGroup, QuoteFormService } from './quote-form.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-quote-update',
  templateUrl: './quote-update.html',
  imports: [TranslateDirective, TranslateModule, FontAwesomeModule, AlertError, ReactiveFormsModule],
})
export class QuoteUpdate implements OnInit {
  readonly isSaving = signal(false);
  quote: IQuote | null = null;
  quoteStatusValues = Object.keys(QuoteStatus);

  clientsSharedCollection = signal<IClient[]>([]);

  protected quoteService = inject(QuoteService);
  protected quoteFormService = inject(QuoteFormService);
  protected clientService = inject(ClientService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: QuoteFormGroup = this.quoteFormService.createQuoteFormGroup();

  compareClient = (o1: IClient | null, o2: IClient | null): boolean => this.clientService.compareClient(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ quote }) => {
      this.quote = quote;
      if (quote) {
        this.updateForm(quote);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    globalThis.history.back();
  }

  save(): void {
    this.isSaving.set(true);
    const quote = this.quoteFormService.getQuote(this.editForm);
    if (quote.id === null) {
      this.subscribeToSaveResponse(this.quoteService.create(quote));
    } else {
      this.subscribeToSaveResponse(this.quoteService.update(quote));
    }
  }

  protected subscribeToSaveResponse(result: Observable<IQuote | null>): void {
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

  protected updateForm(quote: IQuote): void {
    this.quote = quote;
    this.quoteFormService.resetForm(this.editForm, quote);

    this.clientsSharedCollection.update(clients => this.clientService.addClientToCollectionIfMissing<IClient>(clients, quote.client));
  }

  protected loadRelationshipsOptions(): void {
    this.clientService
      .query()
      .pipe(map((res: HttpResponse<IClient[]>) => res.body ?? []))
      .pipe(map((clients: IClient[]) => this.clientService.addClientToCollectionIfMissing<IClient>(clients, this.quote?.client)))
      .subscribe((clients: IClient[]) => this.clientsSharedCollection.set(clients));
  }
}
