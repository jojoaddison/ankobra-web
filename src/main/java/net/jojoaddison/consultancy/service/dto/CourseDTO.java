package net.jojoaddison.consultancy.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;
import net.jojoaddison.consultancy.domain.enumeration.DeliveryMode;

/**
 * A DTO for the {@link net.jojoaddison.consultancy.domain.Course} entity.
 */
@Schema(description = "Training course offered as part of capacity building.")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CourseDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 160)
    private String name;

    @Size(max = 300)
    private String description;

    @Min(value = 0)
    private Integer moduleCount;

    private DeliveryMode mode;

    private Boolean labBased;

    @Min(value = 0)
    private Integer enrolledCount;

    @Min(value = 0)
    @Max(value = 100)
    private Integer progress;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getModuleCount() {
        return moduleCount;
    }

    public void setModuleCount(Integer moduleCount) {
        this.moduleCount = moduleCount;
    }

    public DeliveryMode getMode() {
        return mode;
    }

    public void setMode(DeliveryMode mode) {
        this.mode = mode;
    }

    public Boolean getLabBased() {
        return labBased;
    }

    public void setLabBased(Boolean labBased) {
        this.labBased = labBased;
    }

    public Integer getEnrolledCount() {
        return enrolledCount;
    }

    public void setEnrolledCount(Integer enrolledCount) {
        this.enrolledCount = enrolledCount;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CourseDTO)) {
            return false;
        }

        CourseDTO courseDTO = (CourseDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, courseDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CourseDTO{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", description='" + getDescription() + "'" +
            ", moduleCount=" + getModuleCount() +
            ", mode='" + getMode() + "'" +
            ", labBased='" + getLabBased() + "'" +
            ", enrolledCount=" + getEnrolledCount() +
            ", progress=" + getProgress() +
            "}";
    }
}
