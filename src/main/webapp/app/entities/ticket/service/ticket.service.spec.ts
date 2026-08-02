import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { ITicket } from '../ticket.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../ticket.test-samples';

import { RestTicket, TicketService } from './ticket.service';

const requireRestSample: RestTicket = {
  ...sampleWithRequiredData,
  openedAt: sampleWithRequiredData.openedAt?.toJSON(),
};

describe('Ticket Service', () => {
  let service: TicketService;
  let httpMock: HttpTestingController;
  let expectedResult: ITicket | ITicket[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(TicketService);
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

    it('should create a Ticket', () => {
      const ticket = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(ticket).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a Ticket', () => {
      const ticket = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(ticket).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a Ticket', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of Ticket', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a Ticket', () => {
      service.delete(123).subscribe();

      const requests = httpMock.match({ method: 'DELETE' });
      expect(requests.length).toBe(1);
    });

    describe('addTicketToCollectionIfMissing', () => {
      it('should add a Ticket to an empty array', () => {
        const ticket: ITicket = sampleWithRequiredData;
        expectedResult = service.addTicketToCollectionIfMissing([], ticket);
        expect(expectedResult).toEqual([ticket]);
      });

      it('should not add a Ticket to an array that contains it', () => {
        const ticket: ITicket = sampleWithRequiredData;
        const ticketCollection: ITicket[] = [
          {
            ...ticket,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addTicketToCollectionIfMissing(ticketCollection, ticket);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a Ticket to an array that doesn't contain it", () => {
        const ticket: ITicket = sampleWithRequiredData;
        const ticketCollection: ITicket[] = [sampleWithPartialData];
        expectedResult = service.addTicketToCollectionIfMissing(ticketCollection, ticket);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(ticket);
      });

      it('should add only unique Ticket to an array', () => {
        const ticketArray: ITicket[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const ticketCollection: ITicket[] = [sampleWithRequiredData];
        expectedResult = service.addTicketToCollectionIfMissing(ticketCollection, ...ticketArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const ticket: ITicket = sampleWithRequiredData;
        const ticket2: ITicket = sampleWithPartialData;
        expectedResult = service.addTicketToCollectionIfMissing([], ticket, ticket2);
        expect(expectedResult).toEqual([ticket, ticket2]);
      });

      it('should accept null and undefined values', () => {
        const ticket: ITicket = sampleWithRequiredData;
        expectedResult = service.addTicketToCollectionIfMissing([], null, ticket, undefined);
        expect(expectedResult).toEqual([ticket]);
      });

      it('should return initial array if no Ticket is added', () => {
        const ticketCollection: ITicket[] = [sampleWithRequiredData];
        expectedResult = service.addTicketToCollectionIfMissing(ticketCollection, undefined, null);
        expect(expectedResult).toEqual(ticketCollection);
      });
    });

    describe('compareTicket', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareTicket(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: 29380 };
        const entity2 = null;

        const compareResult1 = service.compareTicket(entity1, entity2);
        const compareResult2 = service.compareTicket(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: 29380 };
        const entity2 = { id: 23717 };

        const compareResult1 = service.compareTicket(entity1, entity2);
        const compareResult2 = service.compareTicket(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey matches', () => {
        const entity1 = { id: 29380 };
        const entity2 = { id: 29380 };

        const compareResult1 = service.compareTicket(entity1, entity2);
        const compareResult2 = service.compareTicket(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
