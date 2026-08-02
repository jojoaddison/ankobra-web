package net.jojoaddison.consultancy.service.criteria;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import net.jojoaddison.consultancy.domain.enumeration.DeliveryMode;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link net.jojoaddison.consultancy.domain.Course} entity. This class is used
 * in {@link net.jojoaddison.consultancy.web.rest.CourseResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /courses?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CourseCriteria implements Serializable, Criteria {

    /**
     * Class for filtering DeliveryMode
     */
    public static class DeliveryModeFilter extends Filter<DeliveryMode> {

        public DeliveryModeFilter() {}

        public DeliveryModeFilter(DeliveryModeFilter filter) {
            super(filter);
        }

        @Override
        public DeliveryModeFilter copy() {
            return new DeliveryModeFilter(this);
        }
    }

    @Serial
    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter name;

    private StringFilter description;

    private IntegerFilter moduleCount;

    private DeliveryModeFilter mode;

    private BooleanFilter labBased;

    private IntegerFilter enrolledCount;

    private IntegerFilter progress;

    private Boolean distinct;

    public CourseCriteria() {}

    public CourseCriteria(CourseCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.name = other.optionalName().map(StringFilter::copy).orElse(null);
        this.description = other.optionalDescription().map(StringFilter::copy).orElse(null);
        this.moduleCount = other.optionalModuleCount().map(IntegerFilter::copy).orElse(null);
        this.mode = other.optionalMode().map(DeliveryModeFilter::copy).orElse(null);
        this.labBased = other.optionalLabBased().map(BooleanFilter::copy).orElse(null);
        this.enrolledCount = other.optionalEnrolledCount().map(IntegerFilter::copy).orElse(null);
        this.progress = other.optionalProgress().map(IntegerFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public CourseCriteria copy() {
        return new CourseCriteria(this);
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

    public IntegerFilter getModuleCount() {
        return moduleCount;
    }

    public Optional<IntegerFilter> optionalModuleCount() {
        return Optional.ofNullable(moduleCount);
    }

    public IntegerFilter moduleCount() {
        if (moduleCount == null) {
            setModuleCount(new IntegerFilter());
        }
        return moduleCount;
    }

    public void setModuleCount(IntegerFilter moduleCount) {
        this.moduleCount = moduleCount;
    }

    public DeliveryModeFilter getMode() {
        return mode;
    }

    public Optional<DeliveryModeFilter> optionalMode() {
        return Optional.ofNullable(mode);
    }

    public DeliveryModeFilter mode() {
        if (mode == null) {
            setMode(new DeliveryModeFilter());
        }
        return mode;
    }

    public void setMode(DeliveryModeFilter mode) {
        this.mode = mode;
    }

    public BooleanFilter getLabBased() {
        return labBased;
    }

    public Optional<BooleanFilter> optionalLabBased() {
        return Optional.ofNullable(labBased);
    }

    public BooleanFilter labBased() {
        if (labBased == null) {
            setLabBased(new BooleanFilter());
        }
        return labBased;
    }

    public void setLabBased(BooleanFilter labBased) {
        this.labBased = labBased;
    }

    public IntegerFilter getEnrolledCount() {
        return enrolledCount;
    }

    public Optional<IntegerFilter> optionalEnrolledCount() {
        return Optional.ofNullable(enrolledCount);
    }

    public IntegerFilter enrolledCount() {
        if (enrolledCount == null) {
            setEnrolledCount(new IntegerFilter());
        }
        return enrolledCount;
    }

    public void setEnrolledCount(IntegerFilter enrolledCount) {
        this.enrolledCount = enrolledCount;
    }

    public IntegerFilter getProgress() {
        return progress;
    }

    public Optional<IntegerFilter> optionalProgress() {
        return Optional.ofNullable(progress);
    }

    public IntegerFilter progress() {
        if (progress == null) {
            setProgress(new IntegerFilter());
        }
        return progress;
    }

    public void setProgress(IntegerFilter progress) {
        this.progress = progress;
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
        final CourseCriteria that = (CourseCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(name, that.name) &&
            Objects.equals(description, that.description) &&
            Objects.equals(moduleCount, that.moduleCount) &&
            Objects.equals(mode, that.mode) &&
            Objects.equals(labBased, that.labBased) &&
            Objects.equals(enrolledCount, that.enrolledCount) &&
            Objects.equals(progress, that.progress) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, moduleCount, mode, labBased, enrolledCount, progress, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CourseCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalName().map(f -> "name=" + f + ", ").orElse("") +
            optionalDescription().map(f -> "description=" + f + ", ").orElse("") +
            optionalModuleCount().map(f -> "moduleCount=" + f + ", ").orElse("") +
            optionalMode().map(f -> "mode=" + f + ", ").orElse("") +
            optionalLabBased().map(f -> "labBased=" + f + ", ").orElse("") +
            optionalEnrolledCount().map(f -> "enrolledCount=" + f + ", ").orElse("") +
            optionalProgress().map(f -> "progress=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
