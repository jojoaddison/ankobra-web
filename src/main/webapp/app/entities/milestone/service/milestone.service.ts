import { HttpClient, HttpResponse, httpResource } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';

import { Observable } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { isPresent } from 'app/core/util/operators';
import { IMilestone, NewMilestone } from '../milestone.model';

export type PartialUpdateMilestone = Partial<IMilestone> & Pick<IMilestone, 'id'>;

@Injectable()
export class MilestonesService {
  readonly milestonesParams = signal<Record<string, string | number | boolean | readonly (string | number | boolean)[]> | undefined>(
    undefined,
  );
  readonly milestonesResource = httpResource<IMilestone[]>(() => {
    const params = this.milestonesParams();
    if (!params) {
      return undefined;
    }
    return { url: this.resourceUrl, params };
  });
  /**
   * This signal holds the list of milestone that have been fetched. It is updated when the milestonesResource emits a new value.
   * In case of error while fetching the milestones, the signal is set to an empty array.
   */
  readonly milestones = computed(() => (this.milestonesResource.hasValue() ? this.milestonesResource.value() : []));
  protected readonly applicationConfigService = inject(ApplicationConfigService);
  protected readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/milestones');
}

@Injectable({ providedIn: 'root' })
export class MilestoneService extends MilestonesService {
  protected readonly http = inject(HttpClient);

  create(milestone: NewMilestone): Observable<IMilestone> {
    return this.http.post<IMilestone>(this.resourceUrl, milestone);
  }

  update(milestone: IMilestone): Observable<IMilestone> {
    return this.http.put<IMilestone>(`${this.resourceUrl}/${encodeURIComponent(this.getMilestoneIdentifier(milestone))}`, milestone);
  }

  partialUpdate(milestone: PartialUpdateMilestone): Observable<IMilestone> {
    return this.http.patch<IMilestone>(`${this.resourceUrl}/${encodeURIComponent(this.getMilestoneIdentifier(milestone))}`, milestone);
  }

  find(id: number): Observable<IMilestone> {
    return this.http.get<IMilestone>(`${this.resourceUrl}/${encodeURIComponent(id)}`);
  }

  query(req?: any): Observable<HttpResponse<IMilestone[]>> {
    const options = createRequestOption(req);
    return this.http.get<IMilestone[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<undefined> {
    return this.http.delete<undefined>(`${this.resourceUrl}/${encodeURIComponent(id)}`);
  }

  getMilestoneIdentifier(milestone: Pick<IMilestone, 'id'>): number {
    return milestone.id;
  }

  compareMilestone(o1: Pick<IMilestone, 'id'> | null, o2: Pick<IMilestone, 'id'> | null): boolean {
    return o1 && o2 ? this.getMilestoneIdentifier(o1) === this.getMilestoneIdentifier(o2) : o1 === o2;
  }

  addMilestoneToCollectionIfMissing<Type extends Pick<IMilestone, 'id'>>(
    milestoneCollection: Type[],
    ...milestonesToCheck: (Type | null | undefined)[]
  ): Type[] {
    const milestones: Type[] = milestonesToCheck.filter(isPresent);
    if (milestones.length > 0) {
      const milestoneCollectionIdentifiers = milestoneCollection.map(milestoneItem => this.getMilestoneIdentifier(milestoneItem));
      const milestonesToAdd = milestones.filter(milestoneItem => {
        const milestoneIdentifier = this.getMilestoneIdentifier(milestoneItem);
        if (milestoneCollectionIdentifiers.includes(milestoneIdentifier)) {
          return false;
        }
        milestoneCollectionIdentifiers.push(milestoneIdentifier);
        return true;
      });
      return [...milestonesToAdd, ...milestoneCollection];
    }
    return milestoneCollection;
  }
}
