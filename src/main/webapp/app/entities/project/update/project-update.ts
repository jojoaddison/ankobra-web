import { HttpResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { NgbInputDatepicker } from '@ng-bootstrap/ng-bootstrap/datepicker';
import { TranslateModule } from '@ngx-translate/core';
import { Observable, finalize, map } from 'rxjs';

import { IClient } from 'app/entities/client/client.model';
import { ClientService } from 'app/entities/client/service/client.service';
import { ServicePillar } from 'app/entities/enumerations/service-pillar.model';
import { Status } from 'app/entities/enumerations/status.model';
import { TeamMemberService } from 'app/entities/team-member/service/team-member.service';
import { ITeamMember } from 'app/entities/team-member/team-member.model';
import { AlertError } from 'app/shared/alert/alert-error';
import { TranslateDirective } from 'app/shared/language';
import { IProject } from '../project.model';
import { ProjectService } from '../service/project.service';

import { ProjectFormGroup, ProjectFormService } from './project-form.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-project-update',
  templateUrl: './project-update.html',
  imports: [TranslateDirective, TranslateModule, FontAwesomeModule, AlertError, ReactiveFormsModule, NgbInputDatepicker],
})
export class ProjectUpdate implements OnInit {
  readonly isSaving = signal(false);
  project: IProject | null = null;
  servicePillarValues = Object.keys(ServicePillar);
  statusValues = Object.keys(Status);

  teamMembersSharedCollection = signal<ITeamMember[]>([]);
  clientsSharedCollection = signal<IClient[]>([]);

  protected projectService = inject(ProjectService);
  protected projectFormService = inject(ProjectFormService);
  protected teamMemberService = inject(TeamMemberService);
  protected clientService = inject(ClientService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: ProjectFormGroup = this.projectFormService.createProjectFormGroup();

  compareTeamMember = (o1: ITeamMember | null, o2: ITeamMember | null): boolean => this.teamMemberService.compareTeamMember(o1, o2);

  compareClient = (o1: IClient | null, o2: IClient | null): boolean => this.clientService.compareClient(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ project }) => {
      this.project = project;
      if (project) {
        this.updateForm(project);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    globalThis.history.back();
  }

  save(): void {
    this.isSaving.set(true);
    const project = this.projectFormService.getProject(this.editForm);
    if (project.id === null) {
      this.subscribeToSaveResponse(this.projectService.create(project));
    } else {
      this.subscribeToSaveResponse(this.projectService.update(project));
    }
  }

  protected subscribeToSaveResponse(result: Observable<IProject | null>): void {
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

  protected updateForm(project: IProject): void {
    this.project = project;
    this.projectFormService.resetForm(this.editForm, project);

    this.teamMembersSharedCollection.update(teamMembers =>
      this.teamMemberService.addTeamMemberToCollectionIfMissing<ITeamMember>(teamMembers, project.lead),
    );
    this.clientsSharedCollection.update(clients => this.clientService.addClientToCollectionIfMissing<IClient>(clients, project.client));
  }

  protected loadRelationshipsOptions(): void {
    this.teamMemberService
      .query()
      .pipe(map((res: HttpResponse<ITeamMember[]>) => res.body ?? []))
      .pipe(
        map((teamMembers: ITeamMember[]) =>
          this.teamMemberService.addTeamMemberToCollectionIfMissing<ITeamMember>(teamMembers, this.project?.lead),
        ),
      )
      .subscribe((teamMembers: ITeamMember[]) => this.teamMembersSharedCollection.set(teamMembers));

    this.clientService
      .query()
      .pipe(map((res: HttpResponse<IClient[]>) => res.body ?? []))
      .pipe(map((clients: IClient[]) => this.clientService.addClientToCollectionIfMissing<IClient>(clients, this.project?.client)))
      .subscribe((clients: IClient[]) => this.clientsSharedCollection.set(clients));
  }
}
