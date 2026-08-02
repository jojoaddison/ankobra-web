import dayjs from 'dayjs/esm';

import { IClient } from 'app/entities/client/client.model';
import { Status } from 'app/entities/enumerations/status.model';
import { TicketState } from 'app/entities/enumerations/ticket-state.model';
import { ITeamMember } from 'app/entities/team-member/team-member.model';

export interface ITicket {
  id: number;
  reference?: string | null;
  subject?: string | null;
  priority?: keyof typeof Status | null;
  openedAt?: dayjs.Dayjs | null;
  slaHours?: number | null;
  state?: keyof typeof TicketState | null;
  owner?: Pick<ITeamMember, 'id' | 'name'> | null;
  client?: Pick<IClient, 'id' | 'name'> | null;
}

export type NewTicket = Omit<ITicket, 'id'> & { id: null };
