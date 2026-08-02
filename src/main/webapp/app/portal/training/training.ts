import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';

import { ICourse } from 'app/entities/course/course.model';
import { CourseService } from 'app/entities/course/service/course.service';
import { PortalFormat } from '../shared/portal-format';

@Component({
  selector: 'jhi-portal-training',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './training.html',
  imports: [],
})
export default class Training implements OnInit {
  protected readonly fmt = PortalFormat;
  protected readonly courses = signal<ICourse[]>([]);
  protected readonly loading = signal(true);

  private readonly courseService = inject(CourseService);

  ngOnInit(): void {
    this.courseService.query({ size: 100, sort: ['name,asc'] }).subscribe({
      next: res => {
        this.courses.set(res.body ?? []);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  protected meta(course: ICourse): string {
    const parts = [`${course.moduleCount ?? 0} modules`, this.fmt.humanize(course.mode)];
    if (course.labBased) {
      parts.push('lab based');
    }
    return parts.join(' · ');
  }
}
