import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { ICourse, NewCourse } from '../course.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts ICourse for edit and NewCourseFormGroupInput for create.
 */
type CourseFormGroupInput = ICourse | PartialWithRequiredKeyOf<NewCourse>;

type CourseFormDefaults = Pick<NewCourse, 'id' | 'labBased'>;

type CourseFormGroupContent = {
  id: FormControl<ICourse['id'] | NewCourse['id']>;
  name: FormControl<ICourse['name']>;
  description: FormControl<ICourse['description']>;
  moduleCount: FormControl<ICourse['moduleCount']>;
  mode: FormControl<ICourse['mode']>;
  labBased: FormControl<ICourse['labBased']>;
  enrolledCount: FormControl<ICourse['enrolledCount']>;
  progress: FormControl<ICourse['progress']>;
};

export type CourseFormGroup = FormGroup<CourseFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class CourseFormService {
  createCourseFormGroup(course?: CourseFormGroupInput): CourseFormGroup {
    const courseRawValue = {
      ...this.getFormDefaults(),
      ...(course ?? { id: null }),
    };
    return new FormGroup<CourseFormGroupContent>({
      id: new FormControl(
        { value: courseRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      name: new FormControl(courseRawValue.name, {
        validators: [Validators.required, Validators.maxLength(160)],
      }),
      description: new FormControl(courseRawValue.description, {
        validators: [Validators.maxLength(300)],
      }),
      moduleCount: new FormControl(courseRawValue.moduleCount, {
        validators: [Validators.min(0)],
      }),
      mode: new FormControl(courseRawValue.mode),
      labBased: new FormControl(courseRawValue.labBased),
      enrolledCount: new FormControl(courseRawValue.enrolledCount, {
        validators: [Validators.min(0)],
      }),
      progress: new FormControl(courseRawValue.progress, {
        validators: [Validators.min(0), Validators.max(100)],
      }),
    });
  }

  getCourse(form: CourseFormGroup): ICourse | NewCourse {
    return form.getRawValue();
  }

  resetForm(form: CourseFormGroup, course: CourseFormGroupInput): void {
    const courseRawValue = { ...this.getFormDefaults(), ...course };
    form.reset({
      ...courseRawValue,
      id: { value: courseRawValue.id, disabled: true },
    });
  }

  private getFormDefaults(): CourseFormDefaults {
    return {
      id: null,
      labBased: false,
    };
  }
}
