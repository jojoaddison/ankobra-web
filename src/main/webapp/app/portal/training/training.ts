import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';

import { ICourse } from 'app/entities/course/course.model';
import { CourseService } from 'app/entities/course/service/course.service';
import { PortalFormat } from '../shared/portal-format';
import Pager from '../shared/pager';

@Component({
  selector: 'jhi-portal-training',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './training.html',
  imports: [Pager],
})
export default class Training implements OnInit {
  protected readonly fmt = PortalFormat;
  protected readonly courses = signal<ICourse[]>([]);
  protected readonly loading = signal(true);
  protected readonly page = signal(1);
  protected readonly total = signal(0);
  protected readonly pageSize = 8;

  private readonly courseService = inject(CourseService);

  ngOnInit(): void {
    this.load();
  }

  protected onPage(page: number): void {
    this.page.set(page);
    this.load();
  }

  protected meta(course: ICourse): string {
    const parts = [`${course.moduleCount ?? 0} modules`, this.fmt.humanize(course.mode)];
    if (course.labBased) {
      parts.push('lab based');
    }
    return parts.join(' · ');
  }

  private load(): void {
    this.loading.set(true);
    this.courseService.query({ page: this.page() - 1, size: this.pageSize, sort: ['name,asc'] }).subscribe({
      next: res => {
        this.courses.set(res.body ?? []);
        this.total.set(Number(res.headers.get('X-Total-Count') ?? 0));
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
