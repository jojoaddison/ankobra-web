import { HttpClient, HttpResponse, httpResource } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';

import dayjs from 'dayjs/esm';
import { Observable, map } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { isPresent } from 'app/core/util/operators';
import { ILead, NewLead } from '../lead.model';

export type PartialUpdateLead = Partial<ILead> & Pick<ILead, 'id'>;

type RestOf<T extends ILead | NewLead> = Omit<T, 'createdDate'> & {
  createdDate?: string | null;
};

export type RestLead = RestOf<ILead>;

export type NewRestLead = RestOf<NewLead>;

export type PartialUpdateRestLead = RestOf<PartialUpdateLead>;

@Injectable()
export class LeadsService {
  readonly leadsParams = signal<Record<string, string | number | boolean | readonly (string | number | boolean)[]> | undefined>(undefined);
  readonly leadsResource = httpResource<RestLead[]>(() => {
    const params = this.leadsParams();
    if (!params) {
      return undefined;
    }
    return { url: this.resourceUrl, params };
  });
  /**
   * This signal holds the list of lead that have been fetched. It is updated when the leadsResource emits a new value.
   * In case of error while fetching the leads, the signal is set to an empty array.
   */
  readonly leads = computed(() =>
    (this.leadsResource.hasValue() ? this.leadsResource.value() : []).map(item => this.convertValueFromServer(item)),
  );
  protected readonly applicationConfigService = inject(ApplicationConfigService);
  protected readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/leads');

  protected convertValueFromServer(restLead: RestLead): ILead {
    return {
      ...restLead,
      createdDate: restLead.createdDate ? dayjs(restLead.createdDate) : undefined,
    };
  }
}

@Injectable({ providedIn: 'root' })
export class LeadService extends LeadsService {
  protected readonly http = inject(HttpClient);

  create(lead: NewLead): Observable<ILead> {
    const copy = this.convertValueFromClient(lead);
    return this.http.post<RestLead>(this.resourceUrl, copy).pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(lead: ILead): Observable<ILead> {
    const copy = this.convertValueFromClient(lead);
    return this.http
      .put<RestLead>(`${this.resourceUrl}/${encodeURIComponent(this.getLeadIdentifier(lead))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(lead: PartialUpdateLead): Observable<ILead> {
    const copy = this.convertValueFromClient(lead);
    return this.http
      .patch<RestLead>(`${this.resourceUrl}/${encodeURIComponent(this.getLeadIdentifier(lead))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<ILead> {
    return this.http.get<RestLead>(`${this.resourceUrl}/${encodeURIComponent(id)}`).pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<HttpResponse<ILead[]>> {
    const options = createRequestOption(req);
    return this.http
      .get<RestLead[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => res.clone({ body: this.convertResponseArrayFromServer(res.body!) })));
  }

  delete(id: number): Observable<undefined> {
    return this.http.delete<undefined>(`${this.resourceUrl}/${encodeURIComponent(id)}`);
  }

  getLeadIdentifier(lead: Pick<ILead, 'id'>): number {
    return lead.id;
  }

  compareLead(o1: Pick<ILead, 'id'> | null, o2: Pick<ILead, 'id'> | null): boolean {
    return o1 && o2 ? this.getLeadIdentifier(o1) === this.getLeadIdentifier(o2) : o1 === o2;
  }

  addLeadToCollectionIfMissing<Type extends Pick<ILead, 'id'>>(
    leadCollection: Type[],
    ...leadsToCheck: (Type | null | undefined)[]
  ): Type[] {
    const leads: Type[] = leadsToCheck.filter(isPresent);
    if (leads.length > 0) {
      const leadCollectionIdentifiers = leadCollection.map(leadItem => this.getLeadIdentifier(leadItem));
      const leadsToAdd = leads.filter(leadItem => {
        const leadIdentifier = this.getLeadIdentifier(leadItem);
        if (leadCollectionIdentifiers.includes(leadIdentifier)) {
          return false;
        }
        leadCollectionIdentifiers.push(leadIdentifier);
        return true;
      });
      return [...leadsToAdd, ...leadCollection];
    }
    return leadCollection;
  }

  protected convertValueFromClient<T extends ILead | NewLead | PartialUpdateLead>(lead: T): RestOf<T> {
    return {
      ...lead,
      createdDate: lead.createdDate?.toJSON() ?? null,
    };
  }

  protected convertResponseFromServer(res: RestLead): ILead {
    return this.convertValueFromServer(res);
  }

  protected convertResponseArrayFromServer(res: RestLead[]): ILead[] {
    return res.map(item => this.convertValueFromServer(item));
  }
}
