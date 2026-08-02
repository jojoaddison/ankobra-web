import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { IProject, NewProject } from '../project.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IProject for edit and NewProjectFormGroupInput for create.
 */
type ProjectFormGroupInput = IProject | PartialWithRequiredKeyOf<NewProject>;

type ProjectFormDefaults = Pick<NewProject, 'id' | 'delivered'>;

type ProjectFormGroupContent = {
  id: FormControl<IProject['id'] | NewProject['id']>;
  reference: FormControl<IProject['reference']>;
  name: FormControl<IProject['name']>;
  pillar: FormControl<IProject['pillar']>;
  status: FormControl<IProject['status']>;
  progress: FormControl<IProject['progress']>;
  dueDate: FormControl<IProject['dueDate']>;
  delivered: FormControl<IProject['delivered']>;
  budget: FormControl<IProject['budget']>;
  spent: FormControl<IProject['spent']>;
  techStack: FormControl<IProject['techStack']>;
  lead: FormControl<IProject['lead']>;
  client: FormControl<IProject['client']>;
};

export type ProjectFormGroup = FormGroup<ProjectFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class ProjectFormService {
  createProjectFormGroup(project?: ProjectFormGroupInput): ProjectFormGroup {
    const projectRawValue = {
      ...this.getFormDefaults(),
      ...(project ?? { id: null }),
    };
    return new FormGroup<ProjectFormGroupContent>({
      id: new FormControl(
        { value: projectRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      reference: new FormControl(projectRawValue.reference, {
        validators: [Validators.required, Validators.maxLength(20)],
      }),
      name: new FormControl(projectRawValue.name, {
        validators: [Validators.required, Validators.maxLength(160)],
      }),
      pillar: new FormControl(projectRawValue.pillar, {
        validators: [Validators.required],
      }),
      status: new FormControl(projectRawValue.status, {
        validators: [Validators.required],
      }),
      progress: new FormControl(projectRawValue.progress, {
        validators: [Validators.min(0), Validators.max(100)],
      }),
      dueDate: new FormControl(projectRawValue.dueDate),
      delivered: new FormControl(projectRawValue.delivered),
      budget: new FormControl(projectRawValue.budget, {
        validators: [Validators.min(0)],
      }),
      spent: new FormControl(projectRawValue.spent, {
        validators: [Validators.min(0)],
      }),
      techStack: new FormControl(projectRawValue.techStack, {
        validators: [Validators.maxLength(255)],
      }),
      lead: new FormControl(projectRawValue.lead),
      client: new FormControl(projectRawValue.client, {
        validators: [Validators.required],
      }),
    });
  }

  getProject(form: ProjectFormGroup): IProject | NewProject {
    return form.getRawValue();
  }

  resetForm(form: ProjectFormGroup, project: ProjectFormGroupInput): void {
    const projectRawValue = { ...this.getFormDefaults(), ...project };
    form.reset({
      ...projectRawValue,
      id: { value: projectRawValue.id, disabled: true },
    });
  }

  private getFormDefaults(): ProjectFormDefaults {
    return {
      id: null,
      delivered: false,
    };
  }
}
