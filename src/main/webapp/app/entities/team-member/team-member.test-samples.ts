import { ITeamMember, NewTeamMember } from './team-member.model';

export const sampleWithRequiredData: ITeamMember = {
  id: 2894,
  name: 'but morbidity',
};

export const sampleWithPartialData: ITeamMember = {
  id: 20078,
  name: 'shrilly glimmer like',
  qualification: 'scotch utterly after',
};

export const sampleWithFullData: ITeamMember = {
  id: 10195,
  name: 'scheme till backburn',
  initials: 'inasm',
  role: 'noteworthy',
  qualification: 'noteworthy feather opposite',
  bio: '../fake-data/blob/hipster.txt',
};

export const sampleWithNewData: NewTeamMember = {
  name: 'opposite',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
