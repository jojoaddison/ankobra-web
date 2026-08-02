import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';
import { Observable, finalize } from 'rxjs';

import { DataUtils, FileLoadError } from 'app/core/util/data-util.service';
import { EventManager, EventWithContent } from 'app/core/util/event-manager.service';
import { EnquiryType } from 'app/entities/enumerations/enquiry-type.model';
import { LeadStatus } from 'app/entities/enumerations/lead-status.model';
import { AlertError } from 'app/shared/alert/alert-error';
import { AlertErrorModel } from 'app/shared/alert/alert-error.model';
import { TranslateDirective } from 'app/shared/language';
import { ILead } from '../lead.model';
import { LeadService } from '../service/lead.service';

import { LeadFormGroup, LeadFormService } from './lead-form.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-lead-update',
  templateUrl: './lead-update.html',
  imports: [TranslateDirective, TranslateModule, FontAwesomeModule, AlertError, ReactiveFormsModule],
})
export class LeadUpdate implements OnInit {
  readonly isSaving = signal(false);
  lead: ILead | null = null;
  enquiryTypeValues = Object.keys(EnquiryType);
  leadStatusValues = Object.keys(LeadStatus);

  protected dataUtils = inject(DataUtils);
  protected eventManager = inject(EventManager);
  protected leadService = inject(LeadService);
  protected leadFormService = inject(LeadFormService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: LeadFormGroup = this.leadFormService.createLeadFormGroup();

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ lead }) => {
      this.lead = lead;
      if (lead) {
        this.updateForm(lead);
      }
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
    const lead = this.leadFormService.getLead(this.editForm);
    if (lead.id === null) {
      this.subscribeToSaveResponse(this.leadService.create(lead));
    } else {
      this.subscribeToSaveResponse(this.leadService.update(lead));
    }
  }

  protected subscribeToSaveResponse(result: Observable<ILead | null>): void {
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

  protected updateForm(lead: ILead): void {
    this.lead = lead;
    this.leadFormService.resetForm(this.editForm, lead);
  }
}
