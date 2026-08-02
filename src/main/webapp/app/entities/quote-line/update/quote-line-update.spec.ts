import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { HttpResponse } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';

import { TranslateModule } from '@ngx-translate/core';
import { Subject, from, of } from 'rxjs';

import { IQuote } from 'app/entities/quote/quote.model';
import { QuoteService } from 'app/entities/quote/service/quote.service';
import { ServiceItemService } from 'app/entities/service-item/service/service-item.service';
import { IServiceItem } from 'app/entities/service-item/service-item.model';
import { IQuoteLine } from '../quote-line.model';
import { QuoteLineService } from '../service/quote-line.service';

import { QuoteLineFormService } from './quote-line-form.service';
import { QuoteLineUpdate } from './quote-line-update';

describe('QuoteLine Management Update Component', () => {
  let comp: QuoteLineUpdate;
  let fixture: ComponentFixture<QuoteLineUpdate>;
  let activatedRoute: ActivatedRoute;
  let quoteLineFormService: QuoteLineFormService;
  let quoteLineService: QuoteLineService;
  let serviceItemService: ServiceItemService;
  let quoteService: QuoteService;

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

    fixture = TestBed.createComponent(QuoteLineUpdate);
    activatedRoute = TestBed.inject(ActivatedRoute);
    quoteLineFormService = TestBed.inject(QuoteLineFormService);
    quoteLineService = TestBed.inject(QuoteLineService);
    serviceItemService = TestBed.inject(ServiceItemService);
    quoteService = TestBed.inject(QuoteService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call ServiceItem query and add missing value', () => {
      const quoteLine: IQuoteLine = { id: 3248 };
      const item: IServiceItem = { id: 16652 };
      quoteLine.item = item;

      const serviceItemCollection: IServiceItem[] = [{ id: 16652 }];
      vitest.spyOn(serviceItemService, 'query').mockReturnValue(of(new HttpResponse({ body: serviceItemCollection })));
      const additionalServiceItems = [item];
      const expectedCollection: IServiceItem[] = [...additionalServiceItems, ...serviceItemCollection];
      vitest.spyOn(serviceItemService, 'addServiceItemToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ quoteLine });
      comp.ngOnInit();

      expect(serviceItemService.query).toHaveBeenCalled();
      expect(serviceItemService.addServiceItemToCollectionIfMissing).toHaveBeenCalledWith(
        serviceItemCollection,
        ...additionalServiceItems.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.serviceItemsSharedCollection()).toEqual(expectedCollection);
    });

    it('should call Quote query and add missing value', () => {
      const quoteLine: IQuoteLine = { id: 3248 };
      const quote: IQuote = { id: 10209 };
      quoteLine.quote = quote;

      const quoteCollection: IQuote[] = [{ id: 10209 }];
      vitest.spyOn(quoteService, 'query').mockReturnValue(of(new HttpResponse({ body: quoteCollection })));
      const additionalQuotes = [quote];
      const expectedCollection: IQuote[] = [...additionalQuotes, ...quoteCollection];
      vitest.spyOn(quoteService, 'addQuoteToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ quoteLine });
      comp.ngOnInit();

      expect(quoteService.query).toHaveBeenCalled();
      expect(quoteService.addQuoteToCollectionIfMissing).toHaveBeenCalledWith(
        quoteCollection,
        ...additionalQuotes.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.quotesSharedCollection()).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const quoteLine: IQuoteLine = { id: 3248 };
      const item: IServiceItem = { id: 16652 };
      quoteLine.item = item;
      const quote: IQuote = { id: 10209 };
      quoteLine.quote = quote;

      activatedRoute.data = of({ quoteLine });
      comp.ngOnInit();

      expect(comp.serviceItemsSharedCollection()).toContainEqual(item);
      expect(comp.quotesSharedCollection()).toContainEqual(quote);
      expect(comp.quoteLine).toEqual(quoteLine);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<IQuoteLine>();
      const quoteLine = { id: 18028 };
      vitest.spyOn(quoteLineFormService, 'getQuoteLine').mockReturnValue(quoteLine);
      vitest.spyOn(quoteLineService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ quoteLine });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(quoteLine);
      saveSubject.complete();

      // THEN
      expect(quoteLineFormService.getQuoteLine).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(quoteLineService.update).toHaveBeenCalledWith(expect.objectContaining(quoteLine));
      expect(comp.isSaving()).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<IQuoteLine>();
      const quoteLine = { id: 18028 };
      vitest.spyOn(quoteLineFormService, 'getQuoteLine').mockReturnValue({ id: null });
      vitest.spyOn(quoteLineService, 'create').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ quoteLine: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(quoteLine);
      saveSubject.complete();

      // THEN
      expect(quoteLineFormService.getQuoteLine).toHaveBeenCalled();
      expect(quoteLineService.create).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<IQuoteLine>();
      const quoteLine = { id: 18028 };
      vitest.spyOn(quoteLineService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ quoteLine });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(quoteLineService.update).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareServiceItem', () => {
      it('should forward to serviceItemService', () => {
        const entity = { id: 16652 };
        const entity2 = { id: 12516 };
        vitest.spyOn(serviceItemService, 'compareServiceItem');
        comp.compareServiceItem(entity, entity2);
        expect(serviceItemService.compareServiceItem).toHaveBeenCalledWith(entity, entity2);
      });
    });

    describe('compareQuote', () => {
      it('should forward to quoteService', () => {
        const entity = { id: 10209 };
        const entity2 = { id: 23928 };
        vitest.spyOn(quoteService, 'compareQuote');
        comp.compareQuote(entity, entity2);
        expect(quoteService.compareQuote).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
