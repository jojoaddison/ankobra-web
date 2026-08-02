import { HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, Router } from '@angular/router';

import { EMPTY, Observable, catchError, of } from 'rxjs';

import { IClient } from '../client.model';
import { ClientService } from '../service/client.service';

const clientResolve = (route: ActivatedRouteSnapshot): Observable<null | IClient> => {
  const { id } = route.params;
  if (id) {
    const router = inject(Router);
    const service = inject(ClientService);
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

export default clientResolve;
