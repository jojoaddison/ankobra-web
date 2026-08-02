import { HttpResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';
import { Observable, finalize, map } from 'rxjs';

import { MilestoneState } from 'app/entities/enumerations/milestone-state.model';
import { IProject } from 'app/entities/project/project.model';
import { ProjectService } from 'app/entities/project/service/project.service';
import { AlertError } from 'app/shared/alert/alert-error';
import { TranslateDirective } from 'app/shared/language';
import { IMilestone } from '../milestone.model';
import { MilestoneService } from '../service/milestone.service';

import { MilestoneFormGroup, MilestoneFormService } from './milestone-form.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-milestone-update',
  templateUrl: './milestone-update.html',
  imports: [TranslateDirective, TranslateModule, FontAwesomeModule, AlertError, ReactiveFormsModule],
})
export class MilestoneUpdate implements OnInit {
  readonly isSaving = signal(false);
  milestone: IMilestone | null = null;
  milestoneStateValues = Object.keys(MilestoneState);

  projectsSharedCollection = signal<IProject[]>([]);

  protected milestoneService = inject(MilestoneService);
  protected milestoneFormService = inject(MilestoneFormService);
  protected projectService = inject(ProjectService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: MilestoneFormGroup = this.milestoneFormService.createMilestoneFormGroup();

  compareProject = (o1: IProject | null, o2: IProject | null): boolean => this.projectService.compareProject(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ milestone }) => {
      this.milestone = milestone;
      if (milestone) {
        this.updateForm(milestone);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    globalThis.history.back();
  }

  save(): void {
    this.isSaving.set(true);
    const milestone = this.milestoneFormService.getMilestone(this.editForm);
    if (milestone.id === null) {
      this.subscribeToSaveResponse(this.milestoneService.create(milestone));
    } else {
      this.subscribeToSaveResponse(this.milestoneService.update(milestone));
    }
  }

  protected subscribeToSaveResponse(result: Observable<IMilestone | null>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving.set(false);
  }

  protected updateForm(milestone: IMilestone): void {
    this.milestone = milestone;
    this.milestoneFormService.resetForm(this.editForm, milestone);

    this.projectsSharedCollection.update(projects =>
      this.projectService.addProjectToCollectionIfMissing<IProject>(projects, milestone.project),
    );
  }

  protected loadRelationshipsOptions(): void {
    this.projectService
      .query()
      .pipe(map((res: HttpResponse<IProject[]>) => res.body ?? []))
      .pipe(map((projects: IProject[]) => this.projectService.addProjectToCollectionIfMissing<IProject>(projects, this.milestone?.project)))
      .subscribe((projects: IProject[]) => this.projectsSharedCollection.set(projects));
  }
}
