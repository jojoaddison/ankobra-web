import { HttpClient, HttpResponse, httpResource } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';

import { Observable } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { isPresent } from 'app/core/util/operators';
import { ITeamMember, NewTeamMember } from '../team-member.model';

export type PartialUpdateTeamMember = Partial<ITeamMember> & Pick<ITeamMember, 'id'>;

@Injectable()
export class TeamMembersService {
  readonly teamMembersParams = signal<Record<string, string | number | boolean | readonly (string | number | boolean)[]> | undefined>(
    undefined,
  );
  readonly teamMembersResource = httpResource<ITeamMember[]>(() => {
    const params = this.teamMembersParams();
    if (!params) {
      return undefined;
    }
    return { url: this.resourceUrl, params };
  });
  /**
   * This signal holds the list of teamMember that have been fetched. It is updated when the teamMembersResource emits a new value.
   * In case of error while fetching the teamMembers, the signal is set to an empty array.
   */
  readonly teamMembers = computed(() => (this.teamMembersResource.hasValue() ? this.teamMembersResource.value() : []));
  protected readonly applicationConfigService = inject(ApplicationConfigService);
  protected readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/team-members');
}

@Injectable({ providedIn: 'root' })
export class TeamMemberService extends TeamMembersService {
  protected readonly http = inject(HttpClient);

  create(teamMember: NewTeamMember): Observable<ITeamMember> {
    return this.http.post<ITeamMember>(this.resourceUrl, teamMember);
  }

  update(teamMember: ITeamMember): Observable<ITeamMember> {
    return this.http.put<ITeamMember>(`${this.resourceUrl}/${encodeURIComponent(this.getTeamMemberIdentifier(teamMember))}`, teamMember);
  }

  partialUpdate(teamMember: PartialUpdateTeamMember): Observable<ITeamMember> {
    return this.http.patch<ITeamMember>(`${this.resourceUrl}/${encodeURIComponent(this.getTeamMemberIdentifier(teamMember))}`, teamMember);
  }

  find(id: number): Observable<ITeamMember> {
    return this.http.get<ITeamMember>(`${this.resourceUrl}/${encodeURIComponent(id)}`);
  }

  query(req?: any): Observable<HttpResponse<ITeamMember[]>> {
    const options = createRequestOption(req);
    return this.http.get<ITeamMember[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<undefined> {
    return this.http.delete<undefined>(`${this.resourceUrl}/${encodeURIComponent(id)}`);
  }

  getTeamMemberIdentifier(teamMember: Pick<ITeamMember, 'id'>): number {
    return teamMember.id;
  }

  compareTeamMember(o1: Pick<ITeamMember, 'id'> | null, o2: Pick<ITeamMember, 'id'> | null): boolean {
    return o1 && o2 ? this.getTeamMemberIdentifier(o1) === this.getTeamMemberIdentifier(o2) : o1 === o2;
  }

  addTeamMemberToCollectionIfMissing<Type extends Pick<ITeamMember, 'id'>>(
    teamMemberCollection: Type[],
    ...teamMembersToCheck: (Type | null | undefined)[]
  ): Type[] {
    const teamMembers: Type[] = teamMembersToCheck.filter(isPresent);
    if (teamMembers.length > 0) {
      const teamMemberCollectionIdentifiers = teamMemberCollection.map(teamMemberItem => this.getTeamMemberIdentifier(teamMemberItem));
      const teamMembersToAdd = teamMembers.filter(teamMemberItem => {
        const teamMemberIdentifier = this.getTeamMemberIdentifier(teamMemberItem);
        if (teamMemberCollectionIdentifiers.includes(teamMemberIdentifier)) {
          return false;
        }
        teamMemberCollectionIdentifiers.push(teamMemberIdentifier);
        return true;
      });
      return [...teamMembersToAdd, ...teamMemberCollection];
    }
    return teamMemberCollection;
  }
}
