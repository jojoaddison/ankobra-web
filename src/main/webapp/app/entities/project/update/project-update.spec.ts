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
import { IProject } from '../project.model';
import { ProjectService } from '../service/project.service';

import { ProjectFormService } from './project-form.service';
import { ProjectUpdate } from './project-update';

describe('Project Management Update Component', () => {
  let comp: ProjectUpdate;
  let fixture: ComponentFixture<ProjectUpdate>;
  let activatedRoute: ActivatedRoute;
  let projectFormService: ProjectFormService;
  let projectService: ProjectService;
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

    fixture = TestBed.createComponent(ProjectUpdate);
    activatedRoute = TestBed.inject(ActivatedRoute);
    projectFormService = TestBed.inject(ProjectFormService);
    projectService = TestBed.inject(ProjectService);
    teamMemberService = TestBed.inject(TeamMemberService);
    clientService = TestBed.inject(ClientService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call TeamMember query and add missing value', () => {
      const project: IProject = { id: 3319 };
      const lead: ITeamMember = { id: 11287 };
      project.lead = lead;

      const teamMemberCollection: ITeamMember[] = [{ id: 11287 }];
      vitest.spyOn(teamMemberService, 'query').mockReturnValue(of(new HttpResponse({ body: teamMemberCollection })));
      const additionalTeamMembers = [lead];
      const expectedCollection: ITeamMember[] = [...additionalTeamMembers, ...teamMemberCollection];
      vitest.spyOn(teamMemberService, 'addTeamMemberToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ project });
      comp.ngOnInit();

      expect(teamMemberService.query).toHaveBeenCalled();
      expect(teamMemberService.addTeamMemberToCollectionIfMissing).toHaveBeenCalledWith(
        teamMemberCollection,
        ...additionalTeamMembers.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.teamMembersSharedCollection()).toEqual(expectedCollection);
    });

    it('should call Client query and add missing value', () => {
      const project: IProject = { id: 3319 };
      const client: IClient = { id: 26282 };
      project.client = client;

      const clientCollection: IClient[] = [{ id: 26282 }];
      vitest.spyOn(clientService, 'query').mockReturnValue(of(new HttpResponse({ body: clientCollection })));
      const additionalClients = [client];
      const expectedCollection: IClient[] = [...additionalClients, ...clientCollection];
      vitest.spyOn(clientService, 'addClientToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ project });
      comp.ngOnInit();

      expect(clientService.query).toHaveBeenCalled();
      expect(clientService.addClientToCollectionIfMissing).toHaveBeenCalledWith(
        clientCollection,
        ...additionalClients.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.clientsSharedCollection()).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const project: IProject = { id: 3319 };
      const lead: ITeamMember = { id: 11287 };
      project.lead = lead;
      const client: IClient = { id: 26282 };
      project.client = client;

      activatedRoute.data = of({ project });
      comp.ngOnInit();

      expect(comp.teamMembersSharedCollection()).toContainEqual(lead);
      expect(comp.clientsSharedCollection()).toContainEqual(client);
      expect(comp.project).toEqual(project);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<IProject>();
      const project = { id: 10300 };
      vitest.spyOn(projectFormService, 'getProject').mockReturnValue(project);
      vitest.spyOn(projectService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ project });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(project);
      saveSubject.complete();

      // THEN
      expect(projectFormService.getProject).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(projectService.update).toHaveBeenCalledWith(expect.objectContaining(project));
      expect(comp.isSaving()).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<IProject>();
      const project = { id: 10300 };
      vitest.spyOn(projectFormService, 'getProject').mockReturnValue({ id: null });
      vitest.spyOn(projectService, 'create').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ project: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(project);
      saveSubject.complete();

      // THEN
      expect(projectFormService.getProject).toHaveBeenCalled();
      expect(projectService.create).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<IProject>();
      const project = { id: 10300 };
      vitest.spyOn(projectService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ project });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(projectService.update).toHaveBeenCalled();
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
