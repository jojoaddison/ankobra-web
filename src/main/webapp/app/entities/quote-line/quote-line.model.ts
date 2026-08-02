import { IQuote } from 'app/entities/quote/quote.model';
import { IServiceItem } from 'app/entities/service-item/service-item.model';

export interface IQuoteLine {
  id: number;
  quantity?: number | null;
  rate?: number | null;
  item?: Pick<IServiceItem, 'id' | 'name'> | null;
  quote?: Pick<IQuote, 'id' | 'reference'> | null;
}

export type NewQuoteLine = Omit<IQuoteLine, 'id'> & { id: null };
