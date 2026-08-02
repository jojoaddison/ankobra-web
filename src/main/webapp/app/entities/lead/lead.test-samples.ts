import dayjs from 'dayjs/esm';

import { ILead, NewLead } from './lead.model';

export const sampleWithRequiredData: ILead = {
  id: 12555,
  name: 'measly vice',
  email: 'Victoria22@hotmail.com',
};

export const sampleWithPartialData: ILead = {
  id: 8171,
  name: 'than',
  email: 'Cody46@gmail.com',
  need: 'BESPOKE_SOLUTION',
  message: '../fake-data/blob/hipster.txt',
  createdDate: dayjs('2026-08-01T22:12'),
  status: 'CLOSED',
};

export const sampleWithFullData: ILead = {
  id: 26383,
  name: 'worthy',
  email: 'Justin11@gmail.com',
  need: 'CAPACITY_BUILDING',
  message: '../fake-data/blob/hipster.txt',
  createdDate: dayjs('2026-08-02T09:46'),
  status: 'QUALIFIED',
};

export const sampleWithNewData: NewLead = {
  name: 'slide provided lend',
  email: 'Marie_Dickinson@gmail.com',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
