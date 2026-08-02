package net.jojoaddison.consultancy.service.criteria;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import net.jojoaddison.consultancy.domain.enumeration.Status;
import net.jojoaddison.consultancy.domain.enumeration.TicketState;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link net.jojoaddison.consultancy.domain.Ticket} entity. This class is used
 * in {@link net.jojoaddison.consultancy.web.rest.TicketResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /tickets?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class TicketCriteria implements Serializable, Criteria {

    /**
     * Class for filtering Status
     */
    public static class StatusFilter extends Filter<Status> {

        public StatusFilter() {}

        public StatusFilter(StatusFilter filter) {
            super(filter);
        }

        @Override
        public StatusFilter copy() {
            return new StatusFilter(this);
        }
    }

    /**
     * Class for filtering TicketState
     */
    public static class TicketStateFilter extends Filter<TicketState> {

        public TicketStateFilter() {}

        public TicketStateFilter(TicketStateFilter filter) {
            super(filter);
        }

        @Override
        public TicketStateFilter copy() {
            return new TicketStateFilter(this);
        }
    }

    @Serial
    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter reference;

    private StringFilter subject;

    private StatusFilter priority;

    private InstantFilter openedAt;

    private IntegerFilter slaHours;

    private TicketStateFilter state;

    private LongFilter ownerId;

    private LongFilter clientId;

    private Boolean distinct;

    public TicketCriteria() {}

    public TicketCriteria(TicketCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.reference = other.optionalReference().map(StringFilter::copy).orElse(null);
        this.subject = other.optionalSubject().map(StringFilter::copy).orElse(null);
        this.priority = other.optionalPriority().map(StatusFilter::copy).orElse(null);
        this.openedAt = other.optionalOpenedAt().map(InstantFilter::copy).orElse(null);
        this.slaHours = other.optionalSlaHours().map(IntegerFilter::copy).orElse(null);
        this.state = other.optionalState().map(TicketStateFilter::copy).orElse(null);
        this.ownerId = other.optionalOwnerId().map(LongFilter::copy).orElse(null);
        this.clientId = other.optionalClientId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public TicketCriteria copy() {
        return new TicketCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public Optional<LongFilter> optionalId() {
        return Optional.ofNullable(id);
    }

    public LongFilter id() {
        if (id == null) {
            setId(new LongFilter());
        }
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getReference() {
        return reference;
    }

    public Optional<StringFilter> optionalReference() {
        return Optional.ofNullable(reference);
    }

    public StringFilter reference() {
        if (reference == null) {
            setReference(new StringFilter());
        }
        return reference;
    }

    public void setReference(StringFilter reference) {
        this.reference = reference;
    }

    public StringFilter getSubject() {
        return subject;
    }

    public Optional<StringFilter> optionalSubject() {
        return Optional.ofNullable(subject);
    }

    public StringFilter subject() {
        if (subject == null) {
            setSubject(new StringFilter());
        }
        return subject;
    }

    public void setSubject(StringFilter subject) {
        this.subject = subject;
    }

    public StatusFilter getPriority() {
        return priority;
    }

    public Optional<StatusFilter> optionalPriority() {
        return Optional.ofNullable(priority);
    }

    public StatusFilter priority() {
        if (priority == null) {
            setPriority(new StatusFilter());
        }
        return priority;
    }

    public void setPriority(StatusFilter priority) {
        this.priority = priority;
    }

    public InstantFilter getOpenedAt() {
        return openedAt;
    }

    public Optional<InstantFilter> optionalOpenedAt() {
        return Optional.ofNullable(openedAt);
    }

    public InstantFilter openedAt() {
        if (openedAt == null) {
            setOpenedAt(new InstantFilter());
        }
        return openedAt;
    }

    public void setOpenedAt(InstantFilter openedAt) {
        this.openedAt = openedAt;
    }

    public IntegerFilter getSlaHours() {
        return slaHours;
    }

    public Optional<IntegerFilter> optionalSlaHours() {
        return Optional.ofNullable(slaHours);
    }

    public IntegerFilter slaHours() {
        if (slaHours == null) {
            setSlaHours(new IntegerFilter());
        }
        return slaHours;
    }

    public void setSlaHours(IntegerFilter slaHours) {
        this.slaHours = slaHours;
    }

    public TicketStateFilter getState() {
        return state;
    }

    public Optional<TicketStateFilter> optionalState() {
        return Optional.ofNullable(state);
    }

    public TicketStateFilter state() {
        if (state == null) {
            setState(new TicketStateFilter());
        }
        return state;
    }

    public void setState(TicketStateFilter state) {
        this.state = state;
    }

    public LongFilter getOwnerId() {
        return ownerId;
    }

    public Optional<LongFilter> optionalOwnerId() {
        return Optional.ofNullable(ownerId);
    }

    public LongFilter ownerId() {
        if (ownerId == null) {
            setOwnerId(new LongFilter());
        }
        return ownerId;
    }

    public void setOwnerId(LongFilter ownerId) {
        this.ownerId = ownerId;
    }

    public LongFilter getClientId() {
        return clientId;
    }

    public Optional<LongFilter> optionalClientId() {
        return Optional.ofNullable(clientId);
    }

    public LongFilter clientId() {
        if (clientId == null) {
            setClientId(new LongFilter());
        }
        return clientId;
    }

    public void setClientId(LongFilter clientId) {
        this.clientId = clientId;
    }

    public Boolean getDistinct() {
        return distinct;
    }

    public Optional<Boolean> optionalDistinct() {
        return Optional.ofNullable(distinct);
    }

    public Boolean distinct() {
        if (distinct == null) {
            setDistinct(true);
        }
        return distinct;
    }

    public void setDistinct(Boolean distinct) {
        this.distinct = distinct;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final TicketCriteria that = (TicketCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(reference, that.reference) &&
            Objects.equals(subject, that.subject) &&
            Objects.equals(priority, that.priority) &&
            Objects.equals(openedAt, that.openedAt) &&
            Objects.equals(slaHours, that.slaHours) &&
            Objects.equals(state, that.state) &&
            Objects.equals(ownerId, that.ownerId) &&
            Objects.equals(clientId, that.clientId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, reference, subject, priority, openedAt, slaHours, state, ownerId, clientId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "TicketCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalReference().map(f -> "reference=" + f + ", ").orElse("") +
            optionalSubject().map(f -> "subject=" + f + ", ").orElse("") +
            optionalPriority().map(f -> "priority=" + f + ", ").orElse("") +
            optionalOpenedAt().map(f -> "openedAt=" + f + ", ").orElse("") +
            optionalSlaHours().map(f -> "slaHours=" + f + ", ").orElse("") +
            optionalState().map(f -> "state=" + f + ", ").orElse("") +
            optionalOwnerId().map(f -> "ownerId=" + f + ", ").orElse("") +
            optionalClientId().map(f -> "clientId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
