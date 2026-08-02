import { MilestoneState } from 'app/entities/enumerations/milestone-state.model';
import { IProject } from 'app/entities/project/project.model';

export interface IMilestone {
  id: number;
  title?: string | null;
  state?: keyof typeof MilestoneState | null;
  position?: number | null;
  project?: Pick<IProject, 'id' | 'name'> | null;
}

export type NewMilestone = Omit<IMilestone, 'id'> & { id: null };
