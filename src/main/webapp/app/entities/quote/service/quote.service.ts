import { HttpClient, HttpResponse, httpResource } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';

import dayjs from 'dayjs/esm';
import { Observable, map } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { isPresent } from 'app/core/util/operators';
import { IQuote, NewQuote } from '../quote.model';

export type PartialUpdateQuote = Partial<IQuote> & Pick<IQuote, 'id'>;

type RestOf<T extends IQuote | NewQuote> = Omit<T, 'createdDate'> & {
  createdDate?: string | null;
};

export type RestQuote = RestOf<IQuote>;

export type NewRestQuote = RestOf<NewQuote>;

export type PartialUpdateRestQuote = RestOf<PartialUpdateQuote>;

@Injectable()
export class QuotesService {
  readonly quotesParams = signal<Record<string, string | number | boolean | readonly (string | number | boolean)[]> | undefined>(undefined);
  readonly quotesResource = httpResource<RestQuote[]>(() => {
    const params = this.quotesParams();
    if (!params) {
      return undefined;
    }
    return { url: this.resourceUrl, params };
  });
  /**
   * This signal holds the list of quote that have been fetched. It is updated when the quotesResource emits a new value.
   * In case of error while fetching the quotes, the signal is set to an empty array.
   */
  readonly quotes = computed(() =>
    (this.quotesResource.hasValue() ? this.quotesResource.value() : []).map(item => this.convertValueFromServer(item)),
  );
  protected readonly applicationConfigService = inject(ApplicationConfigService);
  protected readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/quotes');

  protected convertValueFromServer(restQuote: RestQuote): IQuote {
    return {
      ...restQuote,
      createdDate: restQuote.createdDate ? dayjs(restQuote.createdDate) : undefined,
    };
  }
}

@Injectable({ providedIn: 'root' })
export class QuoteService extends QuotesService {
  protected readonly http = inject(HttpClient);

  create(quote: NewQuote): Observable<IQuote> {
    const copy = this.convertValueFromClient(quote);
    return this.http.post<RestQuote>(this.resourceUrl, copy).pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(quote: IQuote): Observable<IQuote> {
    const copy = this.convertValueFromClient(quote);
    return this.http
      .put<RestQuote>(`${this.resourceUrl}/${encodeURIComponent(this.getQuoteIdentifier(quote))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(quote: PartialUpdateQuote): Observable<IQuote> {
    const copy = this.convertValueFromClient(quote);
    return this.http
      .patch<RestQuote>(`${this.resourceUrl}/${encodeURIComponent(this.getQuoteIdentifier(quote))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<IQuote> {
    return this.http.get<RestQuote>(`${this.resourceUrl}/${encodeURIComponent(id)}`).pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<HttpResponse<IQuote[]>> {
    const options = createRequestOption(req);
    return this.http
      .get<RestQuote[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => res.clone({ body: this.convertResponseArrayFromServer(res.body!) })));
  }

  delete(id: number): Observable<undefined> {
    return this.http.delete<undefined>(`${this.resourceUrl}/${encodeURIComponent(id)}`);
  }

  getQuoteIdentifier(quote: Pick<IQuote, 'id'>): number {
    return quote.id;
  }

  compareQuote(o1: Pick<IQuote, 'id'> | null, o2: Pick<IQuote, 'id'> | null): boolean {
    return o1 && o2 ? this.getQuoteIdentifier(o1) === this.getQuoteIdentifier(o2) : o1 === o2;
  }

  addQuoteToCollectionIfMissing<Type extends Pick<IQuote, 'id'>>(
    quoteCollection: Type[],
    ...quotesToCheck: (Type | null | undefined)[]
  ): Type[] {
    const quotes: Type[] = quotesToCheck.filter(isPresent);
    if (quotes.length > 0) {
      const quoteCollectionIdentifiers = quoteCollection.map(quoteItem => this.getQuoteIdentifier(quoteItem));
      const quotesToAdd = quotes.filter(quoteItem => {
        const quoteIdentifier = this.getQuoteIdentifier(quoteItem);
        if (quoteCollectionIdentifiers.includes(quoteIdentifier)) {
          return false;
        }
        quoteCollectionIdentifiers.push(quoteIdentifier);
        return true;
      });
      return [...quotesToAdd, ...quoteCollection];
    }
    return quoteCollection;
  }

  protected convertValueFromClient<T extends IQuote | NewQuote | PartialUpdateQuote>(quote: T): RestOf<T> {
    return {
      ...quote,
      createdDate: quote.createdDate?.toJSON() ?? null,
    };
  }

  protected convertResponseFromServer(res: RestQuote): IQuote {
    return this.convertValueFromServer(res);
  }

  protected convertResponseArrayFromServer(res: RestQuote[]): IQuote[] {
    return res.map(item => this.convertValueFromServer(item));
  }
}
