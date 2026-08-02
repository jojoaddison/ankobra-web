import { HttpResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';
import { Observable, finalize, map } from 'rxjs';

import { DataUtils, FileLoadError } from 'app/core/util/data-util.service';
import { EventManager, EventWithContent } from 'app/core/util/event-manager.service';
import { UserService } from 'app/entities/user/service/user.service';
import { IUser } from 'app/entities/user/user.model';
import { AlertError } from 'app/shared/alert/alert-error';
import { AlertErrorModel } from 'app/shared/alert/alert-error.model';
import { TranslateDirective } from 'app/shared/language';
import { TeamMemberService } from '../service/team-member.service';
import { ITeamMember } from '../team-member.model';

import { TeamMemberFormGroup, TeamMemberFormService } from './team-member-form.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-team-member-update',
  templateUrl: './team-member-update.html',
  imports: [TranslateDirective, TranslateModule, FontAwesomeModule, AlertError, ReactiveFormsModule],
})
export class TeamMemberUpdate implements OnInit {
  readonly isSaving = signal(false);
  teamMember: ITeamMember | null = null;

  usersSharedCollection = signal<IUser[]>([]);

  protected dataUtils = inject(DataUtils);
  protected eventManager = inject(EventManager);
  protected teamMemberService = inject(TeamMemberService);
  protected teamMemberFormService = inject(TeamMemberFormService);
  protected userService = inject(UserService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: TeamMemberFormGroup = this.teamMemberFormService.createTeamMemberFormGroup();

  compareUser = (o1: IUser | null, o2: IUser | null): boolean => this.userService.compareUser(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ teamMember }) => {
      this.teamMember = teamMember;
      if (teamMember) {
        this.updateForm(teamMember);
      }

      this.loadRelationshipsOptions();
    });
  }

  byteSize(base64String: string): string {
    return this.dataUtils.byteSize(base64String);
  }

  openFile(base64String: string, contentType: string | null | undefined): void {
    this.dataUtils.openFile(base64String, contentType);
  }

  setFileData(event: Event, field: string, isImage: boolean): void {
    this.dataUtils.loadFileToForm(event, this.editForm, field, isImage).subscribe({
      error: (err: FileLoadError) =>
        this.eventManager.broadcast(
          new EventWithContent<AlertErrorModel>('jojoaddisonApp.error', { ...err, key: `error.file.${err.key}` }),
        ),
    });
  }

  previousState(): void {
    globalThis.history.back();
  }

  save(): void {
    this.isSaving.set(true);
    const teamMember = this.teamMemberFormService.getTeamMember(this.editForm);
    if (teamMember.id === null) {
      this.subscribeToSaveResponse(this.teamMemberService.create(teamMember));
    } else {
      this.subscribeToSaveResponse(this.teamMemberService.update(teamMember));
    }
  }

  protected subscribeToSaveResponse(result: Observable<ITeamMember | null>): void {
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

  protected updateForm(teamMember: ITeamMember): void {
    this.teamMember = teamMember;
    this.teamMemberFormService.resetForm(this.editForm, teamMember);

    this.usersSharedCollection.update(users => this.userService.addUserToCollectionIfMissing<IUser>(users, teamMember.user));
  }

  protected loadRelationshipsOptions(): void {
    this.userService
      .query()
      .pipe(map((res: HttpResponse<IUser[]>) => res.body ?? []))
      .pipe(map((users: IUser[]) => this.userService.addUserToCollectionIfMissing<IUser>(users, this.teamMember?.user)))
      .subscribe((users: IUser[]) => this.usersSharedCollection.set(users));
  }
}
