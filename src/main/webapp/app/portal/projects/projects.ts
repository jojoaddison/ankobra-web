import { LowerCasePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';

import { IProject } from 'app/entities/project/project.model';
import { ProjectService } from 'app/entities/project/service/project.service';
import { IMilestone } from 'app/entities/milestone/milestone.model';
import { MilestoneService } from 'app/entities/milestone/service/milestone.service';
import { PortalFormat } from '../shared/portal-format';
import Pager from '../shared/pager';

@Component({
  selector: 'jhi-portal-projects',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './projects.html',
  styleUrl: './projects.scss',
  imports: [LowerCasePipe, Pager],
})
export default class Projects implements OnInit {
  protected readonly fmt = PortalFormat;
  protected readonly loading = signal(true);
  protected readonly projects = signal<IProject[]>([]);
  protected readonly page = signal(1);
  protected readonly total = signal(0);
  protected readonly pageSize = 6;

  protected readonly milestonesByProject = computed(() => {
    const map = new Map<number, IMilestone[]>();
    for (const m of this.milestones()) {
      const pid = m.project?.id;
      if (pid == null) {
        continue;
      }
      const list = map.get(pid) ?? [];
      list.push(m);
      map.set(pid, list);
    }
    for (const list of map.values()) {
      list.sort((a, b) => (a.position ?? 0) - (b.position ?? 0));
    }
    return map;
  });

  private readonly milestones = signal<IMilestone[]>([]);
  private readonly projectService = inject(ProjectService);
  private readonly milestoneService = inject(MilestoneService);

  ngOnInit(): void {
    // Milestones are few; fetch them all once, then paginate the projects.
    this.milestoneService.query({ size: 500, sort: ['position,asc'] }).subscribe(res => this.milestones.set(res.body ?? []));
    this.load();
  }

  protected onPage(page: number): void {
    this.page.set(page);
    this.load();
  }

  protected milestonesFor(project: IProject): IMilestone[] {
    return this.milestonesByProject().get(project.id) ?? [];
  }

  protected dueLabel(project: IProject): string {
    if (!project.dueDate) {
      return '—';
    }
    const date = project.dueDate.format('DD MMM YYYY');
    return project.delivered ? `Delivered ${date}` : `Due ${date}`;
  }

  protected stackTags(project: IProject): string[] {
    return (project.techStack ?? '')
      .split(',')
      .map(s => s.trim())
      .filter(Boolean);
  }

  private load(): void {
    this.loading.set(true);
    this.projectService.query({ page: this.page() - 1, size: this.pageSize, sort: ['reference,asc'] }).subscribe({
      next: res => {
        this.projects.set(res.body ?? []);
        this.total.set(Number(res.headers.get('X-Total-Count') ?? 0));
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
