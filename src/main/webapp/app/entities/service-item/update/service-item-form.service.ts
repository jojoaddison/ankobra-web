import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { IServiceItem, NewServiceItem } from '../service-item.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IServiceItem for edit and NewServiceItemFormGroupInput for create.
 */
type ServiceItemFormGroupInput = IServiceItem | PartialWithRequiredKeyOf<NewServiceItem>;

type ServiceItemFormDefaults = Pick<NewServiceItem, 'id'>;

type ServiceItemFormGroupContent = {
  id: FormControl<IServiceItem['id'] | NewServiceItem['id']>;
  code: FormControl<IServiceItem['code']>;
  name: FormControl<IServiceItem['name']>;
  description: FormControl<IServiceItem['description']>;
  rate: FormControl<IServiceItem['rate']>;
  unit: FormControl<IServiceItem['unit']>;
  serviceGroup: FormControl<IServiceItem['serviceGroup']>;
};

export type ServiceItemFormGroup = FormGroup<ServiceItemFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class ServiceItemFormService {
  createServiceItemFormGroup(serviceItem?: ServiceItemFormGroupInput): ServiceItemFormGroup {
    const serviceItemRawValue = {
      ...this.getFormDefaults(),
      ...(serviceItem ?? { id: null }),
    };
    return new FormGroup<ServiceItemFormGroupContent>({
      id: new FormControl(
        { value: serviceItemRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      code: new FormControl(serviceItemRawValue.code, {
        validators: [Validators.required, Validators.maxLength(20)],
      }),
      name: new FormControl(serviceItemRawValue.name, {
        validators: [Validators.required, Validators.maxLength(160)],
      }),
      description: new FormControl(serviceItemRawValue.description, {
        validators: [Validators.maxLength(400)],
      }),
      rate: new FormControl(serviceItemRawValue.rate, {
        validators: [Validators.required, Validators.min(0)],
      }),
      unit: new FormControl(serviceItemRawValue.unit, {
        validators: [Validators.required],
      }),
      serviceGroup: new FormControl(serviceItemRawValue.serviceGroup, {
        validators: [Validators.required],
      }),
    });
  }

  getServiceItem(form: ServiceItemFormGroup): IServiceItem | NewServiceItem {
    return form.getRawValue();
  }

  resetForm(form: ServiceItemFormGroup, serviceItem: ServiceItemFormGroupInput): void {
    const serviceItemRawValue = { ...this.getFormDefaults(), ...serviceItem };
    form.reset({
      ...serviceItemRawValue,
      id: { value: serviceItemRawValue.id, disabled: true },
    });
  }

  private getFormDefaults(): ServiceItemFormDefaults {
    return {
      id: null,
    };
  }
}
