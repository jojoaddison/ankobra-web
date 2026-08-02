import dayjs from 'dayjs/esm';

import { IClient } from 'app/entities/client/client.model';
import { QuoteStatus } from 'app/entities/enumerations/quote-status.model';

export interface IQuote {
  id: number;
  reference?: string | null;
  title?: string | null;
  createdDate?: dayjs.Dayjs | null;
  status?: keyof typeof QuoteStatus | null;
  client?: Pick<IClient, 'id' | 'name'> | null;
}

export type NewQuote = Omit<IQuote, 'id'> & { id: null };
