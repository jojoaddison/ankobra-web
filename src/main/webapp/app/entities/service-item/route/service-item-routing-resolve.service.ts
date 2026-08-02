import { HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, Router } from '@angular/router';

import { EMPTY, Observable, catchError, of } from 'rxjs';

import { ServiceItemService } from '../service/service-item.service';
import { IServiceItem } from '../service-item.model';

const serviceItemResolve = (route: ActivatedRouteSnapshot): Observable<null | IServiceItem> => {
  const { id } = route.params;
  if (id) {
    const router = inject(Router);
    const service = inject(ServiceItemService);
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

export default serviceItemResolve;
