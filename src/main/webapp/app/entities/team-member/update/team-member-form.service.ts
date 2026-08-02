import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { ITeamMember, NewTeamMember } from '../team-member.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts ITeamMember for edit and NewTeamMemberFormGroupInput for create.
 */
type TeamMemberFormGroupInput = ITeamMember | PartialWithRequiredKeyOf<NewTeamMember>;

type TeamMemberFormDefaults = Pick<NewTeamMember, 'id'>;

type TeamMemberFormGroupContent = {
  id: FormControl<ITeamMember['id'] | NewTeamMember['id']>;
  name: FormControl<ITeamMember['name']>;
  initials: FormControl<ITeamMember['initials']>;
  role: FormControl<ITeamMember['role']>;
  qualification: FormControl<ITeamMember['qualification']>;
  bio: FormControl<ITeamMember['bio']>;
  user: FormControl<ITeamMember['user']>;
};

export type TeamMemberFormGroup = FormGroup<TeamMemberFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class TeamMemberFormService {
  createTeamMemberFormGroup(teamMember?: TeamMemberFormGroupInput): TeamMemberFormGroup {
    const teamMemberRawValue = {
      ...this.getFormDefaults(),
      ...(teamMember ?? { id: null }),
    };
    return new FormGroup<TeamMemberFormGroupContent>({
      id: new FormControl(
        { value: teamMemberRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      name: new FormControl(teamMemberRawValue.name, {
        validators: [Validators.required, Validators.maxLength(120)],
      }),
      initials: new FormControl(teamMemberRawValue.initials, {
        validators: [Validators.maxLength(5)],
      }),
      role: new FormControl(teamMemberRawValue.role, {
        validators: [Validators.maxLength(80)],
      }),
      qualification: new FormControl(teamMemberRawValue.qualification, {
        validators: [Validators.maxLength(160)],
      }),
      bio: new FormControl(teamMemberRawValue.bio),
      user: new FormControl(teamMemberRawValue.user),
    });
  }

  getTeamMember(form: TeamMemberFormGroup): ITeamMember | NewTeamMember {
    return form.getRawValue();
  }

  resetForm(form: TeamMemberFormGroup, teamMember: TeamMemberFormGroupInput): void {
    const teamMemberRawValue = { ...this.getFormDefaults(), ...teamMember };
    form.reset({
      ...teamMemberRawValue,
      id: { value: teamMemberRawValue.id, disabled: true },
    });
  }

  private getFormDefaults(): TeamMemberFormDefaults {
    return {
      id: null,
    };
  }
}
