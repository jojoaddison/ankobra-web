import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { IQuoteLine } from '../quote-line.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../quote-line.test-samples';

import { QuoteLineService } from './quote-line.service';

const requireRestSample: IQuoteLine = {
  ...sampleWithRequiredData,
};

describe('QuoteLine Service', () => {
  let service: QuoteLineService;
  let httpMock: HttpTestingController;
  let expectedResult: IQuoteLine | IQuoteLine[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(QuoteLineService);
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

    it('should create a QuoteLine', () => {
      const quoteLine = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(quoteLine).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a QuoteLine', () => {
      const quoteLine = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(quoteLine).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a QuoteLine', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of QuoteLine', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a QuoteLine', () => {
      service.delete(123).subscribe();

      const requests = httpMock.match({ method: 'DELETE' });
      expect(requests.length).toBe(1);
    });

    describe('addQuoteLineToCollectionIfMissing', () => {
      it('should add a QuoteLine to an empty array', () => {
        const quoteLine: IQuoteLine = sampleWithRequiredData;
        expectedResult = service.addQuoteLineToCollectionIfMissing([], quoteLine);
        expect(expectedResult).toEqual([quoteLine]);
      });

      it('should not add a QuoteLine to an array that contains it', () => {
        const quoteLine: IQuoteLine = sampleWithRequiredData;
        const quoteLineCollection: IQuoteLine[] = [
          {
            ...quoteLine,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addQuoteLineToCollectionIfMissing(quoteLineCollection, quoteLine);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a QuoteLine to an array that doesn't contain it", () => {
        const quoteLine: IQuoteLine = sampleWithRequiredData;
        const quoteLineCollection: IQuoteLine[] = [sampleWithPartialData];
        expectedResult = service.addQuoteLineToCollectionIfMissing(quoteLineCollection, quoteLine);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(quoteLine);
      });

      it('should add only unique QuoteLine to an array', () => {
        const quoteLineArray: IQuoteLine[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const quoteLineCollection: IQuoteLine[] = [sampleWithRequiredData];
        expectedResult = service.addQuoteLineToCollectionIfMissing(quoteLineCollection, ...quoteLineArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const quoteLine: IQuoteLine = sampleWithRequiredData;
        const quoteLine2: IQuoteLine = sampleWithPartialData;
        expectedResult = service.addQuoteLineToCollectionIfMissing([], quoteLine, quoteLine2);
        expect(expectedResult).toEqual([quoteLine, quoteLine2]);
      });

      it('should accept null and undefined values', () => {
        const quoteLine: IQuoteLine = sampleWithRequiredData;
        expectedResult = service.addQuoteLineToCollectionIfMissing([], null, quoteLine, undefined);
        expect(expectedResult).toEqual([quoteLine]);
      });

      it('should return initial array if no QuoteLine is added', () => {
        const quoteLineCollection: IQuoteLine[] = [sampleWithRequiredData];
        expectedResult = service.addQuoteLineToCollectionIfMissing(quoteLineCollection, undefined, null);
        expect(expectedResult).toEqual(quoteLineCollection);
      });
    });

    describe('compareQuoteLine', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareQuoteLine(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: 18028 };
        const entity2 = null;

        const compareResult1 = service.compareQuoteLine(entity1, entity2);
        const compareResult2 = service.compareQuoteLine(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: 18028 };
        const entity2 = { id: 3248 };

        const compareResult1 = service.compareQuoteLine(entity1, entity2);
        const compareResult2 = service.compareQuoteLine(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey matches', () => {
        const entity1 = { id: 18028 };
        const entity2 = { id: 18028 };

        const compareResult1 = service.compareQuoteLine(entity1, entity2);
        const compareResult2 = service.compareQuoteLine(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
