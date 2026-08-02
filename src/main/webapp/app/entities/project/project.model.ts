import dayjs from 'dayjs/esm';

import { IClient } from 'app/entities/client/client.model';
import { ServicePillar } from 'app/entities/enumerations/service-pillar.model';
import { Status } from 'app/entities/enumerations/status.model';
import { ITeamMember } from 'app/entities/team-member/team-member.model';

export interface IProject {
  id: number;
  reference?: string | null;
  name?: string | null;
  pillar?: keyof typeof ServicePillar | null;
  status?: keyof typeof Status | null;
  progress?: number | null;
  dueDate?: dayjs.Dayjs | null;
  delivered?: boolean | null;
  budget?: number | null;
  spent?: number | null;
  techStack?: string | null;
  lead?: Pick<ITeamMember, 'id' | 'name'> | null;
  client?: Pick<IClient, 'id' | 'name'> | null;
}

export type NewProject = Omit<IProject, 'id'> & { id: null };
