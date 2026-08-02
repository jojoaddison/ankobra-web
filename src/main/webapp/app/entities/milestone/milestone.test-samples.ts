import { IMilestone, NewMilestone } from './milestone.model';

export const sampleWithRequiredData: IMilestone = {
  id: 23161,
  title: 'zowie forenenst very',
  state: 'NOW',
  position: 22516,
};

export const sampleWithPartialData: IMilestone = {
  id: 5186,
  title: 'amidst yet',
  state: 'NEXT',
  position: 27816,
};

export const sampleWithFullData: IMilestone = {
  id: 22588,
  title: 'opposite',
  state: 'NEXT',
  position: 16353,
};

export const sampleWithNewData: NewMilestone = {
  title: 'pointed contravene',
  state: 'DONE',
  position: 8530,
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
