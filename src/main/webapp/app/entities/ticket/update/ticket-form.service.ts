import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';

import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { ITicket, NewTicket } from '../ticket.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts ITicket for edit and NewTicketFormGroupInput for create.
 */
type TicketFormGroupInput = ITicket | PartialWithRequiredKeyOf<NewTicket>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends ITicket | NewTicket> = Omit<T, 'openedAt'> & {
  openedAt?: string | null;
};

type TicketFormRawValue = FormValueOf<ITicket>;

type NewTicketFormRawValue = FormValueOf<NewTicket>;

type TicketFormDefaults = Pick<NewTicket, 'id' | 'openedAt'>;

type TicketFormGroupContent = {
  id: FormControl<TicketFormRawValue['id'] | NewTicket['id']>;
  reference: FormControl<TicketFormRawValue['reference']>;
  subject: FormControl<TicketFormRawValue['subject']>;
  priority: FormControl<TicketFormRawValue['priority']>;
  openedAt: FormControl<TicketFormRawValue['openedAt']>;
  slaHours: FormControl<TicketFormRawValue['slaHours']>;
  state: FormControl<TicketFormRawValue['state']>;
  owner: FormControl<TicketFormRawValue['owner']>;
  client: FormControl<TicketFormRawValue['client']>;
};

export type TicketFormGroup = FormGroup<TicketFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class TicketFormService {
  createTicketFormGroup(ticket?: TicketFormGroupInput): TicketFormGroup {
    const ticketRawValue = this.convertTicketToTicketRawValue({
      ...this.getFormDefaults(),
      ...(ticket ?? { id: null }),
    });
    return new FormGroup<TicketFormGroupContent>({
      id: new FormControl(
        { value: ticketRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      reference: new FormControl(ticketRawValue.reference, {
        validators: [Validators.required, Validators.maxLength(20)],
      }),
      subject: new FormControl(ticketRawValue.subject, {
        validators: [Validators.required, Validators.maxLength(200)],
      }),
      priority: new FormControl(ticketRawValue.priority, {
        validators: [Validators.required],
      }),
      openedAt: new FormControl(ticketRawValue.openedAt),
      slaHours: new FormControl(ticketRawValue.slaHours, {
        validators: [Validators.min(0)],
      }),
      state: new FormControl(ticketRawValue.state, {
        validators: [Validators.required],
      }),
      owner: new FormControl(ticketRawValue.owner),
      client: new FormControl(ticketRawValue.client, {
        validators: [Validators.required],
      }),
    });
  }

  getTicket(form: TicketFormGroup): ITicket | NewTicket {
    return this.convertTicketRawValueToTicket(form.getRawValue());
  }

  resetForm(form: TicketFormGroup, ticket: TicketFormGroupInput): void {
    const ticketRawValue = this.convertTicketToTicketRawValue({ ...this.getFormDefaults(), ...ticket });
    form.reset({
      ...ticketRawValue,
      id: { value: ticketRawValue.id, disabled: true },
    });
  }

  private getFormDefaults(): TicketFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      openedAt: currentTime,
    };
  }

  private convertTicketRawValueToTicket(rawTicket: TicketFormRawValue | NewTicketFormRawValue): ITicket | NewTicket {
    return {
      ...rawTicket,
      openedAt: dayjs(rawTicket.openedAt, DATE_TIME_FORMAT),
    };
  }

  private convertTicketToTicketRawValue(
    ticket: ITicket | (Partial<NewTicket> & TicketFormDefaults),
  ): TicketFormRawValue | PartialWithRequiredKeyOf<NewTicketFormRawValue> {
    return {
      ...ticket,
      openedAt: ticket.openedAt ? ticket.openedAt.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
