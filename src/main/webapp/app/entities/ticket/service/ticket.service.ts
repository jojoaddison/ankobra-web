import { HttpClient, HttpResponse, httpResource } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';

import dayjs from 'dayjs/esm';
import { Observable, map } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { isPresent } from 'app/core/util/operators';
import { ITicket, NewTicket } from '../ticket.model';

export type PartialUpdateTicket = Partial<ITicket> & Pick<ITicket, 'id'>;

type RestOf<T extends ITicket | NewTicket> = Omit<T, 'openedAt'> & {
  openedAt?: string | null;
};

export type RestTicket = RestOf<ITicket>;

export type NewRestTicket = RestOf<NewTicket>;

export type PartialUpdateRestTicket = RestOf<PartialUpdateTicket>;

@Injectable()
export class TicketsService {
  readonly ticketsParams = signal<Record<string, string | number | boolean | readonly (string | number | boolean)[]> | undefined>(
    undefined,
  );
  readonly ticketsResource = httpResource<RestTicket[]>(() => {
    const params = this.ticketsParams();
    if (!params) {
      return undefined;
    }
    return { url: this.resourceUrl, params };
  });
  /**
   * This signal holds the list of ticket that have been fetched. It is updated when the ticketsResource emits a new value.
   * In case of error while fetching the tickets, the signal is set to an empty array.
   */
  readonly tickets = computed(() =>
    (this.ticketsResource.hasValue() ? this.ticketsResource.value() : []).map(item => this.convertValueFromServer(item)),
  );
  protected readonly applicationConfigService = inject(ApplicationConfigService);
  protected readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/tickets');

  protected convertValueFromServer(restTicket: RestTicket): ITicket {
    return {
      ...restTicket,
      openedAt: restTicket.openedAt ? dayjs(restTicket.openedAt) : undefined,
    };
  }
}

@Injectable({ providedIn: 'root' })
export class TicketService extends TicketsService {
  protected readonly http = inject(HttpClient);

  create(ticket: NewTicket): Observable<ITicket> {
    const copy = this.convertValueFromClient(ticket);
    return this.http.post<RestTicket>(this.resourceUrl, copy).pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(ticket: ITicket): Observable<ITicket> {
    const copy = this.convertValueFromClient(ticket);
    return this.http
      .put<RestTicket>(`${this.resourceUrl}/${encodeURIComponent(this.getTicketIdentifier(ticket))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(ticket: PartialUpdateTicket): Observable<ITicket> {
    const copy = this.convertValueFromClient(ticket);
    return this.http
      .patch<RestTicket>(`${this.resourceUrl}/${encodeURIComponent(this.getTicketIdentifier(ticket))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<ITicket> {
    return this.http.get<RestTicket>(`${this.resourceUrl}/${encodeURIComponent(id)}`).pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<HttpResponse<ITicket[]>> {
    const options = createRequestOption(req);
    return this.http
      .get<RestTicket[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => res.clone({ body: this.convertResponseArrayFromServer(res.body!) })));
  }

  delete(id: number): Observable<undefined> {
    return this.http.delete<undefined>(`${this.resourceUrl}/${encodeURIComponent(id)}`);
  }

  getTicketIdentifier(ticket: Pick<ITicket, 'id'>): number {
    return ticket.id;
  }

  compareTicket(o1: Pick<ITicket, 'id'> | null, o2: Pick<ITicket, 'id'> | null): boolean {
    return o1 && o2 ? this.getTicketIdentifier(o1) === this.getTicketIdentifier(o2) : o1 === o2;
  }

  addTicketToCollectionIfMissing<Type extends Pick<ITicket, 'id'>>(
    ticketCollection: Type[],
    ...ticketsToCheck: (Type | null | undefined)[]
  ): Type[] {
    const tickets: Type[] = ticketsToCheck.filter(isPresent);
    if (tickets.length > 0) {
      const ticketCollectionIdentifiers = ticketCollection.map(ticketItem => this.getTicketIdentifier(ticketItem));
      const ticketsToAdd = tickets.filter(ticketItem => {
        const ticketIdentifier = this.getTicketIdentifier(ticketItem);
        if (ticketCollectionIdentifiers.includes(ticketIdentifier)) {
          return false;
        }
        ticketCollectionIdentifiers.push(ticketIdentifier);
        return true;
      });
      return [...ticketsToAdd, ...ticketCollection];
    }
    return ticketCollection;
  }

  protected convertValueFromClient<T extends ITicket | NewTicket | PartialUpdateTicket>(ticket: T): RestOf<T> {
    return {
      ...ticket,
      openedAt: ticket.openedAt?.toJSON() ?? null,
    };
  }

  protected convertResponseFromServer(res: RestTicket): ITicket {
    return this.convertValueFromServer(res);
  }

  protected convertResponseArrayFromServer(res: RestTicket[]): ITicket[] {
    return res.map(item => this.convertValueFromServer(item));
  }
}
