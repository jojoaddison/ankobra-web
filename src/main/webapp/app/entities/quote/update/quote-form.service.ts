import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';

import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { IQuote, NewQuote } from '../quote.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IQuote for edit and NewQuoteFormGroupInput for create.
 */
type QuoteFormGroupInput = IQuote | PartialWithRequiredKeyOf<NewQuote>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends IQuote | NewQuote> = Omit<T, 'createdDate'> & {
  createdDate?: string | null;
};

type QuoteFormRawValue = FormValueOf<IQuote>;

type NewQuoteFormRawValue = FormValueOf<NewQuote>;

type QuoteFormDefaults = Pick<NewQuote, 'id' | 'createdDate'>;

type QuoteFormGroupContent = {
  id: FormControl<QuoteFormRawValue['id'] | NewQuote['id']>;
  reference: FormControl<QuoteFormRawValue['reference']>;
  title: FormControl<QuoteFormRawValue['title']>;
  createdDate: FormControl<QuoteFormRawValue['createdDate']>;
  status: FormControl<QuoteFormRawValue['status']>;
  client: FormControl<QuoteFormRawValue['client']>;
};

export type QuoteFormGroup = FormGroup<QuoteFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class QuoteFormService {
  createQuoteFormGroup(quote?: QuoteFormGroupInput): QuoteFormGroup {
    const quoteRawValue = this.convertQuoteToQuoteRawValue({
      ...this.getFormDefaults(),
      ...(quote ?? { id: null }),
    });
    return new FormGroup<QuoteFormGroupContent>({
      id: new FormControl(
        { value: quoteRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      reference: new FormControl(quoteRawValue.reference, {
        validators: [Validators.required, Validators.maxLength(20)],
      }),
      title: new FormControl(quoteRawValue.title, {
        validators: [Validators.maxLength(160)],
      }),
      createdDate: new FormControl(quoteRawValue.createdDate),
      status: new FormControl(quoteRawValue.status),
      client: new FormControl(quoteRawValue.client),
    });
  }

  getQuote(form: QuoteFormGroup): IQuote | NewQuote {
    return this.convertQuoteRawValueToQuote(form.getRawValue());
  }

  resetForm(form: QuoteFormGroup, quote: QuoteFormGroupInput): void {
    const quoteRawValue = this.convertQuoteToQuoteRawValue({ ...this.getFormDefaults(), ...quote });
    form.reset({
      ...quoteRawValue,
      id: { value: quoteRawValue.id, disabled: true },
    });
  }

  private getFormDefaults(): QuoteFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      createdDate: currentTime,
    };
  }

  private convertQuoteRawValueToQuote(rawQuote: QuoteFormRawValue | NewQuoteFormRawValue): IQuote | NewQuote {
    return {
      ...rawQuote,
      createdDate: dayjs(rawQuote.createdDate, DATE_TIME_FORMAT),
    };
  }

  private convertQuoteToQuoteRawValue(
    quote: IQuote | (Partial<NewQuote> & QuoteFormDefaults),
  ): QuoteFormRawValue | PartialWithRequiredKeyOf<NewQuoteFormRawValue> {
    return {
      ...quote,
      createdDate: quote.createdDate ? quote.createdDate.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
