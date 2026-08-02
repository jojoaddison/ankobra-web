import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';
import { Observable, finalize } from 'rxjs';

import { CatalogueGroup } from 'app/entities/enumerations/catalogue-group.model';
import { RateUnit } from 'app/entities/enumerations/rate-unit.model';
import { AlertError } from 'app/shared/alert/alert-error';
import { TranslateDirective } from 'app/shared/language';
import { ServiceItemService } from '../service/service-item.service';
import { IServiceItem } from '../service-item.model';

import { ServiceItemFormGroup, ServiceItemFormService } from './service-item-form.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-service-item-update',
  templateUrl: './service-item-update.html',
  imports: [TranslateDirective, TranslateModule, FontAwesomeModule, AlertError, ReactiveFormsModule],
})
export class ServiceItemUpdate implements OnInit {
  readonly isSaving = signal(false);
  serviceItem: IServiceItem | null = null;
  rateUnitValues = Object.keys(RateUnit);
  catalogueGroupValues = Object.keys(CatalogueGroup);

  protected serviceItemService = inject(ServiceItemService);
  protected serviceItemFormService = inject(ServiceItemFormService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: ServiceItemFormGroup = this.serviceItemFormService.createServiceItemFormGroup();

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ serviceItem }) => {
      this.serviceItem = serviceItem;
      if (serviceItem) {
        this.updateForm(serviceItem);
      }
    });
  }

  previousState(): void {
    globalThis.history.back();
  }

  save(): void {
    this.isSaving.set(true);
    const serviceItem = this.serviceItemFormService.getServiceItem(this.editForm);
    if (serviceItem.id === null) {
      this.subscribeToSaveResponse(this.serviceItemService.create(serviceItem));
    } else {
      this.subscribeToSaveResponse(this.serviceItemService.update(serviceItem));
    }
  }

  protected subscribeToSaveResponse(result: Observable<IServiceItem | null>): void {
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

  protected updateForm(serviceItem: IServiceItem): void {
    this.serviceItem = serviceItem;
    this.serviceItemFormService.resetForm(this.editForm, serviceItem);
  }
}
