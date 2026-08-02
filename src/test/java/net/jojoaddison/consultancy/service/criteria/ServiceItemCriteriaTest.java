package net.jojoaddison.consultancy.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class ServiceItemCriteriaTest {

    @Test
    void newServiceItemCriteriaHasAllFiltersNullTest() {
        var serviceItemCriteria = new ServiceItemCriteria();
        assertThat(serviceItemCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void serviceItemCriteriaFluentMethodsCreatesFiltersTest() {
        var serviceItemCriteria = new ServiceItemCriteria();

        setAllFilters(serviceItemCriteria);

        assertThat(serviceItemCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void serviceItemCriteriaCopyCreatesNullFilterTest() {
        var serviceItemCriteria = new ServiceItemCriteria();
        var copy = serviceItemCriteria.copy();

        assertThat(serviceItemCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(serviceItemCriteria)
        );
    }

    @Test
    void serviceItemCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var serviceItemCriteria = new ServiceItemCriteria();
        setAllFilters(serviceItemCriteria);

        var copy = serviceItemCriteria.copy();

        assertThat(serviceItemCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(serviceItemCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var serviceItemCriteria = new ServiceItemCriteria();

        assertThat(serviceItemCriteria).hasToString("ServiceItemCriteria{}");
    }

    private static void setAllFilters(ServiceItemCriteria serviceItemCriteria) {
        serviceItemCriteria.id();
        serviceItemCriteria.code();
        serviceItemCriteria.name();
        serviceItemCriteria.description();
        serviceItemCriteria.rate();
        serviceItemCriteria.unit();
        serviceItemCriteria.serviceGroup();
        serviceItemCriteria.distinct();
    }

    private static Condition<ServiceItemCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getCode()) &&
                condition.apply(criteria.getName()) &&
                condition.apply(criteria.getDescription()) &&
                condition.apply(criteria.getRate()) &&
                condition.apply(criteria.getUnit()) &&
                condition.apply(criteria.getServiceGroup()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<ServiceItemCriteria> copyFiltersAre(ServiceItemCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getCode(), copy.getCode()) &&
                condition.apply(criteria.getName(), copy.getName()) &&
                condition.apply(criteria.getDescription(), copy.getDescription()) &&
                condition.apply(criteria.getRate(), copy.getRate()) &&
                condition.apply(criteria.getUnit(), copy.getUnit()) &&
                condition.apply(criteria.getServiceGroup(), copy.getServiceGroup()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
