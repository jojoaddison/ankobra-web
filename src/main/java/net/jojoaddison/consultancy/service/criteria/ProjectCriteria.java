package net.jojoaddison.consultancy.service.criteria;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import net.jojoaddison.consultancy.domain.enumeration.ServicePillar;
import net.jojoaddison.consultancy.domain.enumeration.Status;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link net.jojoaddison.consultancy.domain.Project} entity. This class is used
 * in {@link net.jojoaddison.consultancy.web.rest.ProjectResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /projects?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ProjectCriteria implements Serializable, Criteria {

    /**
     * Class for filtering ServicePillar
     */
    public static class ServicePillarFilter extends Filter<ServicePillar> {

        public ServicePillarFilter() {}

        public ServicePillarFilter(ServicePillarFilter filter) {
            super(filter);
        }

        @Override
        public ServicePillarFilter copy() {
            return new ServicePillarFilter(this);
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

    private StringFilter reference;

    private StringFilter name;

    private ServicePillarFilter pillar;

    private StatusFilter status;

    private IntegerFilter progress;

    private LocalDateFilter dueDate;

    private BooleanFilter delivered;

    private BigDecimalFilter budget;

    private BigDecimalFilter spent;

    private StringFilter techStack;

    private LongFilter milestoneId;

    private LongFilter leadId;

    private LongFilter clientId;

    private Boolean distinct;

    public ProjectCriteria() {}

    public ProjectCriteria(ProjectCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.reference = other.optionalReference().map(StringFilter::copy).orElse(null);
        this.name = other.optionalName().map(StringFilter::copy).orElse(null);
        this.pillar = other.optionalPillar().map(ServicePillarFilter::copy).orElse(null);
        this.status = other.optionalStatus().map(StatusFilter::copy).orElse(null);
        this.progress = other.optionalProgress().map(IntegerFilter::copy).orElse(null);
        this.dueDate = other.optionalDueDate().map(LocalDateFilter::copy).orElse(null);
        this.delivered = other.optionalDelivered().map(BooleanFilter::copy).orElse(null);
        this.budget = other.optionalBudget().map(BigDecimalFilter::copy).orElse(null);
        this.spent = other.optionalSpent().map(BigDecimalFilter::copy).orElse(null);
        this.techStack = other.optionalTechStack().map(StringFilter::copy).orElse(null);
        this.milestoneId = other.optionalMilestoneId().map(LongFilter::copy).orElse(null);
        this.leadId = other.optionalLeadId().map(LongFilter::copy).orElse(null);
        this.clientId = other.optionalClientId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public ProjectCriteria copy() {
        return new ProjectCriteria(this);
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

    public ServicePillarFilter getPillar() {
        return pillar;
    }

    public Optional<ServicePillarFilter> optionalPillar() {
        return Optional.ofNullable(pillar);
    }

    public ServicePillarFilter pillar() {
        if (pillar == null) {
            setPillar(new ServicePillarFilter());
        }
        return pillar;
    }

    public void setPillar(ServicePillarFilter pillar) {
        this.pillar = pillar;
    }

    public StatusFilter getStatus() {
        return status;
    }

    public Optional<StatusFilter> optionalStatus() {
        return Optional.ofNullable(status);
    }

    public StatusFilter status() {
        if (status == null) {
            setStatus(new StatusFilter());
        }
        return status;
    }

    public void setStatus(StatusFilter status) {
        this.status = status;
    }

    public IntegerFilter getProgress() {
        return progress;
    }

    public Optional<IntegerFilter> optionalProgress() {
        return Optional.ofNullable(progress);
    }

    public IntegerFilter progress() {
        if (progress == null) {
            setProgress(new IntegerFilter());
        }
        return progress;
    }

    public void setProgress(IntegerFilter progress) {
        this.progress = progress;
    }

    public LocalDateFilter getDueDate() {
        return dueDate;
    }

    public Optional<LocalDateFilter> optionalDueDate() {
        return Optional.ofNullable(dueDate);
    }

    public LocalDateFilter dueDate() {
        if (dueDate == null) {
            setDueDate(new LocalDateFilter());
        }
        return dueDate;
    }

    public void setDueDate(LocalDateFilter dueDate) {
        this.dueDate = dueDate;
    }

    public BooleanFilter getDelivered() {
        return delivered;
    }

    public Optional<BooleanFilter> optionalDelivered() {
        return Optional.ofNullable(delivered);
    }

    public BooleanFilter delivered() {
        if (delivered == null) {
            setDelivered(new BooleanFilter());
        }
        return delivered;
    }

    public void setDelivered(BooleanFilter delivered) {
        this.delivered = delivered;
    }

    public BigDecimalFilter getBudget() {
        return budget;
    }

    public Optional<BigDecimalFilter> optionalBudget() {
        return Optional.ofNullable(budget);
    }

    public BigDecimalFilter budget() {
        if (budget == null) {
            setBudget(new BigDecimalFilter());
        }
        return budget;
    }

    public void setBudget(BigDecimalFilter budget) {
        this.budget = budget;
    }

    public BigDecimalFilter getSpent() {
        return spent;
    }

    public Optional<BigDecimalFilter> optionalSpent() {
        return Optional.ofNullable(spent);
    }

    public BigDecimalFilter spent() {
        if (spent == null) {
            setSpent(new BigDecimalFilter());
        }
        return spent;
    }

    public void setSpent(BigDecimalFilter spent) {
        this.spent = spent;
    }

    public StringFilter getTechStack() {
        return techStack;
    }

    public Optional<StringFilter> optionalTechStack() {
        return Optional.ofNullable(techStack);
    }

    public StringFilter techStack() {
        if (techStack == null) {
            setTechStack(new StringFilter());
        }
        return techStack;
    }

    public void setTechStack(StringFilter techStack) {
        this.techStack = techStack;
    }

    public LongFilter getMilestoneId() {
        return milestoneId;
    }

    public Optional<LongFilter> optionalMilestoneId() {
        return Optional.ofNullable(milestoneId);
    }

    public LongFilter milestoneId() {
        if (milestoneId == null) {
            setMilestoneId(new LongFilter());
        }
        return milestoneId;
    }

    public void setMilestoneId(LongFilter milestoneId) {
        this.milestoneId = milestoneId;
    }

    public LongFilter getLeadId() {
        return leadId;
    }

    public Optional<LongFilter> optionalLeadId() {
        return Optional.ofNullable(leadId);
    }

    public LongFilter leadId() {
        if (leadId == null) {
            setLeadId(new LongFilter());
        }
        return leadId;
    }

    public void setLeadId(LongFilter leadId) {
        this.leadId = leadId;
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
        final ProjectCriteria that = (ProjectCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(reference, that.reference) &&
            Objects.equals(name, that.name) &&
            Objects.equals(pillar, that.pillar) &&
            Objects.equals(status, that.status) &&
            Objects.equals(progress, that.progress) &&
            Objects.equals(dueDate, that.dueDate) &&
            Objects.equals(delivered, that.delivered) &&
            Objects.equals(budget, that.budget) &&
            Objects.equals(spent, that.spent) &&
            Objects.equals(techStack, that.techStack) &&
            Objects.equals(milestoneId, that.milestoneId) &&
            Objects.equals(leadId, that.leadId) &&
            Objects.equals(clientId, that.clientId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            reference,
            name,
            pillar,
            status,
            progress,
            dueDate,
            delivered,
            budget,
            spent,
            techStack,
            milestoneId,
            leadId,
            clientId,
            distinct
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ProjectCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalReference().map(f -> "reference=" + f + ", ").orElse("") +
            optionalName().map(f -> "name=" + f + ", ").orElse("") +
            optionalPillar().map(f -> "pillar=" + f + ", ").orElse("") +
            optionalStatus().map(f -> "status=" + f + ", ").orElse("") +
            optionalProgress().map(f -> "progress=" + f + ", ").orElse("") +
            optionalDueDate().map(f -> "dueDate=" + f + ", ").orElse("") +
            optionalDelivered().map(f -> "delivered=" + f + ", ").orElse("") +
            optionalBudget().map(f -> "budget=" + f + ", ").orElse("") +
            optionalSpent().map(f -> "spent=" + f + ", ").orElse("") +
            optionalTechStack().map(f -> "techStack=" + f + ", ").orElse("") +
            optionalMilestoneId().map(f -> "milestoneId=" + f + ", ").orElse("") +
            optionalLeadId().map(f -> "leadId=" + f + ", ").orElse("") +
            optionalClientId().map(f -> "clientId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
