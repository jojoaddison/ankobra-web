import { Market } from 'app/entities/enumerations/market.model';
import { Status } from 'app/entities/enumerations/status.model';
import { IUser } from 'app/entities/user/user.model';

export interface IClient {
  id: number;
  name?: string | null;
  sector?: keyof typeof Market | null;
  clientSince?: number | null;
  health?: keyof typeof Status | null;
  totalSpend?: number | null;
  user?: Pick<IUser, 'id' | 'login'> | null;
}

export type NewClient = Omit<IClient, 'id'> & { id: null };
