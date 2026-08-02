package net.jojoaddison.consultancy.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import net.jojoaddison.consultancy.domain.enumeration.QuoteStatus;

/**
 * A DTO for the {@link net.jojoaddison.consultancy.domain.Quote} entity.
 */
@Schema(description = "An estimate assembled from catalogue items.")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class QuoteDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 20)
    private String reference;

    @Size(max = 160)
    private String title;

    private Instant createdDate;

    private QuoteStatus status;

    private ClientDTO client;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public QuoteStatus getStatus() {
        return status;
    }

    public void setStatus(QuoteStatus status) {
        this.status = status;
    }

    public ClientDTO getClient() {
        return client;
    }

    public void setClient(ClientDTO client) {
        this.client = client;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof QuoteDTO)) {
            return false;
        }

        QuoteDTO quoteDTO = (QuoteDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, quoteDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "QuoteDTO{" +
            "id=" + getId() +
            ", reference='" + getReference() + "'" +
            ", title='" + getTitle() + "'" +
            ", createdDate='" + getCreatedDate() + "'" +
            ", status='" + getStatus() + "'" +
            ", client=" + getClient() +
            "}";
    }
}
