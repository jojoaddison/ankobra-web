import { DeliveryMode } from 'app/entities/enumerations/delivery-mode.model';

export interface ICourse {
  id: number;
  name?: string | null;
  description?: string | null;
  moduleCount?: number | null;
  mode?: keyof typeof DeliveryMode | null;
  labBased?: boolean | null;
  enrolledCount?: number | null;
  progress?: number | null;
}

export type NewCourse = Omit<ICourse, 'id'> & { id: null };
