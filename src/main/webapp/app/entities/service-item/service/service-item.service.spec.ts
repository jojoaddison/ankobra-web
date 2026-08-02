import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { IServiceItem } from '../service-item.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../service-item.test-samples';

import { ServiceItemService } from './service-item.service';

const requireRestSample: IServiceItem = {
  ...sampleWithRequiredData,
};

describe('ServiceItem Service', () => {
  let service: ServiceItemService;
  let httpMock: HttpTestingController;
  let expectedResult: IServiceItem | IServiceItem[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(ServiceItemService);
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

    it('should create a ServiceItem', () => {
      const serviceItem = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(serviceItem).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a ServiceItem', () => {
      const serviceItem = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(serviceItem).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a ServiceItem', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of ServiceItem', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a ServiceItem', () => {
      service.delete(123).subscribe();

      const requests = httpMock.match({ method: 'DELETE' });
      expect(requests.length).toBe(1);
    });

    describe('addServiceItemToCollectionIfMissing', () => {
      it('should add a ServiceItem to an empty array', () => {
        const serviceItem: IServiceItem = sampleWithRequiredData;
        expectedResult = service.addServiceItemToCollectionIfMissing([], serviceItem);
        expect(expectedResult).toEqual([serviceItem]);
      });

      it('should not add a ServiceItem to an array that contains it', () => {
        const serviceItem: IServiceItem = sampleWithRequiredData;
        const serviceItemCollection: IServiceItem[] = [
          {
            ...serviceItem,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addServiceItemToCollectionIfMissing(serviceItemCollection, serviceItem);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a ServiceItem to an array that doesn't contain it", () => {
        const serviceItem: IServiceItem = sampleWithRequiredData;
        const serviceItemCollection: IServiceItem[] = [sampleWithPartialData];
        expectedResult = service.addServiceItemToCollectionIfMissing(serviceItemCollection, serviceItem);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(serviceItem);
      });

      it('should add only unique ServiceItem to an array', () => {
        const serviceItemArray: IServiceItem[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const serviceItemCollection: IServiceItem[] = [sampleWithRequiredData];
        expectedResult = service.addServiceItemToCollectionIfMissing(serviceItemCollection, ...serviceItemArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const serviceItem: IServiceItem = sampleWithRequiredData;
        const serviceItem2: IServiceItem = sampleWithPartialData;
        expectedResult = service.addServiceItemToCollectionIfMissing([], serviceItem, serviceItem2);
        expect(expectedResult).toEqual([serviceItem, serviceItem2]);
      });

      it('should accept null and undefined values', () => {
        const serviceItem: IServiceItem = sampleWithRequiredData;
        expectedResult = service.addServiceItemToCollectionIfMissing([], null, serviceItem, undefined);
        expect(expectedResult).toEqual([serviceItem]);
      });

      it('should return initial array if no ServiceItem is added', () => {
        const serviceItemCollection: IServiceItem[] = [sampleWithRequiredData];
        expectedResult = service.addServiceItemToCollectionIfMissing(serviceItemCollection, undefined, null);
        expect(expectedResult).toEqual(serviceItemCollection);
      });
    });

    describe('compareServiceItem', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareServiceItem(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: 16652 };
        const entity2 = null;

        const compareResult1 = service.compareServiceItem(entity1, entity2);
        const compareResult2 = service.compareServiceItem(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: 16652 };
        const entity2 = { id: 12516 };

        const compareResult1 = service.compareServiceItem(entity1, entity2);
        const compareResult2 = service.compareServiceItem(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey matches', () => {
        const entity1 = { id: 16652 };
        const entity2 = { id: 16652 };

        const compareResult1 = service.compareServiceItem(entity1, entity2);
        const compareResult2 = service.compareServiceItem(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
