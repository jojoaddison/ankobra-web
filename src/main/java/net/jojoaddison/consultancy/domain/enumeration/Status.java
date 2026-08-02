package net.jojoaddison.consultancy.domain.enumeration;

/**
 * Shared status vocabulary. Rendered On track / At risk / Delayed / Blocked / Delivered
 * for projects & clients, and reused as ticket priority Low / Medium / High / Critical.
 */
public enum Status {
    GOOD,
    WARN,
    SERIOUS,
    CRIT,
    DONE,
}
