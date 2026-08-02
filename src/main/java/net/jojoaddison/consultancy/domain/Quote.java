package net.jojoaddison.consultancy.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import net.jojoaddison.consultancy.domain.enumeration.QuoteStatus;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * An estimate assembled from catalogue items.
 */
@Entity
@Table(name = "quote")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Quote implements Serializable {

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

    @Size(max = 160)
    @Column(name = "title", length = 160)
    private String title;

    @Column(name = "created_date")
    private Instant createdDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private QuoteStatus status;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "quote")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "item", "quote" }, allowSetters = true)
    private Set<QuoteLine> lines = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "user", "projects", "tickets", "quotes" }, allowSetters = true)
    private Client client;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Quote id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReference() {
        return this.reference;
    }

    public Quote reference(String reference) {
        this.setReference(reference);
        return this;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getTitle() {
        return this.title;
    }

    public Quote title(String title) {
        this.setTitle(title);
        return this;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Instant getCreatedDate() {
        return this.createdDate;
    }

    public Quote createdDate(Instant createdDate) {
        this.setCreatedDate(createdDate);
        return this;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public QuoteStatus getStatus() {
        return this.status;
    }

    public Quote status(QuoteStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(QuoteStatus status) {
        this.status = status;
    }

    public Set<QuoteLine> getLines() {
        return this.lines;
    }

    public void setLines(Set<QuoteLine> quoteLines) {
        if (this.lines != null) {
            this.lines.forEach(i -> i.setQuote(null));
        }
        if (quoteLines != null) {
            quoteLines.forEach(i -> i.setQuote(this));
        }
        this.lines = quoteLines;
    }

    public Quote lines(Set<QuoteLine> quoteLines) {
        this.setLines(quoteLines);
        return this;
    }

    public Quote addLine(QuoteLine quoteLine) {
        this.lines.add(quoteLine);
        quoteLine.setQuote(this);
        return this;
    }

    public Quote removeLine(QuoteLine quoteLine) {
        this.lines.remove(quoteLine);
        quoteLine.setQuote(null);
        return this;
    }

    public Client getClient() {
        return this.client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Quote client(Client client) {
        this.setClient(client);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Quote)) {
            return false;
        }
        return getId() != null && getId().equals(((Quote) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Quote{" +
            "id=" + getId() +
            ", reference='" + getReference() + "'" +
            ", title='" + getTitle() + "'" +
            ", createdDate='" + getCreatedDate() + "'" +
            ", status='" + getStatus() + "'" +
            "}";
    }
}
