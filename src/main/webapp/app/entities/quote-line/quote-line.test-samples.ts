import { IQuoteLine, NewQuoteLine } from './quote-line.model';

export const sampleWithRequiredData: IQuoteLine = {
  id: 11026,
  quantity: 11089,
  rate: 3736.57,
};

export const sampleWithPartialData: IQuoteLine = {
  id: 24344,
  quantity: 1224,
  rate: 30475.05,
};

export const sampleWithFullData: IQuoteLine = {
  id: 22540,
  quantity: 1435,
  rate: 27672.83,
};

export const sampleWithNewData: NewQuoteLine = {
  quantity: 4238,
  rate: 13881.94,
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
