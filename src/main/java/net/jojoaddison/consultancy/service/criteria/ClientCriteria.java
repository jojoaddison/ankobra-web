package net.jojoaddison.consultancy.service.criteria;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import net.jojoaddison.consultancy.domain.enumeration.Market;
import net.jojoaddison.consultancy.domain.enumeration.Status;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link net.jojoaddison.consultancy.domain.Client} entity. This class is used
 * in {@link net.jojoaddison.consultancy.web.rest.ClientResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /clients?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ClientCriteria implements Serializable, Criteria {

    /**
     * Class for filtering Market
     */
    public static class MarketFilter extends Filter<Market> {

        public MarketFilter() {}

        public MarketFilter(MarketFilter filter) {
            super(filter);
        }

        @Override
        public MarketFilter copy() {
            return new MarketFilter(this);
        }
    }

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

    @Serial
    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter name;

    private MarketFilter sector;

    private IntegerFilter clientSince;

    private StatusFilter health;

    private BigDecimalFilter totalSpend;

    private LongFilter userId;

    private LongFilter projectId;

    private LongFilter ticketId;

    private LongFilter quoteId;

    private Boolean distinct;

    public ClientCriteria() {}

    public ClientCriteria(ClientCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.name = other.optionalName().map(StringFilter::copy).orElse(null);
        this.sector = other.optionalSector().map(MarketFilter::copy).orElse(null);
        this.clientSince = other.optionalClientSince().map(IntegerFilter::copy).orElse(null);
        this.health = other.optionalHealth().map(StatusFilter::copy).orElse(null);
        this.totalSpend = other.optionalTotalSpend().map(BigDecimalFilter::copy).orElse(null);
        this.userId = other.optionalUserId().map(LongFilter::copy).orElse(null);
        this.projectId = other.optionalProjectId().map(LongFilter::copy).orElse(null);
        this.ticketId = other.optionalTicketId().map(LongFilter::copy).orElse(null);
        this.quoteId = other.optionalQuoteId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public ClientCriteria copy() {
        return new ClientCriteria(this);
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

    public StringFilter getName() {
        return name;
    }

    public Optional<StringFilter> optionalName() {
        return Optional.ofNullable(name);
    }

    public StringFilter name() {
        if (name == null) {
            setName(new StringFilter());
        }
        return name;
    }

    public void setName(StringFilter name) {
        this.name = name;
    }

    public MarketFilter getSector() {
        return sector;
    }

    public Optional<MarketFilter> optionalSector() {
        return Optional.ofNullable(sector);
    }

    public MarketFilter sector() {
        if (sector == null) {
            setSector(new MarketFilter());
        }
        return sector;
    }

    public void setSector(MarketFilter sector) {
        this.sector = sector;
    }

    public IntegerFilter getClientSince() {
        return clientSince;
    }

    public Optional<IntegerFilter> optionalClientSince() {
        return Optional.ofNullable(clientSince);
    }

    public IntegerFilter clientSince() {
        if (clientSince == null) {
            setClientSince(new IntegerFilter());
        }
        return clientSince;
    }

    public void setClientSince(IntegerFilter clientSince) {
        this.clientSince = clientSince;
    }

    public StatusFilter getHealth() {
        return health;
    }

    public Optional<StatusFilter> optionalHealth() {
        return Optional.ofNullable(health);
    }

    public StatusFilter health() {
        if (health == null) {
            setHealth(new StatusFilter());
        }
        return health;
    }

    public void setHealth(StatusFilter health) {
        this.health = health;
    }

    public BigDecimalFilter getTotalSpend() {
        return totalSpend;
    }

    public Optional<BigDecimalFilter> optionalTotalSpend() {
        return Optional.ofNullable(totalSpend);
    }

    public BigDecimalFilter totalSpend() {
        if (totalSpend == null) {
            setTotalSpend(new BigDecimalFilter());
        }
        return totalSpend;
    }

    public void setTotalSpend(BigDecimalFilter totalSpend) {
        this.totalSpend = totalSpend;
    }

    public LongFilter getUserId() {
        return userId;
    }

    public Optional<LongFilter> optionalUserId() {
        return Optional.ofNullable(userId);
    }

    public LongFilter userId() {
        if (userId == null) {
            setUserId(new LongFilter());
        }
        return userId;
    }

    public void setUserId(LongFilter userId) {
        this.userId = userId;
    }

    public LongFilter getProjectId() {
        return projectId;
    }

    public Optional<LongFilter> optionalProjectId() {
        return Optional.ofNullable(projectId);
    }

    public LongFilter projectId() {
        if (projectId == null) {
            setProjectId(new LongFilter());
        }
        return projectId;
    }

    public void setProjectId(LongFilter projectId) {
        this.projectId = projectId;
    }

    public LongFilter getTicketId() {
        return ticketId;
    }

    public Optional<LongFilter> optionalTicketId() {
        return Optional.ofNullable(ticketId);
    }

    public LongFilter ticketId() {
        if (ticketId == null) {
            setTicketId(new LongFilter());
        }
        return ticketId;
    }

    public void setTicketId(LongFilter ticketId) {
        this.ticketId = ticketId;
    }

    public LongFilter getQuoteId() {
        return quoteId;
    }

    public Optional<LongFilter> optionalQuoteId() {
        return Optional.ofNullable(quoteId);
    }

    public LongFilter quoteId() {
        if (quoteId == null) {
            setQuoteId(new LongFilter());
        }
        return quoteId;
    }

    public void setQuoteId(LongFilter quoteId) {
        this.quoteId = quoteId;
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
        final ClientCriteria that = (ClientCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(name, that.name) &&
            Objects.equals(sector, that.sector) &&
            Objects.equals(clientSince, that.clientSince) &&
            Objects.equals(health, that.health) &&
            Objects.equals(totalSpend, that.totalSpend) &&
            Objects.equals(userId, that.userId) &&
            Objects.equals(projectId, that.projectId) &&
            Objects.equals(ticketId, that.ticketId) &&
            Objects.equals(quoteId, that.quoteId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, sector, clientSince, health, totalSpend, userId, projectId, ticketId, quoteId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ClientCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalName().map(f -> "name=" + f + ", ").orElse("") +
            optionalSector().map(f -> "sector=" + f + ", ").orElse("") +
            optionalClientSince().map(f -> "clientSince=" + f + ", ").orElse("") +
            optionalHealth().map(f -> "health=" + f + ", ").orElse("") +
            optionalTotalSpend().map(f -> "totalSpend=" + f + ", ").orElse("") +
            optionalUserId().map(f -> "userId=" + f + ", ").orElse("") +
            optionalProjectId().map(f -> "projectId=" + f + ", ").orElse("") +
            optionalTicketId().map(f -> "ticketId=" + f + ", ").orElse("") +
            optionalQuoteId().map(f -> "quoteId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
