import { beforeEach, describe, expect, it } from 'vitest';
import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../project.test-samples';

import { ProjectFormService } from './project-form.service';

describe('Project Form Service', () => {
  let service: ProjectFormService;

  beforeEach(() => {
    service = TestBed.inject(ProjectFormService);
  });

  describe('Service methods', () => {
    describe('createProjectFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createProjectFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            reference: expect.any(Object),
            name: expect.any(Object),
            pillar: expect.any(Object),
            status: expect.any(Object),
            progress: expect.any(Object),
            dueDate: expect.any(Object),
            delivered: expect.any(Object),
            budget: expect.any(Object),
            spent: expect.any(Object),
            techStack: expect.any(Object),
            lead: expect.any(Object),
            client: expect.any(Object),
          }),
        );
      });

      it('passing IProject should create a new form with FormGroup', () => {
        const formGroup = service.createProjectFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            reference: expect.any(Object),
            name: expect.any(Object),
            pillar: expect.any(Object),
            status: expect.any(Object),
            progress: expect.any(Object),
            dueDate: expect.any(Object),
            delivered: expect.any(Object),
            budget: expect.any(Object),
            spent: expect.any(Object),
            techStack: expect.any(Object),
            lead: expect.any(Object),
            client: expect.any(Object),
          }),
        );
      });
    });

    describe('getProject', () => {
      it('should return NewProject for default Project initial value', () => {
        const formGroup = service.createProjectFormGroup(sampleWithNewData);

        const project = service.getProject(formGroup);

        expect(project).toMatchObject(sampleWithNewData);
      });

      it('should return NewProject for empty Project initial value', () => {
        const formGroup = service.createProjectFormGroup();

        const project = service.getProject(formGroup);

        expect(project).toMatchObject({});
      });

      it('should return IProject', () => {
        const formGroup = service.createProjectFormGroup(sampleWithRequiredData);

        const project = service.getProject(formGroup);

        expect(project).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IProject should not enable id FormControl', () => {
        const formGroup = service.createProjectFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewProject should disable id FormControl', () => {
        const formGroup = service.createProjectFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
