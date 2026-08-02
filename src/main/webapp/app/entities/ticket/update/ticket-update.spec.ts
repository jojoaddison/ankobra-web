import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { HttpResponse } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';

import { TranslateModule } from '@ngx-translate/core';
import { Subject, from, of } from 'rxjs';

import { IClient } from 'app/entities/client/client.model';
import { ClientService } from 'app/entities/client/service/client.service';
import { TeamMemberService } from 'app/entities/team-member/service/team-member.service';
import { ITeamMember } from 'app/entities/team-member/team-member.model';
import { TicketService } from '../service/ticket.service';
import { ITicket } from '../ticket.model';

import { TicketFormService } from './ticket-form.service';
import { TicketUpdate } from './ticket-update';

describe('Ticket Management Update Component', () => {
  let comp: TicketUpdate;
  let fixture: ComponentFixture<TicketUpdate>;
  let activatedRoute: ActivatedRoute;
  let ticketFormService: TicketFormService;
  let ticketService: TicketService;
  let teamMemberService: TeamMemberService;
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

    fixture = TestBed.createComponent(TicketUpdate);
    activatedRoute = TestBed.inject(ActivatedRoute);
    ticketFormService = TestBed.inject(TicketFormService);
    ticketService = TestBed.inject(TicketService);
    teamMemberService = TestBed.inject(TeamMemberService);
    clientService = TestBed.inject(ClientService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call TeamMember query and add missing value', () => {
      const ticket: ITicket = { id: 23717 };
      const owner: ITeamMember = { id: 11287 };
      ticket.owner = owner;

      const teamMemberCollection: ITeamMember[] = [{ id: 11287 }];
      vitest.spyOn(teamMemberService, 'query').mockReturnValue(of(new HttpResponse({ body: teamMemberCollection })));
      const additionalTeamMembers = [owner];
      const expectedCollection: ITeamMember[] = [...additionalTeamMembers, ...teamMemberCollection];
      vitest.spyOn(teamMemberService, 'addTeamMemberToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ ticket });
      comp.ngOnInit();

      expect(teamMemberService.query).toHaveBeenCalled();
      expect(teamMemberService.addTeamMemberToCollectionIfMissing).toHaveBeenCalledWith(
        teamMemberCollection,
        ...additionalTeamMembers.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.teamMembersSharedCollection()).toEqual(expectedCollection);
    });

    it('should call Client query and add missing value', () => {
      const ticket: ITicket = { id: 23717 };
      const client: IClient = { id: 26282 };
      ticket.client = client;

      const clientCollection: IClient[] = [{ id: 26282 }];
      vitest.spyOn(clientService, 'query').mockReturnValue(of(new HttpResponse({ body: clientCollection })));
      const additionalClients = [client];
      const expectedCollection: IClient[] = [...additionalClients, ...clientCollection];
      vitest.spyOn(clientService, 'addClientToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ ticket });
      comp.ngOnInit();

      expect(clientService.query).toHaveBeenCalled();
      expect(clientService.addClientToCollectionIfMissing).toHaveBeenCalledWith(
        clientCollection,
        ...additionalClients.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.clientsSharedCollection()).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const ticket: ITicket = { id: 23717 };
      const owner: ITeamMember = { id: 11287 };
      ticket.owner = owner;
      const client: IClient = { id: 26282 };
      ticket.client = client;

      activatedRoute.data = of({ ticket });
      comp.ngOnInit();

      expect(comp.teamMembersSharedCollection()).toContainEqual(owner);
      expect(comp.clientsSharedCollection()).toContainEqual(client);
      expect(comp.ticket).toEqual(ticket);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<ITicket>();
      const ticket = { id: 29380 };
      vitest.spyOn(ticketFormService, 'getTicket').mockReturnValue(ticket);
      vitest.spyOn(ticketService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ ticket });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(ticket);
      saveSubject.complete();

      // THEN
      expect(ticketFormService.getTicket).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(ticketService.update).toHaveBeenCalledWith(expect.objectContaining(ticket));
      expect(comp.isSaving()).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<ITicket>();
      const ticket = { id: 29380 };
      vitest.spyOn(ticketFormService, 'getTicket').mockReturnValue({ id: null });
      vitest.spyOn(ticketService, 'create').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ ticket: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(ticket);
      saveSubject.complete();

      // THEN
      expect(ticketFormService.getTicket).toHaveBeenCalled();
      expect(ticketService.create).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<ITicket>();
      const ticket = { id: 29380 };
      vitest.spyOn(ticketService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ ticket });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(ticketService.update).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareTeamMember', () => {
      it('should forward to teamMemberService', () => {
        const entity = { id: 11287 };
        const entity2 = { id: 22246 };
        vitest.spyOn(teamMemberService, 'compareTeamMember');
        comp.compareTeamMember(entity, entity2);
        expect(teamMemberService.compareTeamMember).toHaveBeenCalledWith(entity, entity2);
      });
    });

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
