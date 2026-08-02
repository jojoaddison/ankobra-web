package net.jojoaddison.consultancy.service.criteria;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import net.jojoaddison.consultancy.domain.enumeration.CatalogueGroup;
import net.jojoaddison.consultancy.domain.enumeration.RateUnit;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link net.jojoaddison.consultancy.domain.ServiceItem} entity. This class is used
 * in {@link net.jojoaddison.consultancy.web.rest.ServiceItemResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /service-items?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ServiceItemCriteria implements Serializable, Criteria {

    /**
     * Class for filtering RateUnit
     */
    public static class RateUnitFilter extends Filter<RateUnit> {

        public RateUnitFilter() {}

        public RateUnitFilter(RateUnitFilter filter) {
            super(filter);
        }

        @Override
        public RateUnitFilter copy() {
            return new RateUnitFilter(this);
        }
    }

    /**
     * Class for filtering CatalogueGroup
     */
    public static class CatalogueGroupFilter extends Filter<CatalogueGroup> {

        public CatalogueGroupFilter() {}

        public CatalogueGroupFilter(CatalogueGroupFilter filter) {
            super(filter);
        }

        @Override
        public CatalogueGroupFilter copy() {
            return new CatalogueGroupFilter(this);
        }
    }

    @Serial
    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter code;

    private StringFilter name;

    private StringFilter description;

    private BigDecimalFilter rate;

    private RateUnitFilter unit;

    private CatalogueGroupFilter serviceGroup;

    private Boolean distinct;

    public ServiceItemCriteria() {}

    public ServiceItemCriteria(ServiceItemCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.code = other.optionalCode().map(StringFilter::copy).orElse(null);
        this.name = other.optionalName().map(StringFilter::copy).orElse(null);
        this.description = other.optionalDescription().map(StringFilter::copy).orElse(null);
        this.rate = other.optionalRate().map(BigDecimalFilter::copy).orElse(null);
        this.unit = other.optionalUnit().map(RateUnitFilter::copy).orElse(null);
        this.serviceGroup = other.optionalServiceGroup().map(CatalogueGroupFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public ServiceItemCriteria copy() {
        return new ServiceItemCriteria(this);
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

    public StringFilter getCode() {
        return code;
    }

    public Optional<StringFilter> optionalCode() {
        return Optional.ofNullable(code);
    }

    public StringFilter code() {
        if (code == null) {
            setCode(new StringFilter());
        }
        return code;
    }

    public void setCode(StringFilter code) {
        this.code = code;
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

    public StringFilter getDescription() {
        return description;
    }

    public Optional<StringFilter> optionalDescription() {
        return Optional.ofNullable(description);
    }

    public StringFilter description() {
        if (description == null) {
            setDescription(new StringFilter());
        }
        return description;
    }

    public void setDescription(StringFilter description) {
        this.description = description;
    }

    public BigDecimalFilter getRate() {
        return rate;
    }

    public Optional<BigDecimalFilter> optionalRate() {
        return Optional.ofNullable(rate);
    }

    public BigDecimalFilter rate() {
        if (rate == null) {
            setRate(new BigDecimalFilter());
        }
        return rate;
    }

    public void setRate(BigDecimalFilter rate) {
        this.rate = rate;
    }

    public RateUnitFilter getUnit() {
        return unit;
    }

    public Optional<RateUnitFilter> optionalUnit() {
        return Optional.ofNullable(unit);
    }

    public RateUnitFilter unit() {
        if (unit == null) {
            setUnit(new RateUnitFilter());
        }
        return unit;
    }

    public void setUnit(RateUnitFilter unit) {
        this.unit = unit;
    }

    public CatalogueGroupFilter getServiceGroup() {
        return serviceGroup;
    }

    public Optional<CatalogueGroupFilter> optionalServiceGroup() {
        return Optional.ofNullable(serviceGroup);
    }

    public CatalogueGroupFilter serviceGroup() {
        if (serviceGroup == null) {
            setServiceGroup(new CatalogueGroupFilter());
        }
        return serviceGroup;
    }

    public void setServiceGroup(CatalogueGroupFilter serviceGroup) {
        this.serviceGroup = serviceGroup;
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
        final ServiceItemCriteria that = (ServiceItemCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(code, that.code) &&
            Objects.equals(name, that.name) &&
            Objects.equals(description, that.description) &&
            Objects.equals(rate, that.rate) &&
            Objects.equals(unit, that.unit) &&
            Objects.equals(serviceGroup, that.serviceGroup) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, code, name, description, rate, unit, serviceGroup, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ServiceItemCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalCode().map(f -> "code=" + f + ", ").orElse("") +
            optionalName().map(f -> "name=" + f + ", ").orElse("") +
            optionalDescription().map(f -> "description=" + f + ", ").orElse("") +
            optionalRate().map(f -> "rate=" + f + ", ").orElse("") +
            optionalUnit().map(f -> "unit=" + f + ", ").orElse("") +
            optionalServiceGroup().map(f -> "serviceGroup=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
