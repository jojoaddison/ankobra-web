import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { ILead } from '../lead.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../lead.test-samples';

import { LeadService, RestLead } from './lead.service';

const requireRestSample: RestLead = {
  ...sampleWithRequiredData,
  createdDate: sampleWithRequiredData.createdDate?.toJSON(),
};

describe('Lead Service', () => {
  let service: LeadService;
  let httpMock: HttpTestingController;
  let expectedResult: ILead | ILead[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(LeadService);
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

    it('should create a Lead', () => {
      const lead = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(lead).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a Lead', () => {
      const lead = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(lead).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a Lead', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of Lead', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a Lead', () => {
      service.delete(123).subscribe();

      const requests = httpMock.match({ method: 'DELETE' });
      expect(requests.length).toBe(1);
    });

    describe('addLeadToCollectionIfMissing', () => {
      it('should add a Lead to an empty array', () => {
        const lead: ILead = sampleWithRequiredData;
        expectedResult = service.addLeadToCollectionIfMissing([], lead);
        expect(expectedResult).toEqual([lead]);
      });

      it('should not add a Lead to an array that contains it', () => {
        const lead: ILead = sampleWithRequiredData;
        const leadCollection: ILead[] = [
          {
            ...lead,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addLeadToCollectionIfMissing(leadCollection, lead);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a Lead to an array that doesn't contain it", () => {
        const lead: ILead = sampleWithRequiredData;
        const leadCollection: ILead[] = [sampleWithPartialData];
        expectedResult = service.addLeadToCollectionIfMissing(leadCollection, lead);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(lead);
      });

      it('should add only unique Lead to an array', () => {
        const leadArray: ILead[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const leadCollection: ILead[] = [sampleWithRequiredData];
        expectedResult = service.addLeadToCollectionIfMissing(leadCollection, ...leadArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const lead: ILead = sampleWithRequiredData;
        const lead2: ILead = sampleWithPartialData;
        expectedResult = service.addLeadToCollectionIfMissing([], lead, lead2);
        expect(expectedResult).toEqual([lead, lead2]);
      });

      it('should accept null and undefined values', () => {
        const lead: ILead = sampleWithRequiredData;
        expectedResult = service.addLeadToCollectionIfMissing([], null, lead, undefined);
        expect(expectedResult).toEqual([lead]);
      });

      it('should return initial array if no Lead is added', () => {
        const leadCollection: ILead[] = [sampleWithRequiredData];
        expectedResult = service.addLeadToCollectionIfMissing(leadCollection, undefined, null);
        expect(expectedResult).toEqual(leadCollection);
      });
    });

    describe('compareLead', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareLead(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: 32296 };
        const entity2 = null;

        const compareResult1 = service.compareLead(entity1, entity2);
        const compareResult2 = service.compareLead(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: 32296 };
        const entity2 = { id: 6619 };

        const compareResult1 = service.compareLead(entity1, entity2);
        const compareResult2 = service.compareLead(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey matches', () => {
        const entity1 = { id: 32296 };
        const entity2 = { id: 32296 };

        const compareResult1 = service.compareLead(entity1, entity2);
        const compareResult2 = service.compareLead(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
