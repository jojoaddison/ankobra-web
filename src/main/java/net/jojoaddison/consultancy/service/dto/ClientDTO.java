package net.jojoaddison.consultancy.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;
import net.jojoaddison.consultancy.domain.enumeration.Market;
import net.jojoaddison.consultancy.domain.enumeration.Status;

/**
 * A DTO for the {@link net.jojoaddison.consultancy.domain.Client} entity.
 */
@Schema(description = "A consultancy client. A client's portal user is scoped to only this client's data.")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ClientDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 120)
    private String name;

    private Market sector;

    private Integer clientSince;

    private Status health;

    @DecimalMin(value = "0")
    private BigDecimal totalSpend;

    private UserDTO user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Market getSector() {
        return sector;
    }

    public void setSector(Market sector) {
        this.sector = sector;
    }

    public Integer getClientSince() {
        return clientSince;
    }

    public void setClientSince(Integer clientSince) {
        this.clientSince = clientSince;
    }

    public Status getHealth() {
        return health;
    }

    public void setHealth(Status health) {
        this.health = health;
    }

    public BigDecimal getTotalSpend() {
        return totalSpend;
    }

    public void setTotalSpend(BigDecimal totalSpend) {
        this.totalSpend = totalSpend;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ClientDTO)) {
            return false;
        }

        ClientDTO clientDTO = (ClientDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, clientDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ClientDTO{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", sector='" + getSector() + "'" +
            ", clientSince=" + getClientSince() +
            ", health='" + getHealth() + "'" +
            ", totalSpend=" + getTotalSpend() +
            ", user=" + getUser() +
            "}";
    }
}
