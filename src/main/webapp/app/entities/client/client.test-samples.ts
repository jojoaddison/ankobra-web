import { IClient, NewClient } from './client.model';

export const sampleWithRequiredData: IClient = {
  id: 16289,
  name: 'save colour yippee',
};

export const sampleWithPartialData: IClient = {
  id: 1127,
  name: 'absolve mortally',
  sector: 'INSTITUTIONS_COMMISSIONS_AUTHORITIES',
  clientSince: 9964,
  health: 'GOOD',
  totalSpend: 10364.93,
};

export const sampleWithFullData: IClient = {
  id: 31496,
  name: 'boohoo shipper soggy',
  sector: 'COMMUNICATION',
  clientSince: 30008,
  health: 'WARN',
  totalSpend: 31669.26,
};

export const sampleWithNewData: NewClient = {
  name: 'seemingly excepting',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
