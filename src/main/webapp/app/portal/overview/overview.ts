import { ChangeDetectionStrategy, Component } from '@angular/core';

interface Kpi {
  label: string;
  value: string;
  delta: string;
  tone: 'good' | 'warn' | 'muted';
}
interface Bar {
  x: number;
  y: number;
  w: number;
  h: number;
  fill: string;
}
interface Point {
  x: number;
  y: number;
}
interface RevenueRow {
  name: string;
  value: string;
  w: number;
}

const CHART_W = 640;
const CHART_H = 240;
const PAD = { top: 16, right: 16, bottom: 28, left: 40 };

@Component({
  selector: 'jhi-overview',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './overview.html',
  styleUrl: './overview.scss',
})
export default class Overview {
  protected readonly chartW = CHART_W;
  protected readonly chartH = CHART_H;

  protected readonly kpis: Kpi[] = [
    { label: 'Active projects', value: '7', delta: '+2 this quarter', tone: 'good' },
    { label: 'Open tickets', value: '5', delta: '2 within SLA risk', tone: 'warn' },
    { label: 'Revenue booked', value: '$1.24M', delta: '+18% YoY', tone: 'good' },
    { label: 'Active clients', value: '8', delta: 'across 6 sectors', tone: 'muted' },
  ];

  // Delivery hours by month × two service pillars (from the demo HOURS fixture).
  protected readonly months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug'];
  protected readonly hours: number[][] = [
    [420, 260],
    [468, 305],
    [512, 288],
    [496, 350],
    [604, 402],
    [588, 455],
    [651, 470],
    [612, 521],
  ];

  // Tickets raised vs resolved, last 12 weeks (from the demo TREND fixture).
  protected readonly raised = [14, 17, 12, 19, 23, 18, 21, 26, 22, 19, 24, 20];
  protected readonly resolved = [12, 16, 14, 17, 20, 19, 18, 24, 25, 21, 22, 23];

  protected readonly revenue = [
    { name: 'Bespoke solutions', value: 524000 },
    { name: 'Enterprise integration', value: 340000 },
    { name: 'Digital transformation', value: 196000 },
    { name: 'Consultancy', value: 118000 },
    { name: 'Training', value: 64000 },
  ];

  protected readonly hoursLegend = [
    { label: 'Build & integration', color: 'var(--series-1)' },
    { label: 'Advisory & training', color: 'var(--series-3)' },
  ];
  protected readonly trendLegend = [
    { label: 'Raised', color: 'var(--series-2)' },
    { label: 'Resolved', color: 'var(--series-3)' },
  ];

  protected readonly hoursBars: Bar[] = this.buildGroupedBars();
  protected readonly hoursLabels = this.months.map((m, i) => ({
    label: m,
    x: PAD.left + (i + 0.5) * this.bandWidth(this.months.length),
  }));

  protected readonly raisedLine = this.buildLine(this.raised);
  protected readonly resolvedLine = this.buildLine(this.resolved);
  protected readonly trendPoints = { raised: this.buildPoints(this.raised), resolved: this.buildPoints(this.resolved) };

  protected readonly revenueRows: RevenueRow[] = this.buildRevenue();

  private bandWidth(n: number): number {
    return (CHART_W - PAD.left - PAD.right) / n;
  }

  private buildGroupedBars(): Bar[] {
    const max = Math.max(...this.hours.flat()) * 1.1;
    const plotH = CHART_H - PAD.top - PAD.bottom;
    const band = this.bandWidth(this.hours.length);
    const barW = (band * 0.62) / 2;
    const colors = ['var(--series-1)', 'var(--series-3)'];
    const bars: Bar[] = [];
    this.hours.forEach((group, i) => {
      group.forEach((v, s) => {
        const h = (v / max) * plotH;
        const groupX = PAD.left + i * band + band * 0.19;
        bars.push({ x: groupX + s * barW, y: PAD.top + (plotH - h), w: barW - 2, h, fill: colors[s] });
      });
    });
    return bars;
  }

  private buildPoints(series: number[]): Point[] {
    const all = [...this.raised, ...this.resolved];
    const max = Math.max(...all) * 1.15;
    const plotH = CHART_H - PAD.top - PAD.bottom;
    const step = (CHART_W - PAD.left - PAD.right) / (series.length - 1);
    return series.map((v, i) => ({ x: PAD.left + i * step, y: PAD.top + (plotH - (v / max) * plotH) }));
  }

  private buildLine(series: number[]): string {
    return this.buildPoints(series)
      .map((p, i) => `${i === 0 ? 'M' : 'L'}${p.x.toFixed(1)},${p.y.toFixed(1)}`)
      .join(' ');
  }

  private buildRevenue(): RevenueRow[] {
    const max = Math.max(...this.revenue.map(r => r.value));
    return this.revenue.map(r => ({
      name: r.name,
      value: r.value >= 1_000_000 ? `$${(r.value / 1_000_000).toFixed(2)}M` : `$${Math.round(r.value / 1000)}k`,
      w: Math.round((r.value / max) * 100),
    }));
  }
}
