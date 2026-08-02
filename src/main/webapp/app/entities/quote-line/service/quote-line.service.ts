import { HttpClient, HttpResponse, httpResource } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';

import { Observable } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { isPresent } from 'app/core/util/operators';
import { IQuoteLine, NewQuoteLine } from '../quote-line.model';

export type PartialUpdateQuoteLine = Partial<IQuoteLine> & Pick<IQuoteLine, 'id'>;

@Injectable()
export class QuoteLinesService {
  readonly quoteLinesParams = signal<Record<string, string | number | boolean | readonly (string | number | boolean)[]> | undefined>(
    undefined,
  );
  readonly quoteLinesResource = httpResource<IQuoteLine[]>(() => {
    const params = this.quoteLinesParams();
    if (!params) {
      return undefined;
    }
    return { url: this.resourceUrl, params };
  });
  /**
   * This signal holds the list of quoteLine that have been fetched. It is updated when the quoteLinesResource emits a new value.
   * In case of error while fetching the quoteLines, the signal is set to an empty array.
   */
  readonly quoteLines = computed(() => (this.quoteLinesResource.hasValue() ? this.quoteLinesResource.value() : []));
  protected readonly applicationConfigService = inject(ApplicationConfigService);
  protected readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/quote-lines');
}

@Injectable({ providedIn: 'root' })
export class QuoteLineService extends QuoteLinesService {
  protected readonly http = inject(HttpClient);

  create(quoteLine: NewQuoteLine): Observable<IQuoteLine> {
    return this.http.post<IQuoteLine>(this.resourceUrl, quoteLine);
  }

  update(quoteLine: IQuoteLine): Observable<IQuoteLine> {
    return this.http.put<IQuoteLine>(`${this.resourceUrl}/${encodeURIComponent(this.getQuoteLineIdentifier(quoteLine))}`, quoteLine);
  }

  partialUpdate(quoteLine: PartialUpdateQuoteLine): Observable<IQuoteLine> {
    return this.http.patch<IQuoteLine>(`${this.resourceUrl}/${encodeURIComponent(this.getQuoteLineIdentifier(quoteLine))}`, quoteLine);
  }

  find(id: number): Observable<IQuoteLine> {
    return this.http.get<IQuoteLine>(`${this.resourceUrl}/${encodeURIComponent(id)}`);
  }

  query(req?: any): Observable<HttpResponse<IQuoteLine[]>> {
    const options = createRequestOption(req);
    return this.http.get<IQuoteLine[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<undefined> {
    return this.http.delete<undefined>(`${this.resourceUrl}/${encodeURIComponent(id)}`);
  }

  getQuoteLineIdentifier(quoteLine: Pick<IQuoteLine, 'id'>): number {
    return quoteLine.id;
  }

  compareQuoteLine(o1: Pick<IQuoteLine, 'id'> | null, o2: Pick<IQuoteLine, 'id'> | null): boolean {
    return o1 && o2 ? this.getQuoteLineIdentifier(o1) === this.getQuoteLineIdentifier(o2) : o1 === o2;
  }

  addQuoteLineToCollectionIfMissing<Type extends Pick<IQuoteLine, 'id'>>(
    quoteLineCollection: Type[],
    ...quoteLinesToCheck: (Type | null | undefined)[]
  ): Type[] {
    const quoteLines: Type[] = quoteLinesToCheck.filter(isPresent);
    if (quoteLines.length > 0) {
      const quoteLineCollectionIdentifiers = quoteLineCollection.map(quoteLineItem => this.getQuoteLineIdentifier(quoteLineItem));
      const quoteLinesToAdd = quoteLines.filter(quoteLineItem => {
        const quoteLineIdentifier = this.getQuoteLineIdentifier(quoteLineItem);
        if (quoteLineCollectionIdentifiers.includes(quoteLineIdentifier)) {
          return false;
        }
        quoteLineCollectionIdentifiers.push(quoteLineIdentifier);
        return true;
      });
      return [...quoteLinesToAdd, ...quoteLineCollection];
    }
    return quoteLineCollection;
  }
}
