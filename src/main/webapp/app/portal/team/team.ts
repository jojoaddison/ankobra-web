import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';

import { ITeamMember } from 'app/entities/team-member/team-member.model';
import { TeamMemberService } from 'app/entities/team-member/service/team-member.service';

@Component({
  selector: 'jhi-portal-team',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './team.html',
  imports: [],
})
export default class Team implements OnInit {
  protected readonly members = signal<ITeamMember[]>([]);
  protected readonly loading = signal(true);

  private readonly teamMemberService = inject(TeamMemberService);

  ngOnInit(): void {
    this.teamMemberService.query({ size: 100, sort: ['name,asc'] }).subscribe({
      next: res => {
        this.members.set(res.body ?? []);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
