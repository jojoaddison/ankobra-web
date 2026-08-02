import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';

import { TranslateModule } from '@ngx-translate/core';
import { Subject, from, of } from 'rxjs';

import { ILead } from '../lead.model';
import { LeadService } from '../service/lead.service';

import { LeadFormService } from './lead-form.service';
import { LeadUpdate } from './lead-update';

describe('Lead Management Update Component', () => {
  let comp: LeadUpdate;
  let fixture: ComponentFixture<LeadUpdate>;
  let activatedRoute: ActivatedRoute;
  let leadFormService: LeadFormService;
  let leadService: LeadService;

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

    fixture = TestBed.createComponent(LeadUpdate);
    activatedRoute = TestBed.inject(ActivatedRoute);
    leadFormService = TestBed.inject(LeadFormService);
    leadService = TestBed.inject(LeadService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should update editForm', () => {
      const lead: ILead = { id: 6619 };

      activatedRoute.data = of({ lead });
      comp.ngOnInit();

      expect(comp.lead).toEqual(lead);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<ILead>();
      const lead = { id: 32296 };
      vitest.spyOn(leadFormService, 'getLead').mockReturnValue(lead);
      vitest.spyOn(leadService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ lead });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(lead);
      saveSubject.complete();

      // THEN
      expect(leadFormService.getLead).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(leadService.update).toHaveBeenCalledWith(expect.objectContaining(lead));
      expect(comp.isSaving()).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<ILead>();
      const lead = { id: 32296 };
      vitest.spyOn(leadFormService, 'getLead').mockReturnValue({ id: null });
      vitest.spyOn(leadService, 'create').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ lead: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(lead);
      saveSubject.complete();

      // THEN
      expect(leadFormService.getLead).toHaveBeenCalled();
      expect(leadService.create).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<ILead>();
      const lead = { id: 32296 };
      vitest.spyOn(leadService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ lead });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(leadService.update).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });
});
