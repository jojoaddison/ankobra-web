package net.jojoaddison.consultancy.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import net.jojoaddison.consultancy.domain.enumeration.CatalogueGroup;
import net.jojoaddison.consultancy.domain.enumeration.RateUnit;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A line in the service catalogue that the quote builder composes.
 */
@Entity
@Table(name = "service_item")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ServiceItem implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(max = 20)
    @Column(name = "code", length = 20, nullable = false, unique = true)
    private String code;

    @NotNull
    @Size(max = 160)
    @Column(name = "name", length = 160, nullable = false)
    private String name;

    @Size(max = 400)
    @Column(name = "description", length = 400)
    private String description;

    @NotNull
    @DecimalMin(value = "0")
    @Column(name = "rate", precision = 21, scale = 2, nullable = false)
    private BigDecimal rate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "unit", nullable = false)
    private RateUnit unit;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "service_group", nullable = false)
    private CatalogueGroup serviceGroup;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public ServiceItem id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return this.code;
    }

    public ServiceItem code(String code) {
        this.setCode(code);
        return this;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return this.name;
    }

    public ServiceItem name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public ServiceItem description(String description) {
        this.setDescription(description);
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getRate() {
        return this.rate;
    }

    public ServiceItem rate(BigDecimal rate) {
        this.setRate(rate);
        return this;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public RateUnit getUnit() {
        return this.unit;
    }

    public ServiceItem unit(RateUnit unit) {
        this.setUnit(unit);
        return this;
    }

    public void setUnit(RateUnit unit) {
        this.unit = unit;
    }

    public CatalogueGroup getServiceGroup() {
        return this.serviceGroup;
    }

    public ServiceItem serviceGroup(CatalogueGroup serviceGroup) {
        this.setServiceGroup(serviceGroup);
        return this;
    }

    public void setServiceGroup(CatalogueGroup serviceGroup) {
        this.serviceGroup = serviceGroup;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ServiceItem)) {
            return false;
        }
        return getId() != null && getId().equals(((ServiceItem) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ServiceItem{" +
            "id=" + getId() +
            ", code='" + getCode() + "'" +
            ", name='" + getName() + "'" +
            ", description='" + getDescription() + "'" +
            ", rate=" + getRate() +
            ", unit='" + getUnit() + "'" +
            ", serviceGroup='" + getServiceGroup() + "'" +
            "}";
    }
}
