import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { IQuoteLine, NewQuoteLine } from '../quote-line.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IQuoteLine for edit and NewQuoteLineFormGroupInput for create.
 */
type QuoteLineFormGroupInput = IQuoteLine | PartialWithRequiredKeyOf<NewQuoteLine>;

type QuoteLineFormDefaults = Pick<NewQuoteLine, 'id'>;

type QuoteLineFormGroupContent = {
  id: FormControl<IQuoteLine['id'] | NewQuoteLine['id']>;
  quantity: FormControl<IQuoteLine['quantity']>;
  rate: FormControl<IQuoteLine['rate']>;
  item: FormControl<IQuoteLine['item']>;
  quote: FormControl<IQuoteLine['quote']>;
};

export type QuoteLineFormGroup = FormGroup<QuoteLineFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class QuoteLineFormService {
  createQuoteLineFormGroup(quoteLine?: QuoteLineFormGroupInput): QuoteLineFormGroup {
    const quoteLineRawValue = {
      ...this.getFormDefaults(),
      ...(quoteLine ?? { id: null }),
    };
    return new FormGroup<QuoteLineFormGroupContent>({
      id: new FormControl(
        { value: quoteLineRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      quantity: new FormControl(quoteLineRawValue.quantity, {
        validators: [Validators.required, Validators.min(1)],
      }),
      rate: new FormControl(quoteLineRawValue.rate, {
        validators: [Validators.required, Validators.min(0)],
      }),
      item: new FormControl(quoteLineRawValue.item, {
        validators: [Validators.required],
      }),
      quote: new FormControl(quoteLineRawValue.quote, {
        validators: [Validators.required],
      }),
    });
  }

  getQuoteLine(form: QuoteLineFormGroup): IQuoteLine | NewQuoteLine {
    return form.getRawValue();
  }

  resetForm(form: QuoteLineFormGroup, quoteLine: QuoteLineFormGroupInput): void {
    const quoteLineRawValue = { ...this.getFormDefaults(), ...quoteLine };
    form.reset({
      ...quoteLineRawValue,
      id: { value: quoteLineRawValue.id, disabled: true },
    });
  }

  private getFormDefaults(): QuoteLineFormDefaults {
    return {
      id: null,
    };
  }
}
