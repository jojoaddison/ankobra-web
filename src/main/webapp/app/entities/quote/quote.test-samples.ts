import dayjs from 'dayjs/esm';

import { IQuote, NewQuote } from './quote.model';

export const sampleWithRequiredData: IQuote = {
  id: 24402,
  reference: 'questionably vacantl',
};

export const sampleWithPartialData: IQuote = {
  id: 18049,
  reference: 'thorny pish except',
  status: 'SENT',
};

export const sampleWithFullData: IQuote = {
  id: 28771,
  reference: 'phew awkwardly',
  title: 'whereas why',
  createdDate: dayjs('2026-08-02T10:35'),
  status: 'ACCEPTED',
};

export const sampleWithNewData: NewQuote = {
  reference: 'huzzah',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
