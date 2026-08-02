import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { IQuote } from '../quote.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../quote.test-samples';

import { QuoteService, RestQuote } from './quote.service';

const requireRestSample: RestQuote = {
  ...sampleWithRequiredData,
  createdDate: sampleWithRequiredData.createdDate?.toJSON(),
};

describe('Quote Service', () => {
  let service: QuoteService;
  let httpMock: HttpTestingController;
  let expectedResult: IQuote | IQuote[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(QuoteService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  describe('Service methods', () => {
    it('should find an element', () => {
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.find(123).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should create a Quote', () => {
      const quote = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(quote).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a Quote', () => {
      const quote = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(quote).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a Quote', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of Quote', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a Quote', () => {
      service.delete(123).subscribe();

      const requests = httpMock.match({ method: 'DELETE' });
      expect(requests.length).toBe(1);
    });

    describe('addQuoteToCollectionIfMissing', () => {
      it('should add a Quote to an empty array', () => {
        const quote: IQuote = sampleWithRequiredData;
        expectedResult = service.addQuoteToCollectionIfMissing([], quote);
        expect(expectedResult).toEqual([quote]);
      });

      it('should not add a Quote to an array that contains it', () => {
        const quote: IQuote = sampleWithRequiredData;
        const quoteCollection: IQuote[] = [
          {
            ...quote,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addQuoteToCollectionIfMissing(quoteCollection, quote);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a Quote to an array that doesn't contain it", () => {
        const quote: IQuote = sampleWithRequiredData;
        const quoteCollection: IQuote[] = [sampleWithPartialData];
        expectedResult = service.addQuoteToCollectionIfMissing(quoteCollection, quote);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(quote);
      });

      it('should add only unique Quote to an array', () => {
        const quoteArray: IQuote[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const quoteCollection: IQuote[] = [sampleWithRequiredData];
        expectedResult = service.addQuoteToCollectionIfMissing(quoteCollection, ...quoteArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const quote: IQuote = sampleWithRequiredData;
        const quote2: IQuote = sampleWithPartialData;
        expectedResult = service.addQuoteToCollectionIfMissing([], quote, quote2);
        expect(expectedResult).toEqual([quote, quote2]);
      });

      it('should accept null and undefined values', () => {
        const quote: IQuote = sampleWithRequiredData;
        expectedResult = service.addQuoteToCollectionIfMissing([], null, quote, undefined);
        expect(expectedResult).toEqual([quote]);
      });

      it('should return initial array if no Quote is added', () => {
        const quoteCollection: IQuote[] = [sampleWithRequiredData];
        expectedResult = service.addQuoteToCollectionIfMissing(quoteCollection, undefined, null);
        expect(expectedResult).toEqual(quoteCollection);
      });
    });

    describe('compareQuote', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareQuote(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: 10209 };
        const entity2 = null;

        const compareResult1 = service.compareQuote(entity1, entity2);
        const compareResult2 = service.compareQuote(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: 10209 };
        const entity2 = { id: 23928 };

        const compareResult1 = service.compareQuote(entity1, entity2);
        const compareResult2 = service.compareQuote(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey matches', () => {
        const entity1 = { id: 10209 };
        const entity2 = { id: 10209 };

        const compareResult1 = service.compareQuote(entity1, entity2);
        const compareResult2 = service.compareQuote(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
