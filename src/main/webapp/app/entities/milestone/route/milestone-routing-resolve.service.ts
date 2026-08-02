import { HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, Router } from '@angular/router';

import { EMPTY, Observable, catchError, of } from 'rxjs';

import { IMilestone } from '../milestone.model';
import { MilestoneService } from '../service/milestone.service';

const milestoneResolve = (route: ActivatedRouteSnapshot): Observable<null | IMilestone> => {
  const { id } = route.params;
  if (id) {
    const router = inject(Router);
    const service = inject(MilestoneService);
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

export default milestoneResolve;
