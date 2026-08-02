package net.jojoaddison.consultancy.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import net.jojoaddison.consultancy.domain.enumeration.EnquiryType;
import net.jojoaddison.consultancy.domain.enumeration.LeadStatus;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Lead captured from the public contact form.
 */
@Entity
@Table(name = "lead")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Lead implements Serializable {

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

    @NotNull
    @Size(max = 160)
    @Column(name = "email", length = 160, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "need")
    private EnquiryType need;

    @Lob
    @Column(name = "message")
    private String message;

    @Column(name = "created_date")
    private Instant createdDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private LeadStatus status;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Lead id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public Lead name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return this.email;
    }

    public Lead email(String email) {
        this.setEmail(email);
        return this;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public EnquiryType getNeed() {
        return this.need;
    }

    public Lead need(EnquiryType need) {
        this.setNeed(need);
        return this;
    }

    public void setNeed(EnquiryType need) {
        this.need = need;
    }

    public String getMessage() {
        return this.message;
    }

    public Lead message(String message) {
        this.setMessage(message);
        return this;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getCreatedDate() {
        return this.createdDate;
    }

    public Lead createdDate(Instant createdDate) {
        this.setCreatedDate(createdDate);
        return this;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public LeadStatus getStatus() {
        return this.status;
    }

    public Lead status(LeadStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(LeadStatus status) {
        this.status = status;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Lead)) {
            return false;
        }
        return getId() != null && getId().equals(((Lead) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Lead{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", email='" + getEmail() + "'" +
            ", need='" + getNeed() + "'" +
            ", message='" + getMessage() + "'" +
            ", createdDate='" + getCreatedDate() + "'" +
            ", status='" + getStatus() + "'" +
            "}";
    }
}
