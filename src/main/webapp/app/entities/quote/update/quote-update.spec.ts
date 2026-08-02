import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { HttpResponse } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';

import { TranslateModule } from '@ngx-translate/core';
import { Subject, from, of } from 'rxjs';

import { IClient } from 'app/entities/client/client.model';
import { ClientService } from 'app/entities/client/service/client.service';
import { IQuote } from '../quote.model';
import { QuoteService } from '../service/quote.service';

import { QuoteFormService } from './quote-form.service';
import { QuoteUpdate } from './quote-update';

describe('Quote Management Update Component', () => {
  let comp: QuoteUpdate;
  let fixture: ComponentFixture<QuoteUpdate>;
  let activatedRoute: ActivatedRoute;
  let quoteFormService: QuoteFormService;
  let quoteService: QuoteService;
  let clientService: ClientService;

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

    fixture = TestBed.createComponent(QuoteUpdate);
    activatedRoute = TestBed.inject(ActivatedRoute);
    quoteFormService = TestBed.inject(QuoteFormService);
    quoteService = TestBed.inject(QuoteService);
    clientService = TestBed.inject(ClientService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call Client query and add missing value', () => {
      const quote: IQuote = { id: 23928 };
      const client: IClient = { id: 26282 };
      quote.client = client;

      const clientCollection: IClient[] = [{ id: 26282 }];
      vitest.spyOn(clientService, 'query').mockReturnValue(of(new HttpResponse({ body: clientCollection })));
      const additionalClients = [client];
      const expectedCollection: IClient[] = [...additionalClients, ...clientCollection];
      vitest.spyOn(clientService, 'addClientToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ quote });
      comp.ngOnInit();

      expect(clientService.query).toHaveBeenCalled();
      expect(clientService.addClientToCollectionIfMissing).toHaveBeenCalledWith(
        clientCollection,
        ...additionalClients.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.clientsSharedCollection()).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const quote: IQuote = { id: 23928 };
      const client: IClient = { id: 26282 };
      quote.client = client;

      activatedRoute.data = of({ quote });
      comp.ngOnInit();

      expect(comp.clientsSharedCollection()).toContainEqual(client);
      expect(comp.quote).toEqual(quote);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<IQuote>();
      const quote = { id: 10209 };
      vitest.spyOn(quoteFormService, 'getQuote').mockReturnValue(quote);
      vitest.spyOn(quoteService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ quote });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(quote);
      saveSubject.complete();

      // THEN
      expect(quoteFormService.getQuote).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(quoteService.update).toHaveBeenCalledWith(expect.objectContaining(quote));
      expect(comp.isSaving()).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<IQuote>();
      const quote = { id: 10209 };
      vitest.spyOn(quoteFormService, 'getQuote').mockReturnValue({ id: null });
      vitest.spyOn(quoteService, 'create').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ quote: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(quote);
      saveSubject.complete();

      // THEN
      expect(quoteFormService.getQuote).toHaveBeenCalled();
      expect(quoteService.create).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<IQuote>();
      const quote = { id: 10209 };
      vitest.spyOn(quoteService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ quote });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(quoteService.update).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareClient', () => {
      it('should forward to clientService', () => {
        const entity = { id: 26282 };
        const entity2 = { id: 16836 };
        vitest.spyOn(clientService, 'compareClient');
        comp.compareClient(entity, entity2);
        expect(clientService.compareClient).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
