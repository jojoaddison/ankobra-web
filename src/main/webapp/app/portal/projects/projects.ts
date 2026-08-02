import { LowerCasePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { forkJoin } from 'rxjs';

import { IProject } from 'app/entities/project/project.model';
import { ProjectService } from 'app/entities/project/service/project.service';
import { IMilestone } from 'app/entities/milestone/milestone.model';
import { MilestoneService } from 'app/entities/milestone/service/milestone.service';
import { PortalFormat } from '../shared/portal-format';

@Component({
  selector: 'jhi-portal-projects',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './projects.html',
  styleUrl: './projects.scss',
  imports: [LowerCasePipe],
})
export default class Projects implements OnInit {
  protected readonly fmt = PortalFormat;
  protected readonly loading = signal(true);
  protected readonly projects = signal<IProject[]>([]);

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
    forkJoin({
      projects: this.projectService.query({ size: 100, sort: ['reference,asc'] }),
      milestones: this.milestoneService.query({ size: 500, sort: ['position,asc'] }),
    }).subscribe({
      next: ({ projects, milestones }) => {
        this.projects.set(projects.body ?? []);
        this.milestones.set(milestones.body ?? []);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
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
}
