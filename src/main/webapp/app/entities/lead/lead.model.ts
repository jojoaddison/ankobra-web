import dayjs from 'dayjs/esm';

import { EnquiryType } from 'app/entities/enumerations/enquiry-type.model';
import { LeadStatus } from 'app/entities/enumerations/lead-status.model';

export interface ILead {
  id: number;
  name?: string | null;
  email?: string | null;
  need?: keyof typeof EnquiryType | null;
  message?: string | null;
  createdDate?: dayjs.Dayjs | null;
  status?: keyof typeof LeadStatus | null;
}

export type NewLead = Omit<ILead, 'id'> & { id: null };
