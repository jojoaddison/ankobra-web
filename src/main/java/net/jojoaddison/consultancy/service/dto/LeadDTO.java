package net.jojoaddison.consultancy.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import net.jojoaddison.consultancy.domain.enumeration.EnquiryType;
import net.jojoaddison.consultancy.domain.enumeration.LeadStatus;

/**
 * A DTO for the {@link net.jojoaddison.consultancy.domain.Lead} entity.
 */
@Schema(description = "Lead captured from the public contact form.")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class LeadDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 120)
    private String name;

    @NotNull
    @Size(max = 160)
    private String email;

    private EnquiryType need;

    @Lob
    private String message;

    private Instant createdDate;

    private LeadStatus status;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public EnquiryType getNeed() {
        return need;
    }

    public void setNeed(EnquiryType need) {
        this.need = need;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public LeadStatus getStatus() {
        return status;
    }

    public void setStatus(LeadStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LeadDTO)) {
            return false;
        }

        LeadDTO leadDTO = (LeadDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, leadDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "LeadDTO{" +
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
