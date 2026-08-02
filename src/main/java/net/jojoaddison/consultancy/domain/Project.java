package net.jojoaddison.consultancy.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import net.jojoaddison.consultancy.domain.enumeration.ServicePillar;
import net.jojoaddison.consultancy.domain.enumeration.Status;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A delivery engagement.
 */
@Entity
@Table(name = "project")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Project implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(max = 20)
    @Column(name = "reference", length = 20, nullable = false, unique = true)
    private String reference;

    @NotNull
    @Size(max = 160)
    @Column(name = "name", length = 160, nullable = false)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "pillar", nullable = false)
    private ServicePillar pillar;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Min(value = 0)
    @Max(value = 100)
    @Column(name = "progress")
    private Integer progress;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "delivered")
    private Boolean delivered;

    @DecimalMin(value = "0")
    @Column(name = "budget", precision = 21, scale = 2)
    private BigDecimal budget;

    @DecimalMin(value = "0")
    @Column(name = "spent", precision = 21, scale = 2)
    private BigDecimal spent;

    @Size(max = 255)
    @Column(name = "tech_stack", length = 255)
    private String techStack;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "project")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "project" }, allowSetters = true)
    private Set<Milestone> milestones = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "user" }, allowSetters = true)
    private TeamMember lead;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "user", "projects", "tickets", "quotes" }, allowSetters = true)
    private Client client;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Project id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReference() {
        return this.reference;
    }

    public Project reference(String reference) {
        this.setReference(reference);
        return this;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getName() {
        return this.name;
    }

    public Project name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ServicePillar getPillar() {
        return this.pillar;
    }

    public Project pillar(ServicePillar pillar) {
        this.setPillar(pillar);
        return this;
    }

    public void setPillar(ServicePillar pillar) {
        this.pillar = pillar;
    }

    public Status getStatus() {
        return this.status;
    }

    public Project status(Status status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Integer getProgress() {
        return this.progress;
    }

    public Project progress(Integer progress) {
        this.setProgress(progress);
        return this;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public LocalDate getDueDate() {
        return this.dueDate;
    }

    public Project dueDate(LocalDate dueDate) {
        this.setDueDate(dueDate);
        return this;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Boolean getDelivered() {
        return this.delivered;
    }

    public Project delivered(Boolean delivered) {
        this.setDelivered(delivered);
        return this;
    }

    public void setDelivered(Boolean delivered) {
        this.delivered = delivered;
    }

    public BigDecimal getBudget() {
        return this.budget;
    }

    public Project budget(BigDecimal budget) {
        this.setBudget(budget);
        return this;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    public BigDecimal getSpent() {
        return this.spent;
    }

    public Project spent(BigDecimal spent) {
        this.setSpent(spent);
        return this;
    }

    public void setSpent(BigDecimal spent) {
        this.spent = spent;
    }

    public String getTechStack() {
        return this.techStack;
    }

    public Project techStack(String techStack) {
        this.setTechStack(techStack);
        return this;
    }

    public void setTechStack(String techStack) {
        this.techStack = techStack;
    }

    public Set<Milestone> getMilestones() {
        return this.milestones;
    }

    public void setMilestones(Set<Milestone> milestones) {
        if (this.milestones != null) {
            this.milestones.forEach(i -> i.setProject(null));
        }
        if (milestones != null) {
            milestones.forEach(i -> i.setProject(this));
        }
        this.milestones = milestones;
    }

    public Project milestones(Set<Milestone> milestones) {
        this.setMilestones(milestones);
        return this;
    }

    public Project addMilestone(Milestone milestone) {
        this.milestones.add(milestone);
        milestone.setProject(this);
        return this;
    }

    public Project removeMilestone(Milestone milestone) {
        this.milestones.remove(milestone);
        milestone.setProject(null);
        return this;
    }

    public TeamMember getLead() {
        return this.lead;
    }

    public void setLead(TeamMember teamMember) {
        this.lead = teamMember;
    }

    public Project lead(TeamMember teamMember) {
        this.setLead(teamMember);
        return this;
    }

    public Client getClient() {
        return this.client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Project client(Client client) {
        this.setClient(client);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Project)) {
            return false;
        }
        return getId() != null && getId().equals(((Project) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Project{" +
            "id=" + getId() +
            ", reference='" + getReference() + "'" +
            ", name='" + getName() + "'" +
            ", pillar='" + getPillar() + "'" +
            ", status='" + getStatus() + "'" +
            ", progress=" + getProgress() +
            ", dueDate='" + getDueDate() + "'" +
            ", delivered='" + getDelivered() + "'" +
            ", budget=" + getBudget() +
            ", spent=" + getSpent() +
            ", techStack='" + getTechStack() + "'" +
            "}";
    }
}
