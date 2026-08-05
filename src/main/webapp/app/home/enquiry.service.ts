import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';

export interface Enquiry {
  name: string;
  email: string;
  need?: string;
  message?: string;
  /**
   * Honeypot. Rendered hidden and off-screen, so a human always leaves it empty; a form-filling bot
   * usually does not. The server silently discards any submission that carries a value.
   */
  website?: string;
}

/** Posts contact-form enquiries to the public (unauthenticated) lead-capture endpoint. */
@Injectable({ providedIn: 'root' })
export class EnquiryService {
  private readonly http = inject(HttpClient);
  private readonly resourceUrl = inject(ApplicationConfigService).getEndpointFor('api/public/enquiries');

  submit(enquiry: Enquiry): Observable<{}> {
    return this.http.post(this.resourceUrl, enquiry);
  }
}
