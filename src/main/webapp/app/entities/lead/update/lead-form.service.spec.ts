import { beforeEach, describe, expect, it } from 'vitest';
import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../lead.test-samples';

import { LeadFormService } from './lead-form.service';

describe('Lead Form Service', () => {
  let service: LeadFormService;

  beforeEach(() => {
    service = TestBed.inject(LeadFormService);
  });

  describe('Service methods', () => {
    describe('createLeadFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createLeadFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            name: expect.any(Object),
            email: expect.any(Object),
            need: expect.any(Object),
            message: expect.any(Object),
            createdDate: expect.any(Object),
            status: expect.any(Object),
          }),
        );
      });

      it('passing ILead should create a new form with FormGroup', () => {
        const formGroup = service.createLeadFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            name: expect.any(Object),
            email: expect.any(Object),
            need: expect.any(Object),
            message: expect.any(Object),
            createdDate: expect.any(Object),
            status: expect.any(Object),
          }),
        );
      });
    });

    describe('getLead', () => {
      it('should return NewLead for default Lead initial value', () => {
        const formGroup = service.createLeadFormGroup(sampleWithNewData);

        const lead = service.getLead(formGroup);

        expect(lead).toMatchObject(sampleWithNewData);
      });

      it('should return NewLead for empty Lead initial value', () => {
        const formGroup = service.createLeadFormGroup();

        const lead = service.getLead(formGroup);

        expect(lead).toMatchObject({});
      });

      it('should return ILead', () => {
        const formGroup = service.createLeadFormGroup(sampleWithRequiredData);

        const lead = service.getLead(formGroup);

        expect(lead).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing ILead should not enable id FormControl', () => {
        const formGroup = service.createLeadFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewLead should disable id FormControl', () => {
        const formGroup = service.createLeadFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
