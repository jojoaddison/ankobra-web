import { IUser } from 'app/entities/user/user.model';

export interface ITeamMember {
  id: number;
  name?: string | null;
  initials?: string | null;
  role?: string | null;
  qualification?: string | null;
  bio?: string | null;
  user?: Pick<IUser, 'id' | 'login'> | null;
}

export type NewTeamMember = Omit<ITeamMember, 'id'> & { id: null };
