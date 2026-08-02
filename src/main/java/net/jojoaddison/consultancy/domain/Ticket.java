package net.jojoaddison.consultancy.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import net.jojoaddison.consultancy.domain.enumeration.Status;
import net.jojoaddison.consultancy.domain.enumeration.TicketState;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Support-desk ticket. priority reuses the shared Status enum.
 */
@Entity
@Table(name = "ticket")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Ticket implements Serializable {

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
    @Size(max = 200)
    @Column(name = "subject", length = 200, nullable = false)
    private String subject;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private Status priority;

    @Column(name = "opened_at")
    private Instant openedAt;

    @Min(value = 0)
    @Column(name = "sla_hours")
    private Integer slaHours;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private TicketState state;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "user" }, allowSetters = true)
    private TeamMember owner;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "user", "projects", "tickets", "quotes" }, allowSetters = true)
    private Client client;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Ticket id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReference() {
        return this.reference;
    }

    public Ticket reference(String reference) {
        this.setReference(reference);
        return this;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getSubject() {
        return this.subject;
    }

    public Ticket subject(String subject) {
        this.setSubject(subject);
        return this;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Status getPriority() {
        return this.priority;
    }

    public Ticket priority(Status priority) {
        this.setPriority(priority);
        return this;
    }

    public void setPriority(Status priority) {
        this.priority = priority;
    }

    public Instant getOpenedAt() {
        return this.openedAt;
    }

    public Ticket openedAt(Instant openedAt) {
        this.setOpenedAt(openedAt);
        return this;
    }

    public void setOpenedAt(Instant openedAt) {
        this.openedAt = openedAt;
    }

    public Integer getSlaHours() {
        return this.slaHours;
    }

    public Ticket slaHours(Integer slaHours) {
        this.setSlaHours(slaHours);
        return this;
    }

    public void setSlaHours(Integer slaHours) {
        this.slaHours = slaHours;
    }

    public TicketState getState() {
        return this.state;
    }

    public Ticket state(TicketState state) {
        this.setState(state);
        return this;
    }

    public void setState(TicketState state) {
        this.state = state;
    }

    public TeamMember getOwner() {
        return this.owner;
    }

    public void setOwner(TeamMember teamMember) {
        this.owner = teamMember;
    }

    public Ticket owner(TeamMember teamMember) {
        this.setOwner(teamMember);
        return this;
    }

    public Client getClient() {
        return this.client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Ticket client(Client client) {
        this.setClient(client);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Ticket)) {
            return false;
        }
        return getId() != null && getId().equals(((Ticket) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Ticket{" +
            "id=" + getId() +
            ", reference='" + getReference() + "'" +
            ", subject='" + getSubject() + "'" +
            ", priority='" + getPriority() + "'" +
            ", openedAt='" + getOpenedAt() + "'" +
            ", slaHours=" + getSlaHours() +
            ", state='" + getState() + "'" +
            "}";
    }
}
