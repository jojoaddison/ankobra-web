import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { IMilestone, NewMilestone } from '../milestone.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IMilestone for edit and NewMilestoneFormGroupInput for create.
 */
type MilestoneFormGroupInput = IMilestone | PartialWithRequiredKeyOf<NewMilestone>;

type MilestoneFormDefaults = Pick<NewMilestone, 'id'>;

type MilestoneFormGroupContent = {
  id: FormControl<IMilestone['id'] | NewMilestone['id']>;
  title: FormControl<IMilestone['title']>;
  state: FormControl<IMilestone['state']>;
  position: FormControl<IMilestone['position']>;
  project: FormControl<IMilestone['project']>;
};

export type MilestoneFormGroup = FormGroup<MilestoneFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class MilestoneFormService {
  createMilestoneFormGroup(milestone?: MilestoneFormGroupInput): MilestoneFormGroup {
    const milestoneRawValue = {
      ...this.getFormDefaults(),
      ...(milestone ?? { id: null }),
    };
    return new FormGroup<MilestoneFormGroupContent>({
      id: new FormControl(
        { value: milestoneRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      title: new FormControl(milestoneRawValue.title, {
        validators: [Validators.required, Validators.maxLength(160)],
      }),
      state: new FormControl(milestoneRawValue.state, {
        validators: [Validators.required],
      }),
      position: new FormControl(milestoneRawValue.position, {
        validators: [Validators.required, Validators.min(0)],
      }),
      project: new FormControl(milestoneRawValue.project, {
        validators: [Validators.required],
      }),
    });
  }

  getMilestone(form: MilestoneFormGroup): IMilestone | NewMilestone {
    return form.getRawValue();
  }

  resetForm(form: MilestoneFormGroup, milestone: MilestoneFormGroupInput): void {
    const milestoneRawValue = { ...this.getFormDefaults(), ...milestone };
    form.reset({
      ...milestoneRawValue,
      id: { value: milestoneRawValue.id, disabled: true },
    });
  }

  private getFormDefaults(): MilestoneFormDefaults {
    return {
      id: null,
    };
  }
}
