package net.jojoaddison.consultancy.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import net.jojoaddison.consultancy.domain.enumeration.ServicePillar;
import net.jojoaddison.consultancy.domain.enumeration.Status;

/**
 * A DTO for the {@link net.jojoaddison.consultancy.domain.Project} entity.
 */
@Schema(description = "A delivery engagement.")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ProjectDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 20)
    private String reference;

    @NotNull
    @Size(max = 160)
    private String name;

    @NotNull
    private ServicePillar pillar;

    @NotNull
    private Status status;

    @Min(value = 0)
    @Max(value = 100)
    private Integer progress;

    private LocalDate dueDate;

    private Boolean delivered;

    @DecimalMin(value = "0")
    private BigDecimal budget;

    @DecimalMin(value = "0")
    private BigDecimal spent;

    @Size(max = 255)
    private String techStack;

    private TeamMemberDTO lead;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ServicePillar getPillar() {
        return pillar;
    }

    public void setPillar(ServicePillar pillar) {
        this.pillar = pillar;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Boolean getDelivered() {
        return delivered;
    }

    public void setDelivered(Boolean delivered) {
        this.delivered = delivered;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    public BigDecimal getSpent() {
        return spent;
    }

    public void setSpent(BigDecimal spent) {
        this.spent = spent;
    }

    public String getTechStack() {
        return techStack;
    }

    public void setTechStack(String techStack) {
        this.techStack = techStack;
    }

    public TeamMemberDTO getLead() {
        return lead;
    }

    public void setLead(TeamMemberDTO lead) {
        this.lead = lead;
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
        if (!(o instanceof ProjectDTO)) {
            return false;
        }

        ProjectDTO projectDTO = (ProjectDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, projectDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ProjectDTO{" +
            "id=" + getId() +
            ", reference='" + getReference() + "'" +
            ", name='" + getName() + "'" +
            ", pillar='" + getPillar() + "'" +
            ", status='" + getStatus() + "'" +
            ", progress=" + getProgress() +
            ", dueDate='" + getDueDate() + "'" +
            ", delivered='" + getDelivered() + "'" +
            ", budget=" + getBudget() +
            ", spent=" + getSpent() +
            ", techStack='" + getTechStack() + "'" +
            ", lead=" + getLead() +
            ", client=" + getClient() +
            "}";
    }
}
