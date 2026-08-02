import { ICourse, NewCourse } from './course.model';

export const sampleWithRequiredData: ICourse = {
  id: 8824,
  name: 'radiant',
};

export const sampleWithPartialData: ICourse = {
  id: 4058,
  name: 'towards to orientate',
  description: 'supposing',
  moduleCount: 29432,
  progress: 1,
};

export const sampleWithFullData: ICourse = {
  id: 30968,
  name: 'weakly',
  description: 'whenever pro',
  moduleCount: 13895,
  mode: 'VIRTUAL',
  labBased: true,
  enrolledCount: 13316,
  progress: 100,
};

export const sampleWithNewData: NewCourse = {
  name: 'unto',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
