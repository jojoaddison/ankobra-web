import { HttpClient, HttpResponse, httpResource } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';

import { Observable } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { isPresent } from 'app/core/util/operators';
import { IServiceItem, NewServiceItem } from '../service-item.model';

export type PartialUpdateServiceItem = Partial<IServiceItem> & Pick<IServiceItem, 'id'>;

@Injectable()
export class ServiceItemsService {
  readonly serviceItemsParams = signal<Record<string, string | number | boolean | readonly (string | number | boolean)[]> | undefined>(
    undefined,
  );
  readonly serviceItemsResource = httpResource<IServiceItem[]>(() => {
    const params = this.serviceItemsParams();
    if (!params) {
      return undefined;
    }
    return { url: this.resourceUrl, params };
  });
  /**
   * This signal holds the list of serviceItem that have been fetched. It is updated when the serviceItemsResource emits a new value.
   * In case of error while fetching the serviceItems, the signal is set to an empty array.
   */
  readonly serviceItems = computed(() => (this.serviceItemsResource.hasValue() ? this.serviceItemsResource.value() : []));
  protected readonly applicationConfigService = inject(ApplicationConfigService);
  protected readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/service-items');
}

@Injectable({ providedIn: 'root' })
export class ServiceItemService extends ServiceItemsService {
  protected readonly http = inject(HttpClient);

  create(serviceItem: NewServiceItem): Observable<IServiceItem> {
    return this.http.post<IServiceItem>(this.resourceUrl, serviceItem);
  }

  update(serviceItem: IServiceItem): Observable<IServiceItem> {
    return this.http.put<IServiceItem>(
      `${this.resourceUrl}/${encodeURIComponent(this.getServiceItemIdentifier(serviceItem))}`,
      serviceItem,
    );
  }

  partialUpdate(serviceItem: PartialUpdateServiceItem): Observable<IServiceItem> {
    return this.http.patch<IServiceItem>(
      `${this.resourceUrl}/${encodeURIComponent(this.getServiceItemIdentifier(serviceItem))}`,
      serviceItem,
    );
  }

  find(id: number): Observable<IServiceItem> {
    return this.http.get<IServiceItem>(`${this.resourceUrl}/${encodeURIComponent(id)}`);
  }

  query(req?: any): Observable<HttpResponse<IServiceItem[]>> {
    const options = createRequestOption(req);
    return this.http.get<IServiceItem[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<undefined> {
    return this.http.delete<undefined>(`${this.resourceUrl}/${encodeURIComponent(id)}`);
  }

  getServiceItemIdentifier(serviceItem: Pick<IServiceItem, 'id'>): number {
    return serviceItem.id;
  }

  compareServiceItem(o1: Pick<IServiceItem, 'id'> | null, o2: Pick<IServiceItem, 'id'> | null): boolean {
    return o1 && o2 ? this.getServiceItemIdentifier(o1) === this.getServiceItemIdentifier(o2) : o1 === o2;
  }

  addServiceItemToCollectionIfMissing<Type extends Pick<IServiceItem, 'id'>>(
    serviceItemCollection: Type[],
    ...serviceItemsToCheck: (Type | null | undefined)[]
  ): Type[] {
    const serviceItems: Type[] = serviceItemsToCheck.filter(isPresent);
    if (serviceItems.length > 0) {
      const serviceItemCollectionIdentifiers = serviceItemCollection.map(serviceItemItem => this.getServiceItemIdentifier(serviceItemItem));
      const serviceItemsToAdd = serviceItems.filter(serviceItemItem => {
        const serviceItemIdentifier = this.getServiceItemIdentifier(serviceItemItem);
        if (serviceItemCollectionIdentifiers.includes(serviceItemIdentifier)) {
          return false;
        }
        serviceItemCollectionIdentifiers.push(serviceItemIdentifier);
        return true;
      });
      return [...serviceItemsToAdd, ...serviceItemCollection];
    }
    return serviceItemCollection;
  }
}
