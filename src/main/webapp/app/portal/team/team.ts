import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';

import { ITeamMember } from 'app/entities/team-member/team-member.model';
import { TeamMemberService } from 'app/entities/team-member/service/team-member.service';
import Pager from '../shared/pager';

@Component({
  selector: 'jhi-portal-team',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './team.html',
  imports: [Pager],
})
export default class Team implements OnInit {
  protected readonly members = signal<ITeamMember[]>([]);
  protected readonly loading = signal(true);
  protected readonly page = signal(1);
  protected readonly total = signal(0);
  protected readonly pageSize = 8;

  private readonly teamMemberService = inject(TeamMemberService);

  ngOnInit(): void {
    this.load();
  }

  protected onPage(page: number): void {
    this.page.set(page);
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.teamMemberService.query({ page: this.page() - 1, size: this.pageSize, sort: ['name,asc'] }).subscribe({
      next: res => {
        this.members.set(res.body ?? []);
        this.total.set(Number(res.headers.get('X-Total-Count') ?? 0));
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
