package net.jojoaddison.consultancy.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class CourseCriteriaTest {

    @Test
    void newCourseCriteriaHasAllFiltersNullTest() {
        var courseCriteria = new CourseCriteria();
        assertThat(courseCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void courseCriteriaFluentMethodsCreatesFiltersTest() {
        var courseCriteria = new CourseCriteria();

        setAllFilters(courseCriteria);

        assertThat(courseCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void courseCriteriaCopyCreatesNullFilterTest() {
        var courseCriteria = new CourseCriteria();
        var copy = courseCriteria.copy();

        assertThat(courseCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(courseCriteria)
        );
    }

    @Test
    void courseCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var courseCriteria = new CourseCriteria();
        setAllFilters(courseCriteria);

        var copy = courseCriteria.copy();

        assertThat(courseCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(courseCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var courseCriteria = new CourseCriteria();

        assertThat(courseCriteria).hasToString("CourseCriteria{}");
    }

    private static void setAllFilters(CourseCriteria courseCriteria) {
        courseCriteria.id();
        courseCriteria.name();
        courseCriteria.description();
        courseCriteria.moduleCount();
        courseCriteria.mode();
        courseCriteria.labBased();
        courseCriteria.enrolledCount();
        courseCriteria.progress();
        courseCriteria.distinct();
    }

    private static Condition<CourseCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getName()) &&
                condition.apply(criteria.getDescription()) &&
                condition.apply(criteria.getModuleCount()) &&
                condition.apply(criteria.getMode()) &&
                condition.apply(criteria.getLabBased()) &&
                condition.apply(criteria.getEnrolledCount()) &&
                condition.apply(criteria.getProgress()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<CourseCriteria> copyFiltersAre(CourseCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getName(), copy.getName()) &&
                condition.apply(criteria.getDescription(), copy.getDescription()) &&
                condition.apply(criteria.getModuleCount(), copy.getModuleCount()) &&
                condition.apply(criteria.getMode(), copy.getMode()) &&
                condition.apply(criteria.getLabBased(), copy.getLabBased()) &&
                condition.apply(criteria.getEnrolledCount(), copy.getEnrolledCount()) &&
                condition.apply(criteria.getProgress(), copy.getProgress()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
