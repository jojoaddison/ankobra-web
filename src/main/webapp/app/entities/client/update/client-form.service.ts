import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { IClient, NewClient } from '../client.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IClient for edit and NewClientFormGroupInput for create.
 */
type ClientFormGroupInput = IClient | PartialWithRequiredKeyOf<NewClient>;

type ClientFormDefaults = Pick<NewClient, 'id'>;

type ClientFormGroupContent = {
  id: FormControl<IClient['id'] | NewClient['id']>;
  name: FormControl<IClient['name']>;
  sector: FormControl<IClient['sector']>;
  clientSince: FormControl<IClient['clientSince']>;
  health: FormControl<IClient['health']>;
  totalSpend: FormControl<IClient['totalSpend']>;
  user: FormControl<IClient['user']>;
};

export type ClientFormGroup = FormGroup<ClientFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class ClientFormService {
  createClientFormGroup(client?: ClientFormGroupInput): ClientFormGroup {
    const clientRawValue = {
      ...this.getFormDefaults(),
      ...(client ?? { id: null }),
    };
    return new FormGroup<ClientFormGroupContent>({
      id: new FormControl(
        { value: clientRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      name: new FormControl(clientRawValue.name, {
        validators: [Validators.required, Validators.maxLength(120)],
      }),
      sector: new FormControl(clientRawValue.sector),
      clientSince: new FormControl(clientRawValue.clientSince),
      health: new FormControl(clientRawValue.health),
      totalSpend: new FormControl(clientRawValue.totalSpend, {
        validators: [Validators.min(0)],
      }),
      user: new FormControl(clientRawValue.user),
    });
  }

  getClient(form: ClientFormGroup): IClient | NewClient {
    return form.getRawValue();
  }

  resetForm(form: ClientFormGroup, client: ClientFormGroupInput): void {
    const clientRawValue = { ...this.getFormDefaults(), ...client };
    form.reset({
      ...clientRawValue,
      id: { value: clientRawValue.id, disabled: true },
    });
  }

  private getFormDefaults(): ClientFormDefaults {
    return {
      id: null,
    };
  }
}
