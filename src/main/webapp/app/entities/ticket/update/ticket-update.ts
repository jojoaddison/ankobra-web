import { HttpResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';
import { Observable, finalize, map } from 'rxjs';

import { IClient } from 'app/entities/client/client.model';
import { ClientService } from 'app/entities/client/service/client.service';
import { Status } from 'app/entities/enumerations/status.model';
import { TicketState } from 'app/entities/enumerations/ticket-state.model';
import { TeamMemberService } from 'app/entities/team-member/service/team-member.service';
import { ITeamMember } from 'app/entities/team-member/team-member.model';
import { AlertError } from 'app/shared/alert/alert-error';
import { TranslateDirective } from 'app/shared/language';
import { TicketService } from '../service/ticket.service';
import { ITicket } from '../ticket.model';

import { TicketFormGroup, TicketFormService } from './ticket-form.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-ticket-update',
  templateUrl: './ticket-update.html',
  imports: [TranslateDirective, TranslateModule, FontAwesomeModule, AlertError, ReactiveFormsModule],
})
export class TicketUpdate implements OnInit {
  readonly isSaving = signal(false);
  ticket: ITicket | null = null;
  statusValues = Object.keys(Status);
  ticketStateValues = Object.keys(TicketState);

  teamMembersSharedCollection = signal<ITeamMember[]>([]);
  clientsSharedCollection = signal<IClient[]>([]);

  protected ticketService = inject(TicketService);
  protected ticketFormService = inject(TicketFormService);
  protected teamMemberService = inject(TeamMemberService);
  protected clientService = inject(ClientService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: TicketFormGroup = this.ticketFormService.createTicketFormGroup();

  compareTeamMember = (o1: ITeamMember | null, o2: ITeamMember | null): boolean => this.teamMemberService.compareTeamMember(o1, o2);

  compareClient = (o1: IClient | null, o2: IClient | null): boolean => this.clientService.compareClient(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ ticket }) => {
      this.ticket = ticket;
      if (ticket) {
        this.updateForm(ticket);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    globalThis.history.back();
  }

  save(): void {
    this.isSaving.set(true);
    const ticket = this.ticketFormService.getTicket(this.editForm);
    if (ticket.id === null) {
      this.subscribeToSaveResponse(this.ticketService.create(ticket));
    } else {
      this.subscribeToSaveResponse(this.ticketService.update(ticket));
    }
  }

  protected subscribeToSaveResponse(result: Observable<ITicket | null>): void {
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

  protected updateForm(ticket: ITicket): void {
    this.ticket = ticket;
    this.ticketFormService.resetForm(this.editForm, ticket);

    this.teamMembersSharedCollection.update(teamMembers =>
      this.teamMemberService.addTeamMemberToCollectionIfMissing<ITeamMember>(teamMembers, ticket.owner),
    );
    this.clientsSharedCollection.update(clients => this.clientService.addClientToCollectionIfMissing<IClient>(clients, ticket.client));
  }

  protected loadRelationshipsOptions(): void {
    this.teamMemberService
      .query()
      .pipe(map((res: HttpResponse<ITeamMember[]>) => res.body ?? []))
      .pipe(
        map((teamMembers: ITeamMember[]) =>
          this.teamMemberService.addTeamMemberToCollectionIfMissing<ITeamMember>(teamMembers, this.ticket?.owner),
        ),
      )
      .subscribe((teamMembers: ITeamMember[]) => this.teamMembersSharedCollection.set(teamMembers));

    this.clientService
      .query()
      .pipe(map((res: HttpResponse<IClient[]>) => res.body ?? []))
      .pipe(map((clients: IClient[]) => this.clientService.addClientToCollectionIfMissing<IClient>(clients, this.ticket?.client)))
      .subscribe((clients: IClient[]) => this.clientsSharedCollection.set(clients));
  }
}
