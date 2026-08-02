package net.jojoaddison.consultancy.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A quantity of a catalogue item on a quote (rate snapshotted at add time).
 */
@Entity
@Table(name = "quote_line")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class QuoteLine implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Min(value = 1)
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @NotNull
    @DecimalMin(value = "0")
    @Column(name = "rate", precision = 21, scale = 2, nullable = false)
    private BigDecimal rate;

    @ManyToOne(optional = false)
    @NotNull
    private ServiceItem item;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "lines", "client" }, allowSetters = true)
    private Quote quote;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public QuoteLine id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getQuantity() {
        return this.quantity;
    }

    public QuoteLine quantity(Integer quantity) {
        this.setQuantity(quantity);
        return this;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getRate() {
        return this.rate;
    }

    public QuoteLine rate(BigDecimal rate) {
        this.setRate(rate);
        return this;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public ServiceItem getItem() {
        return this.item;
    }

    public void setItem(ServiceItem serviceItem) {
        this.item = serviceItem;
    }

    public QuoteLine item(ServiceItem serviceItem) {
        this.setItem(serviceItem);
        return this;
    }

    public Quote getQuote() {
        return this.quote;
    }

    public void setQuote(Quote quote) {
        this.quote = quote;
    }

    public QuoteLine quote(Quote quote) {
        this.setQuote(quote);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof QuoteLine)) {
            return false;
        }
        return getId() != null && getId().equals(((QuoteLine) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "QuoteLine{" +
            "id=" + getId() +
            ", quantity=" + getQuantity() +
            ", rate=" + getRate() +
            "}";
    }
}
