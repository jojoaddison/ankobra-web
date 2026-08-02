package net.jojoaddison.consultancy.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class ProjectCriteriaTest {

    @Test
    void newProjectCriteriaHasAllFiltersNullTest() {
        var projectCriteria = new ProjectCriteria();
        assertThat(projectCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void projectCriteriaFluentMethodsCreatesFiltersTest() {
        var projectCriteria = new ProjectCriteria();

        setAllFilters(projectCriteria);

        assertThat(projectCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void projectCriteriaCopyCreatesNullFilterTest() {
        var projectCriteria = new ProjectCriteria();
        var copy = projectCriteria.copy();

        assertThat(projectCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(projectCriteria)
        );
    }

    @Test
    void projectCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var projectCriteria = new ProjectCriteria();
        setAllFilters(projectCriteria);

        var copy = projectCriteria.copy();

        assertThat(projectCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(projectCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var projectCriteria = new ProjectCriteria();

        assertThat(projectCriteria).hasToString("ProjectCriteria{}");
    }

    private static void setAllFilters(ProjectCriteria projectCriteria) {
        projectCriteria.id();
        projectCriteria.reference();
        projectCriteria.name();
        projectCriteria.pillar();
        projectCriteria.status();
        projectCriteria.progress();
        projectCriteria.dueDate();
        projectCriteria.delivered();
        projectCriteria.budget();
        projectCriteria.spent();
        projectCriteria.techStack();
        projectCriteria.milestoneId();
        projectCriteria.leadId();
        projectCriteria.clientId();
        projectCriteria.distinct();
    }

    private static Condition<ProjectCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getReference()) &&
                condition.apply(criteria.getName()) &&
                condition.apply(criteria.getPillar()) &&
                condition.apply(criteria.getStatus()) &&
                condition.apply(criteria.getProgress()) &&
                condition.apply(criteria.getDueDate()) &&
                condition.apply(criteria.getDelivered()) &&
                condition.apply(criteria.getBudget()) &&
                condition.apply(criteria.getSpent()) &&
                condition.apply(criteria.getTechStack()) &&
                condition.apply(criteria.getMilestoneId()) &&
                condition.apply(criteria.getLeadId()) &&
                condition.apply(criteria.getClientId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<ProjectCriteria> copyFiltersAre(ProjectCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getReference(), copy.getReference()) &&
                condition.apply(criteria.getName(), copy.getName()) &&
                condition.apply(criteria.getPillar(), copy.getPillar()) &&
                condition.apply(criteria.getStatus(), copy.getStatus()) &&
                condition.apply(criteria.getProgress(), copy.getProgress()) &&
                condition.apply(criteria.getDueDate(), copy.getDueDate()) &&
                condition.apply(criteria.getDelivered(), copy.getDelivered()) &&
                condition.apply(criteria.getBudget(), copy.getBudget()) &&
                condition.apply(criteria.getSpent(), copy.getSpent()) &&
                condition.apply(criteria.getTechStack(), copy.getTechStack()) &&
                condition.apply(criteria.getMilestoneId(), copy.getMilestoneId()) &&
                condition.apply(criteria.getLeadId(), copy.getLeadId()) &&
                condition.apply(criteria.getClientId(), copy.getClientId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
