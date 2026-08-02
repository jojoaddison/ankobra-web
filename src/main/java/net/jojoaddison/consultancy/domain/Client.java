package net.jojoaddison.consultancy.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import net.jojoaddison.consultancy.domain.enumeration.Market;
import net.jojoaddison.consultancy.domain.enumeration.Status;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A consultancy client. A client's portal user is scoped to only this client's data.
 */
@Entity
@Table(name = "client")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Client implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(max = 120)
    @Column(name = "name", length = 120, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "sector")
    private Market sector;

    @Column(name = "client_since")
    private Integer clientSince;

    @Enumerated(EnumType.STRING)
    @Column(name = "health")
    private Status health;

    @DecimalMin(value = "0")
    @Column(name = "total_spend", precision = 21, scale = 2)
    private BigDecimal totalSpend;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(unique = true)
    private User user;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "client")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "milestones", "lead", "client" }, allowSetters = true)
    private Set<Project> projects = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "client")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "owner", "client" }, allowSetters = true)
    private Set<Ticket> tickets = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "client")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "lines", "client" }, allowSetters = true)
    private Set<Quote> quotes = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Client id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public Client name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Market getSector() {
        return this.sector;
    }

    public Client sector(Market sector) {
        this.setSector(sector);
        return this;
    }

    public void setSector(Market sector) {
        this.sector = sector;
    }

    public Integer getClientSince() {
        return this.clientSince;
    }

    public Client clientSince(Integer clientSince) {
        this.setClientSince(clientSince);
        return this;
    }

    public void setClientSince(Integer clientSince) {
        this.clientSince = clientSince;
    }

    public Status getHealth() {
        return this.health;
    }

    public Client health(Status health) {
        this.setHealth(health);
        return this;
    }

    public void setHealth(Status health) {
        this.health = health;
    }

    public BigDecimal getTotalSpend() {
        return this.totalSpend;
    }

    public Client totalSpend(BigDecimal totalSpend) {
        this.setTotalSpend(totalSpend);
        return this;
    }

    public void setTotalSpend(BigDecimal totalSpend) {
        this.totalSpend = totalSpend;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Client user(User user) {
        this.setUser(user);
        return this;
    }

    public Set<Project> getProjects() {
        return this.projects;
    }

    public void setProjects(Set<Project> projects) {
        if (this.projects != null) {
            this.projects.forEach(i -> i.setClient(null));
        }
        if (projects != null) {
            projects.forEach(i -> i.setClient(this));
        }
        this.projects = projects;
    }

    public Client projects(Set<Project> projects) {
        this.setProjects(projects);
        return this;
    }

    public Client addProject(Project project) {
        this.projects.add(project);
        project.setClient(this);
        return this;
    }

    public Client removeProject(Project project) {
        this.projects.remove(project);
        project.setClient(null);
        return this;
    }

    public Set<Ticket> getTickets() {
        return this.tickets;
    }

    public void setTickets(Set<Ticket> tickets) {
        if (this.tickets != null) {
            this.tickets.forEach(i -> i.setClient(null));
        }
        if (tickets != null) {
            tickets.forEach(i -> i.setClient(this));
        }
        this.tickets = tickets;
    }

    public Client tickets(Set<Ticket> tickets) {
        this.setTickets(tickets);
        return this;
    }

    public Client addTicket(Ticket ticket) {
        this.tickets.add(ticket);
        ticket.setClient(this);
        return this;
    }

    public Client removeTicket(Ticket ticket) {
        this.tickets.remove(ticket);
        ticket.setClient(null);
        return this;
    }

    public Set<Quote> getQuotes() {
        return this.quotes;
    }

    public void setQuotes(Set<Quote> quotes) {
        if (this.quotes != null) {
            this.quotes.forEach(i -> i.setClient(null));
        }
        if (quotes != null) {
            quotes.forEach(i -> i.setClient(this));
        }
        this.quotes = quotes;
    }

    public Client quotes(Set<Quote> quotes) {
        this.setQuotes(quotes);
        return this;
    }

    public Client addQuote(Quote quote) {
        this.quotes.add(quote);
        quote.setClient(this);
        return this;
    }

    public Client removeQuote(Quote quote) {
        this.quotes.remove(quote);
        quote.setClient(null);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Client)) {
            return false;
        }
        return getId() != null && getId().equals(((Client) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Client{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", sector='" + getSector() + "'" +
            ", clientSince=" + getClientSince() +
            ", health='" + getHealth() + "'" +
            ", totalSpend=" + getTotalSpend() +
            "}";
    }
}
