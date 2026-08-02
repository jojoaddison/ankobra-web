import dayjs from 'dayjs/esm';

import { IProject, NewProject } from './project.model';

export const sampleWithRequiredData: IProject = {
  id: 22823,
  reference: 'anxiously',
  name: 'despite step quash',
  pillar: 'CAPACITY_BUILDING',
  status: 'GOOD',
};

export const sampleWithPartialData: IProject = {
  id: 6444,
  reference: 'before wherever econ',
  name: 'overburden wetly submissive',
  pillar: 'CAPACITY_BUILDING',
  status: 'SERIOUS',
  dueDate: dayjs('2026-08-02'),
  techStack: 'regarding',
};

export const sampleWithFullData: IProject = {
  id: 1375,
  reference: 'tray pack hydrolyze',
  name: 'as',
  pillar: 'DIGITAL_TRANSFORMATION',
  status: 'DONE',
  progress: 28,
  dueDate: dayjs('2026-08-02'),
  delivered: true,
  budget: 1303.72,
  spent: 3413.71,
  techStack: 'convince',
};

export const sampleWithNewData: NewProject = {
  reference: 'zowie vice',
  name: 'ew to',
  pillar: 'ENTERPRISE_INTEGRATION',
  status: 'WARN',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
