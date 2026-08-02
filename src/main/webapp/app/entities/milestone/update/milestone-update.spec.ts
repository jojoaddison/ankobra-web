import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { HttpResponse } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';

import { TranslateModule } from '@ngx-translate/core';
import { Subject, from, of } from 'rxjs';

import { IProject } from 'app/entities/project/project.model';
import { ProjectService } from 'app/entities/project/service/project.service';
import { IMilestone } from '../milestone.model';
import { MilestoneService } from '../service/milestone.service';

import { MilestoneFormService } from './milestone-form.service';
import { MilestoneUpdate } from './milestone-update';

describe('Milestone Management Update Component', () => {
  let comp: MilestoneUpdate;
  let fixture: ComponentFixture<MilestoneUpdate>;
  let activatedRoute: ActivatedRoute;
  let milestoneFormService: MilestoneFormService;
  let milestoneService: MilestoneService;
  let projectService: ProjectService;

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

    fixture = TestBed.createComponent(MilestoneUpdate);
    activatedRoute = TestBed.inject(ActivatedRoute);
    milestoneFormService = TestBed.inject(MilestoneFormService);
    milestoneService = TestBed.inject(MilestoneService);
    projectService = TestBed.inject(ProjectService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call Project query and add missing value', () => {
      const milestone: IMilestone = { id: 18822 };
      const project: IProject = { id: 10300 };
      milestone.project = project;

      const projectCollection: IProject[] = [{ id: 10300 }];
      vitest.spyOn(projectService, 'query').mockReturnValue(of(new HttpResponse({ body: projectCollection })));
      const additionalProjects = [project];
      const expectedCollection: IProject[] = [...additionalProjects, ...projectCollection];
      vitest.spyOn(projectService, 'addProjectToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ milestone });
      comp.ngOnInit();

      expect(projectService.query).toHaveBeenCalled();
      expect(projectService.addProjectToCollectionIfMissing).toHaveBeenCalledWith(
        projectCollection,
        ...additionalProjects.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.projectsSharedCollection()).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const milestone: IMilestone = { id: 18822 };
      const project: IProject = { id: 10300 };
      milestone.project = project;

      activatedRoute.data = of({ milestone });
      comp.ngOnInit();

      expect(comp.projectsSharedCollection()).toContainEqual(project);
      expect(comp.milestone).toEqual(milestone);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<IMilestone>();
      const milestone = { id: 27104 };
      vitest.spyOn(milestoneFormService, 'getMilestone').mockReturnValue(milestone);
      vitest.spyOn(milestoneService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ milestone });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(milestone);
      saveSubject.complete();

      // THEN
      expect(milestoneFormService.getMilestone).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(milestoneService.update).toHaveBeenCalledWith(expect.objectContaining(milestone));
      expect(comp.isSaving()).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<IMilestone>();
      const milestone = { id: 27104 };
      vitest.spyOn(milestoneFormService, 'getMilestone').mockReturnValue({ id: null });
      vitest.spyOn(milestoneService, 'create').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ milestone: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(milestone);
      saveSubject.complete();

      // THEN
      expect(milestoneFormService.getMilestone).toHaveBeenCalled();
      expect(milestoneService.create).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<IMilestone>();
      const milestone = { id: 27104 };
      vitest.spyOn(milestoneService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ milestone });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(milestoneService.update).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareProject', () => {
      it('should forward to projectService', () => {
        const entity = { id: 10300 };
        const entity2 = { id: 3319 };
        vitest.spyOn(projectService, 'compareProject');
        comp.compareProject(entity, entity2);
        expect(projectService.compareProject).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
