package net.jojoaddison.consultancy.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import net.jojoaddison.consultancy.domain.enumeration.Status;
import net.jojoaddison.consultancy.domain.enumeration.TicketState;

/**
 * A DTO for the {@link net.jojoaddison.consultancy.domain.Ticket} entity.
 */
@Schema(description = "Support-desk ticket. priority reuses the shared Status enum.")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class TicketDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 20)
    private String reference;

    @NotNull
    @Size(max = 200)
    private String subject;

    @NotNull
    private Status priority;

    private Instant openedAt;

    @Min(value = 0)
    private Integer slaHours;

    @NotNull
    private TicketState state;

    private TeamMemberDTO owner;

    @NotNull
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

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Status getPriority() {
        return priority;
    }

    public void setPriority(Status priority) {
        this.priority = priority;
    }

    public Instant getOpenedAt() {
        return openedAt;
    }

    public void setOpenedAt(Instant openedAt) {
        this.openedAt = openedAt;
    }

    public Integer getSlaHours() {
        return slaHours;
    }

    public void setSlaHours(Integer slaHours) {
        this.slaHours = slaHours;
    }

    public TicketState getState() {
        return state;
    }

    public void setState(TicketState state) {
        this.state = state;
    }

    public TeamMemberDTO getOwner() {
        return owner;
    }

    public void setOwner(TeamMemberDTO owner) {
        this.owner = owner;
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
        if (!(o instanceof TicketDTO)) {
            return false;
        }

        TicketDTO ticketDTO = (TicketDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, ticketDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "TicketDTO{" +
            "id=" + getId() +
            ", reference='" + getReference() + "'" +
            ", subject='" + getSubject() + "'" +
            ", priority='" + getPriority() + "'" +
            ", openedAt='" + getOpenedAt() + "'" +
            ", slaHours=" + getSlaHours() +
            ", state='" + getState() + "'" +
            ", owner=" + getOwner() +
            ", client=" + getClient() +
            "}";
    }
}
