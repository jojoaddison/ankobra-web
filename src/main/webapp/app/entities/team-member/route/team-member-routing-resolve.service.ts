import { HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, Router } from '@angular/router';

import { EMPTY, Observable, catchError, of } from 'rxjs';

import { TeamMemberService } from '../service/team-member.service';
import { ITeamMember } from '../team-member.model';

const teamMemberResolve = (route: ActivatedRouteSnapshot): Observable<null | ITeamMember> => {
  const { id } = route.params;
  if (id) {
    const router = inject(Router);
    const service = inject(TeamMemberService);
    return service.find(id).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 404) {
          router.navigate(['404']);
        } else {
          router.navigate(['error']);
        }
        return EMPTY;
      }),
    );
  }

  return of(null);
};

export default teamMemberResolve;
