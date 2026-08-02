package net.jojoaddison.consultancy.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import net.jojoaddison.consultancy.domain.enumeration.DeliveryMode;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Training course offered as part of capacity building.
 */
@Entity
@Table(name = "course")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Course implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(max = 160)
    @Column(name = "name", length = 160, nullable = false)
    private String name;

    @Size(max = 300)
    @Column(name = "description", length = 300)
    private String description;

    @Min(value = 0)
    @Column(name = "module_count")
    private Integer moduleCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode")
    private DeliveryMode mode;

    @Column(name = "lab_based")
    private Boolean labBased;

    @Min(value = 0)
    @Column(name = "enrolled_count")
    private Integer enrolledCount;

    @Min(value = 0)
    @Max(value = 100)
    @Column(name = "progress")
    private Integer progress;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Course id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public Course name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public Course description(String description) {
        this.setDescription(description);
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getModuleCount() {
        return this.moduleCount;
    }

    public Course moduleCount(Integer moduleCount) {
        this.setModuleCount(moduleCount);
        return this;
    }

    public void setModuleCount(Integer moduleCount) {
        this.moduleCount = moduleCount;
    }

    public DeliveryMode getMode() {
        return this.mode;
    }

    public Course mode(DeliveryMode mode) {
        this.setMode(mode);
        return this;
    }

    public void setMode(DeliveryMode mode) {
        this.mode = mode;
    }

    public Boolean getLabBased() {
        return this.labBased;
    }

    public Course labBased(Boolean labBased) {
        this.setLabBased(labBased);
        return this;
    }

    public void setLabBased(Boolean labBased) {
        this.labBased = labBased;
    }

    public Integer getEnrolledCount() {
        return this.enrolledCount;
    }

    public Course enrolledCount(Integer enrolledCount) {
        this.setEnrolledCount(enrolledCount);
        return this;
    }

    public void setEnrolledCount(Integer enrolledCount) {
        this.enrolledCount = enrolledCount;
    }

    public Integer getProgress() {
        return this.progress;
    }

    public Course progress(Integer progress) {
        this.setProgress(progress);
        return this;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Course)) {
            return false;
        }
        return getId() != null && getId().equals(((Course) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Course{" +
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
