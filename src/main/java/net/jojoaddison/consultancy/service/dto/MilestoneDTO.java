package net.jojoaddison.consultancy.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;
import net.jojoaddison.consultancy.domain.enumeration.MilestoneState;

/**
 * A DTO for the {@link net.jojoaddison.consultancy.domain.Milestone} entity.
 */
@Schema(description = "Ordered step on a project timeline (done / now / next).")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class MilestoneDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 160)
    private String title;

    @NotNull
    private MilestoneState state;

    @NotNull
    @Min(value = 0)
    private Integer position;

    @NotNull
    private ProjectDTO project;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public MilestoneState getState() {
        return state;
    }

    public void setState(MilestoneState state) {
        this.state = state;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public ProjectDTO getProject() {
        return project;
    }

    public void setProject(ProjectDTO project) {
        this.project = project;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MilestoneDTO)) {
            return false;
        }

        MilestoneDTO milestoneDTO = (MilestoneDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, milestoneDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MilestoneDTO{" +
            "id=" + getId() +
            ", title='" + getTitle() + "'" +
            ", state='" + getState() + "'" +
            ", position=" + getPosition() +
            ", project=" + getProject() +
            "}";
    }
}
