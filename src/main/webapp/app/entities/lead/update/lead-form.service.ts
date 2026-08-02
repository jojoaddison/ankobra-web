import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';

import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { ILead, NewLead } from '../lead.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts ILead for edit and NewLeadFormGroupInput for create.
 */
type LeadFormGroupInput = ILead | PartialWithRequiredKeyOf<NewLead>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends ILead | NewLead> = Omit<T, 'createdDate'> & {
  createdDate?: string | null;
};

type LeadFormRawValue = FormValueOf<ILead>;

type NewLeadFormRawValue = FormValueOf<NewLead>;

type LeadFormDefaults = Pick<NewLead, 'id' | 'createdDate'>;

type LeadFormGroupContent = {
  id: FormControl<LeadFormRawValue['id'] | NewLead['id']>;
  name: FormControl<LeadFormRawValue['name']>;
  email: FormControl<LeadFormRawValue['email']>;
  need: FormControl<LeadFormRawValue['need']>;
  message: FormControl<LeadFormRawValue['message']>;
  createdDate: FormControl<LeadFormRawValue['createdDate']>;
  status: FormControl<LeadFormRawValue['status']>;
};

export type LeadFormGroup = FormGroup<LeadFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class LeadFormService {
  createLeadFormGroup(lead?: LeadFormGroupInput): LeadFormGroup {
    const leadRawValue = this.convertLeadToLeadRawValue({
      ...this.getFormDefaults(),
      ...(lead ?? { id: null }),
    });
    return new FormGroup<LeadFormGroupContent>({
      id: new FormControl(
        { value: leadRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      name: new FormControl(leadRawValue.name, {
        validators: [Validators.required, Validators.maxLength(120)],
      }),
      email: new FormControl(leadRawValue.email, {
        validators: [Validators.required, Validators.maxLength(160)],
      }),
      need: new FormControl(leadRawValue.need),
      message: new FormControl(leadRawValue.message),
      createdDate: new FormControl(leadRawValue.createdDate),
      status: new FormControl(leadRawValue.status),
    });
  }

  getLead(form: LeadFormGroup): ILead | NewLead {
    return this.convertLeadRawValueToLead(form.getRawValue());
  }

  resetForm(form: LeadFormGroup, lead: LeadFormGroupInput): void {
    const leadRawValue = this.convertLeadToLeadRawValue({ ...this.getFormDefaults(), ...lead });
    form.reset({
      ...leadRawValue,
      id: { value: leadRawValue.id, disabled: true },
    });
  }

  private getFormDefaults(): LeadFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      createdDate: currentTime,
    };
  }

  private convertLeadRawValueToLead(rawLead: LeadFormRawValue | NewLeadFormRawValue): ILead | NewLead {
    return {
      ...rawLead,
      createdDate: dayjs(rawLead.createdDate, DATE_TIME_FORMAT),
    };
  }

  private convertLeadToLeadRawValue(
    lead: ILead | (Partial<NewLead> & LeadFormDefaults),
  ): LeadFormRawValue | PartialWithRequiredKeyOf<NewLeadFormRawValue> {
    return {
      ...lead,
      createdDate: lead.createdDate ? lead.createdDate.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
