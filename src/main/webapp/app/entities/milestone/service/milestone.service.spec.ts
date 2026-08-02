import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { IMilestone } from '../milestone.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../milestone.test-samples';

import { MilestoneService } from './milestone.service';

const requireRestSample: IMilestone = {
  ...sampleWithRequiredData,
};

describe('Milestone Service', () => {
  let service: MilestoneService;
  let httpMock: HttpTestingController;
  let expectedResult: IMilestone | IMilestone[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(MilestoneService);
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

    it('should create a Milestone', () => {
      const milestone = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(milestone).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a Milestone', () => {
      const milestone = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(milestone).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a Milestone', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of Milestone', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a Milestone', () => {
      service.delete(123).subscribe();

      const requests = httpMock.match({ method: 'DELETE' });
      expect(requests.length).toBe(1);
    });

    describe('addMilestoneToCollectionIfMissing', () => {
      it('should add a Milestone to an empty array', () => {
        const milestone: IMilestone = sampleWithRequiredData;
        expectedResult = service.addMilestoneToCollectionIfMissing([], milestone);
        expect(expectedResult).toEqual([milestone]);
      });

      it('should not add a Milestone to an array that contains it', () => {
        const milestone: IMilestone = sampleWithRequiredData;
        const milestoneCollection: IMilestone[] = [
          {
            ...milestone,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addMilestoneToCollectionIfMissing(milestoneCollection, milestone);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a Milestone to an array that doesn't contain it", () => {
        const milestone: IMilestone = sampleWithRequiredData;
        const milestoneCollection: IMilestone[] = [sampleWithPartialData];
        expectedResult = service.addMilestoneToCollectionIfMissing(milestoneCollection, milestone);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(milestone);
      });

      it('should add only unique Milestone to an array', () => {
        const milestoneArray: IMilestone[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const milestoneCollection: IMilestone[] = [sampleWithRequiredData];
        expectedResult = service.addMilestoneToCollectionIfMissing(milestoneCollection, ...milestoneArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const milestone: IMilestone = sampleWithRequiredData;
        const milestone2: IMilestone = sampleWithPartialData;
        expectedResult = service.addMilestoneToCollectionIfMissing([], milestone, milestone2);
        expect(expectedResult).toEqual([milestone, milestone2]);
      });

      it('should accept null and undefined values', () => {
        const milestone: IMilestone = sampleWithRequiredData;
        expectedResult = service.addMilestoneToCollectionIfMissing([], null, milestone, undefined);
        expect(expectedResult).toEqual([milestone]);
      });

      it('should return initial array if no Milestone is added', () => {
        const milestoneCollection: IMilestone[] = [sampleWithRequiredData];
        expectedResult = service.addMilestoneToCollectionIfMissing(milestoneCollection, undefined, null);
        expect(expectedResult).toEqual(milestoneCollection);
      });
    });

    describe('compareMilestone', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareMilestone(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: 27104 };
        const entity2 = null;

        const compareResult1 = service.compareMilestone(entity1, entity2);
        const compareResult2 = service.compareMilestone(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: 27104 };
        const entity2 = { id: 18822 };

        const compareResult1 = service.compareMilestone(entity1, entity2);
        const compareResult2 = service.compareMilestone(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey matches', () => {
        const entity1 = { id: 27104 };
        const entity2 = { id: 27104 };

        const compareResult1 = service.compareMilestone(entity1, entity2);
        const compareResult2 = service.compareMilestone(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
