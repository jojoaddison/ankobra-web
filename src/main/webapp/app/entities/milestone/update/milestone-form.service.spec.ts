import { beforeEach, describe, expect, it } from 'vitest';
import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../milestone.test-samples';

import { MilestoneFormService } from './milestone-form.service';

describe('Milestone Form Service', () => {
  let service: MilestoneFormService;

  beforeEach(() => {
    service = TestBed.inject(MilestoneFormService);
  });

  describe('Service methods', () => {
    describe('createMilestoneFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createMilestoneFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            title: expect.any(Object),
            state: expect.any(Object),
            position: expect.any(Object),
            project: expect.any(Object),
          }),
        );
      });

      it('passing IMilestone should create a new form with FormGroup', () => {
        const formGroup = service.createMilestoneFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            title: expect.any(Object),
            state: expect.any(Object),
            position: expect.any(Object),
            project: expect.any(Object),
          }),
        );
      });
    });

    describe('getMilestone', () => {
      it('should return NewMilestone for default Milestone initial value', () => {
        const formGroup = service.createMilestoneFormGroup(sampleWithNewData);

        const milestone = service.getMilestone(formGroup);

        expect(milestone).toMatchObject(sampleWithNewData);
      });

      it('should return NewMilestone for empty Milestone initial value', () => {
        const formGroup = service.createMilestoneFormGroup();

        const milestone = service.getMilestone(formGroup);

        expect(milestone).toMatchObject({});
      });

      it('should return IMilestone', () => {
        const formGroup = service.createMilestoneFormGroup(sampleWithRequiredData);

        const milestone = service.getMilestone(formGroup);

        expect(milestone).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IMilestone should not enable id FormControl', () => {
        const formGroup = service.createMilestoneFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewMilestone should disable id FormControl', () => {
        const formGroup = service.createMilestoneFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
