import { IServiceItem, NewServiceItem } from './service-item.model';

export const sampleWithRequiredData: IServiceItem = {
  id: 23981,
  code: 'whereas',
  name: 'quiet',
  rate: 18646.76,
  unit: 'PER_MODULE',
  serviceGroup: 'TRAINING',
};

export const sampleWithPartialData: IServiceItem = {
  id: 32670,
  code: 'pace than vision',
  name: 'upon',
  rate: 4009.35,
  unit: 'PER_COHORT',
  serviceGroup: 'CONSULTANCY',
};

export const sampleWithFullData: IServiceItem = {
  id: 11862,
  code: 'um license supposing',
  name: 'ugly zowie',
  description: 'wearily roughly',
  rate: 9902.84,
  unit: 'PER_DATASET',
  serviceGroup: 'SERVICES',
};

export const sampleWithNewData: NewServiceItem = {
  code: 'jaggedly splay polis',
  name: 'kowtow now',
  rate: 27637.88,
  unit: 'PER_MONTH',
  serviceGroup: 'SOLUTIONS',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
