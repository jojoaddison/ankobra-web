import { HttpResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';
import { Observable, finalize, map } from 'rxjs';

import { Market } from 'app/entities/enumerations/market.model';
import { Status } from 'app/entities/enumerations/status.model';
import { UserService } from 'app/entities/user/service/user.service';
import { IUser } from 'app/entities/user/user.model';
import { AlertError } from 'app/shared/alert/alert-error';
import { TranslateDirective } from 'app/shared/language';
import { IClient } from '../client.model';
import { ClientService } from '../service/client.service';

import { ClientFormGroup, ClientFormService } from './client-form.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-client-update',
  templateUrl: './client-update.html',
  imports: [TranslateDirective, TranslateModule, FontAwesomeModule, AlertError, ReactiveFormsModule],
})
export class ClientUpdate implements OnInit {
  readonly isSaving = signal(false);
  client: IClient | null = null;
  marketValues = Object.keys(Market);
  statusValues = Object.keys(Status);

  usersSharedCollection = signal<IUser[]>([]);

  protected clientService = inject(ClientService);
  protected clientFormService = inject(ClientFormService);
  protected userService = inject(UserService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: ClientFormGroup = this.clientFormService.createClientFormGroup();

  compareUser = (o1: IUser | null, o2: IUser | null): boolean => this.userService.compareUser(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ client }) => {
      this.client = client;
      if (client) {
        this.updateForm(client);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    globalThis.history.back();
  }

  save(): void {
    this.isSaving.set(true);
    const client = this.clientFormService.getClient(this.editForm);
    if (client.id === null) {
      this.subscribeToSaveResponse(this.clientService.create(client));
    } else {
      this.subscribeToSaveResponse(this.clientService.update(client));
    }
  }

  protected subscribeToSaveResponse(result: Observable<IClient | null>): void {
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

  protected updateForm(client: IClient): void {
    this.client = client;
    this.clientFormService.resetForm(this.editForm, client);

    this.usersSharedCollection.update(users => this.userService.addUserToCollectionIfMissing<IUser>(users, client.user));
  }

  protected loadRelationshipsOptions(): void {
    this.userService
      .query()
      .pipe(map((res: HttpResponse<IUser[]>) => res.body ?? []))
      .pipe(map((users: IUser[]) => this.userService.addUserToCollectionIfMissing<IUser>(users, this.client?.user)))
      .subscribe((users: IUser[]) => this.usersSharedCollection.set(users));
  }
}
