package net.jojoaddison.consultancy.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;
import net.jojoaddison.consultancy.domain.enumeration.CatalogueGroup;
import net.jojoaddison.consultancy.domain.enumeration.RateUnit;

/**
 * A DTO for the {@link net.jojoaddison.consultancy.domain.ServiceItem} entity.
 */
@Schema(description = "A line in the service catalogue that the quote builder composes.")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ServiceItemDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 20)
    private String code;

    @NotNull
    @Size(max = 160)
    private String name;

    @Size(max = 400)
    private String description;

    @NotNull
    @DecimalMin(value = "0")
    private BigDecimal rate;

    @NotNull
    private RateUnit unit;

    @NotNull
    private CatalogueGroup serviceGroup;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public RateUnit getUnit() {
        return unit;
    }

    public void setUnit(RateUnit unit) {
        this.unit = unit;
    }

    public CatalogueGroup getServiceGroup() {
        return serviceGroup;
    }

    public void setServiceGroup(CatalogueGroup serviceGroup) {
        this.serviceGroup = serviceGroup;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ServiceItemDTO)) {
            return false;
        }

        ServiceItemDTO serviceItemDTO = (ServiceItemDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, serviceItemDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ServiceItemDTO{" +
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
