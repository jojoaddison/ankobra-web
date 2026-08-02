import dayjs from 'dayjs/esm';

import { ITicket, NewTicket } from './ticket.model';

export const sampleWithRequiredData: ITicket = {
  id: 12568,
  reference: 'actual',
  subject: 'until weakly broadcast',
  priority: 'GOOD',
  state: 'OPEN',
};

export const sampleWithPartialData: ITicket = {
  id: 15478,
  reference: 'although',
  subject: 'why duh astride',
  priority: 'CRIT',
  openedAt: dayjs('2026-08-02T01:40'),
  state: 'CLOSED',
};

export const sampleWithFullData: ITicket = {
  id: 14664,
  reference: 'although aboard',
  subject: 'march at',
  priority: 'SERIOUS',
  openedAt: dayjs('2026-08-01T19:57'),
  slaHours: 13533,
  state: 'OPEN',
};

export const sampleWithNewData: NewTicket = {
  reference: 'wee devoted',
  subject: 'farm while',
  priority: 'GOOD',
  state: 'OPEN',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
