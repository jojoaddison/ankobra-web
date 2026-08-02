import { Status } from 'app/entities/enumerations/status.model';

const STATUS_LABEL: Record<string, string> = {
  GOOD: 'On track',
  WARN: 'At risk',
  SERIOUS: 'Delayed',
  CRIT: 'Blocked',
  DONE: 'Delivered',
};

const PRIORITY_LABEL: Record<string, string> = {
  GOOD: 'Low',
  WARN: 'Medium',
  SERIOUS: 'High',
  CRIT: 'Critical',
  DONE: 'Resolved',
};

const STATUS_CLASS: Record<string, string> = {
  GOOD: 'st-good',
  WARN: 'st-warn',
  SERIOUS: 'st-serious',
  CRIT: 'st-crit',
  DONE: 'st-neutral',
};

/** Shared presentation helpers for the portal views, bound as `fmt` in templates. */
export const PortalFormat = {
  statusLabel(status?: keyof typeof Status | null): string {
    return status ? (STATUS_LABEL[status] ?? status) : '—';
  },

  priorityLabel(status?: keyof typeof Status | null): string {
    return status ? (PRIORITY_LABEL[status] ?? status) : '—';
  },

  statusClass(status?: keyof typeof Status | null): string {
    return status ? (STATUS_CLASS[status] ?? 'st-neutral') : 'st-neutral';
  },

  /** ENUM_VALUE -> "Enum value"; hyphenates *_HOUSE compounds sensibly enough for labels. */
  humanize(value?: string | null): string {
    if (!value) {
      return '—';
    }
    const words = value.toLowerCase().split('_');
    return words.map((w, i) => (i === 0 ? w.charAt(0).toUpperCase() + w.slice(1) : w)).join(' ');
  },

  money(amount?: number | null): string {
    if (amount == null) {
      return '—';
    }
    return `$${amount.toLocaleString('en-US')}`;
  },

  kmoney(amount?: number | null): string {
    if (amount == null) {
      return '—';
    }
    return amount >= 1_000_000 ? `$${(amount / 1_000_000).toFixed(2)}M` : `$${Math.round(amount / 1000)}k`;
  },
};
