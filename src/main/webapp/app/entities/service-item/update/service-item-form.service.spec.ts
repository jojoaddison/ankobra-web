import { beforeEach, describe, expect, it } from 'vitest';
import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../service-item.test-samples';

import { ServiceItemFormService } from './service-item-form.service';

describe('ServiceItem Form Service', () => {
  let service: ServiceItemFormService;

  beforeEach(() => {
    service = TestBed.inject(ServiceItemFormService);
  });

  describe('Service methods', () => {
    describe('createServiceItemFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createServiceItemFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            code: expect.any(Object),
            name: expect.any(Object),
            description: expect.any(Object),
            rate: expect.any(Object),
            unit: expect.any(Object),
            serviceGroup: expect.any(Object),
          }),
        );
      });

      it('passing IServiceItem should create a new form with FormGroup', () => {
        const formGroup = service.createServiceItemFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            code: expect.any(Object),
            name: expect.any(Object),
            description: expect.any(Object),
            rate: expect.any(Object),
            unit: expect.any(Object),
            serviceGroup: expect.any(Object),
          }),
        );
      });
    });

    describe('getServiceItem', () => {
      it('should return NewServiceItem for default ServiceItem initial value', () => {
        const formGroup = service.createServiceItemFormGroup(sampleWithNewData);

        const serviceItem = service.getServiceItem(formGroup);

        expect(serviceItem).toMatchObject(sampleWithNewData);
      });

      it('should return NewServiceItem for empty ServiceItem initial value', () => {
        const formGroup = service.createServiceItemFormGroup();

        const serviceItem = service.getServiceItem(formGroup);

        expect(serviceItem).toMatchObject({});
      });

      it('should return IServiceItem', () => {
        const formGroup = service.createServiceItemFormGroup(sampleWithRequiredData);

        const serviceItem = service.getServiceItem(formGroup);

        expect(serviceItem).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IServiceItem should not enable id FormControl', () => {
        const formGroup = service.createServiceItemFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewServiceItem should disable id FormControl', () => {
        const formGroup = service.createServiceItemFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
