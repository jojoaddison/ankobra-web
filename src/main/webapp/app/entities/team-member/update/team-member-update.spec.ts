import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { HttpResponse } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';

import { TranslateModule } from '@ngx-translate/core';
import { Subject, from, of } from 'rxjs';

import { UserService } from 'app/entities/user/service/user.service';
import { IUser } from 'app/entities/user/user.model';
import { TeamMemberService } from '../service/team-member.service';
import { ITeamMember } from '../team-member.model';

import { TeamMemberFormService } from './team-member-form.service';
import { TeamMemberUpdate } from './team-member-update';

describe('TeamMember Management Update Component', () => {
  let comp: TeamMemberUpdate;
  let fixture: ComponentFixture<TeamMemberUpdate>;
  let activatedRoute: ActivatedRoute;
  let teamMemberFormService: TeamMemberFormService;
  let teamMemberService: TeamMemberService;
  let userService: UserService;

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

    fixture = TestBed.createComponent(TeamMemberUpdate);
    activatedRoute = TestBed.inject(ActivatedRoute);
    teamMemberFormService = TestBed.inject(TeamMemberFormService);
    teamMemberService = TestBed.inject(TeamMemberService);
    userService = TestBed.inject(UserService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call User query and add missing value', () => {
      const teamMember: ITeamMember = { id: 22246 };
      const user: IUser = { id: 3944 };
      teamMember.user = user;

      const userCollection: IUser[] = [{ id: 3944 }];
      vitest.spyOn(userService, 'query').mockReturnValue(of(new HttpResponse({ body: userCollection })));
      const additionalUsers = [user];
      const expectedCollection: IUser[] = [...additionalUsers, ...userCollection];
      vitest.spyOn(userService, 'addUserToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ teamMember });
      comp.ngOnInit();

      expect(userService.query).toHaveBeenCalled();
      expect(userService.addUserToCollectionIfMissing).toHaveBeenCalledWith(
        userCollection,
        ...additionalUsers.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.usersSharedCollection()).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const teamMember: ITeamMember = { id: 22246 };
      const user: IUser = { id: 3944 };
      teamMember.user = user;

      activatedRoute.data = of({ teamMember });
      comp.ngOnInit();

      expect(comp.usersSharedCollection()).toContainEqual(user);
      expect(comp.teamMember).toEqual(teamMember);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<ITeamMember>();
      const teamMember = { id: 11287 };
      vitest.spyOn(teamMemberFormService, 'getTeamMember').mockReturnValue(teamMember);
      vitest.spyOn(teamMemberService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ teamMember });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(teamMember);
      saveSubject.complete();

      // THEN
      expect(teamMemberFormService.getTeamMember).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(teamMemberService.update).toHaveBeenCalledWith(expect.objectContaining(teamMember));
      expect(comp.isSaving()).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<ITeamMember>();
      const teamMember = { id: 11287 };
      vitest.spyOn(teamMemberFormService, 'getTeamMember').mockReturnValue({ id: null });
      vitest.spyOn(teamMemberService, 'create').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ teamMember: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(teamMember);
      saveSubject.complete();

      // THEN
      expect(teamMemberFormService.getTeamMember).toHaveBeenCalled();
      expect(teamMemberService.create).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<ITeamMember>();
      const teamMember = { id: 11287 };
      vitest.spyOn(teamMemberService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ teamMember });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(teamMemberService.update).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareUser', () => {
      it('should forward to userService', () => {
        const entity = { id: 3944 };
        const entity2 = { id: 6275 };
        vitest.spyOn(userService, 'compareUser');
        comp.compareUser(entity, entity2);
        expect(userService.compareUser).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
