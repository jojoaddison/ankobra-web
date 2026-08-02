import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';

import { TranslateModule } from '@ngx-translate/core';
import { Subject, from, of } from 'rxjs';

import { ServiceItemService } from '../service/service-item.service';
import { IServiceItem } from '../service-item.model';

import { ServiceItemFormService } from './service-item-form.service';
import { ServiceItemUpdate } from './service-item-update';

describe('ServiceItem Management Update Component', () => {
  let comp: ServiceItemUpdate;
  let fixture: ComponentFixture<ServiceItemUpdate>;
  let activatedRoute: ActivatedRoute;
  let serviceItemFormService: ServiceItemFormService;
  let serviceItemService: ServiceItemService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [TranslateModule.forRoot()],
      providers: [
        provideHttpClientTesting(),
        {
          provide: ActivatedRoute,
          useValue: {
            params: from([{}]),
          },
        },
      ],
    });

    fixture = TestBed.createComponent(ServiceItemUpdate);
    activatedRoute = TestBed.inject(ActivatedRoute);
    serviceItemFormService = TestBed.inject(ServiceItemFormService);
    serviceItemService = TestBed.inject(ServiceItemService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should update editForm', () => {
      const serviceItem: IServiceItem = { id: 12516 };

      activatedRoute.data = of({ serviceItem });
      comp.ngOnInit();

      expect(comp.serviceItem).toEqual(serviceItem);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<IServiceItem>();
      const serviceItem = { id: 16652 };
      vitest.spyOn(serviceItemFormService, 'getServiceItem').mockReturnValue(serviceItem);
      vitest.spyOn(serviceItemService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ serviceItem });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(serviceItem);
      saveSubject.complete();

      // THEN
      expect(serviceItemFormService.getServiceItem).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(serviceItemService.update).toHaveBeenCalledWith(expect.objectContaining(serviceItem));
      expect(comp.isSaving()).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<IServiceItem>();
      const serviceItem = { id: 16652 };
      vitest.spyOn(serviceItemFormService, 'getServiceItem').mockReturnValue({ id: null });
      vitest.spyOn(serviceItemService, 'create').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ serviceItem: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(serviceItem);
      saveSubject.complete();

      // THEN
      expect(serviceItemFormService.getServiceItem).toHaveBeenCalled();
      expect(serviceItemService.create).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<IServiceItem>();
      const serviceItem = { id: 16652 };
      vitest.spyOn(serviceItemService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ serviceItem });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(serviceItemService.update).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });
});
