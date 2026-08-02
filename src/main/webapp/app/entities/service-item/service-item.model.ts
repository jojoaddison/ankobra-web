import { CatalogueGroup } from 'app/entities/enumerations/catalogue-group.model';
import { RateUnit } from 'app/entities/enumerations/rate-unit.model';

export interface IServiceItem {
  id: number;
  code?: string | null;
  name?: string | null;
  description?: string | null;
  rate?: number | null;
  unit?: keyof typeof RateUnit | null;
  serviceGroup?: keyof typeof CatalogueGroup | null;
}

export type NewServiceItem = Omit<IServiceItem, 'id'> & { id: null };
