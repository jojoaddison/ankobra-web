import { HttpClient, HttpResponse, httpResource } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';

import { Observable } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { isPresent } from 'app/core/util/operators';
import { IClient, NewClient } from '../client.model';

export type PartialUpdateClient = Partial<IClient> & Pick<IClient, 'id'>;

@Injectable()
export class ClientsService {
  readonly clientsParams = signal<Record<string, string | number | boolean | readonly (string | number | boolean)[]> | undefined>(
    undefined,
  );
  readonly clientsResource = httpResource<IClient[]>(() => {
    const params = this.clientsParams();
    if (!params) {
      return undefined;
    }
    return { url: this.resourceUrl, params };
  });
  /**
   * This signal holds the list of client that have been fetched. It is updated when the clientsResource emits a new value.
   * In case of error while fetching the clients, the signal is set to an empty array.
   */
  readonly clients = computed(() => (this.clientsResource.hasValue() ? this.clientsResource.value() : []));
  protected readonly applicationConfigService = inject(ApplicationConfigService);
  protected readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/clients');
}

@Injectable({ providedIn: 'root' })
export class ClientService extends ClientsService {
  protected readonly http = inject(HttpClient);

  create(client: NewClient): Observable<IClient> {
    return this.http.post<IClient>(this.resourceUrl, client);
  }

  update(client: IClient): Observable<IClient> {
    return this.http.put<IClient>(`${this.resourceUrl}/${encodeURIComponent(this.getClientIdentifier(client))}`, client);
  }

  partialUpdate(client: PartialUpdateClient): Observable<IClient> {
    return this.http.patch<IClient>(`${this.resourceUrl}/${encodeURIComponent(this.getClientIdentifier(client))}`, client);
  }

  find(id: number): Observable<IClient> {
    return this.http.get<IClient>(`${this.resourceUrl}/${encodeURIComponent(id)}`);
  }

  query(req?: any): Observable<HttpResponse<IClient[]>> {
    const options = createRequestOption(req);
    return this.http.get<IClient[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<undefined> {
    return this.http.delete<undefined>(`${this.resourceUrl}/${encodeURIComponent(id)}`);
  }

  getClientIdentifier(client: Pick<IClient, 'id'>): number {
    return client.id;
  }

  compareClient(o1: Pick<IClient, 'id'> | null, o2: Pick<IClient, 'id'> | null): boolean {
    return o1 && o2 ? this.getClientIdentifier(o1) === this.getClientIdentifier(o2) : o1 === o2;
  }

  addClientToCollectionIfMissing<Type extends Pick<IClient, 'id'>>(
    clientCollection: Type[],
    ...clientsToCheck: (Type | null | undefined)[]
  ): Type[] {
    const clients: Type[] = clientsToCheck.filter(isPresent);
    if (clients.length > 0) {
      const clientCollectionIdentifiers = clientCollection.map(clientItem => this.getClientIdentifier(clientItem));
      const clientsToAdd = clients.filter(clientItem => {
        const clientIdentifier = this.getClientIdentifier(clientItem);
        if (clientCollectionIdentifiers.includes(clientIdentifier)) {
          return false;
        }
        clientCollectionIdentifiers.push(clientIdentifier);
        return true;
      });
      return [...clientsToAdd, ...clientCollection];
    }
    return clientCollection;
  }
}
