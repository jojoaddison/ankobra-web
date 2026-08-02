package net.jojoaddison.consultancy.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class ClientCriteriaTest {

    @Test
    void newClientCriteriaHasAllFiltersNullTest() {
        var clientCriteria = new ClientCriteria();
        assertThat(clientCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void clientCriteriaFluentMethodsCreatesFiltersTest() {
        var clientCriteria = new ClientCriteria();

        setAllFilters(clientCriteria);

        assertThat(clientCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void clientCriteriaCopyCreatesNullFilterTest() {
        var clientCriteria = new ClientCriteria();
        var copy = clientCriteria.copy();

        assertThat(clientCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(clientCriteria)
        );
    }

    @Test
    void clientCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var clientCriteria = new ClientCriteria();
        setAllFilters(clientCriteria);

        var copy = clientCriteria.copy();

        assertThat(clientCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(clientCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var clientCriteria = new ClientCriteria();

        assertThat(clientCriteria).hasToString("ClientCriteria{}");
    }

    private static void setAllFilters(ClientCriteria clientCriteria) {
        clientCriteria.id();
        clientCriteria.name();
        clientCriteria.sector();
        clientCriteria.clientSince();
        clientCriteria.health();
        clientCriteria.totalSpend();
        clientCriteria.userId();
        clientCriteria.projectId();
        clientCriteria.ticketId();
        clientCriteria.quoteId();
        clientCriteria.distinct();
    }

    private static Condition<ClientCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getName()) &&
                condition.apply(criteria.getSector()) &&
                condition.apply(criteria.getClientSince()) &&
                condition.apply(criteria.getHealth()) &&
                condition.apply(criteria.getTotalSpend()) &&
                condition.apply(criteria.getUserId()) &&
                condition.apply(criteria.getProjectId()) &&
                condition.apply(criteria.getTicketId()) &&
                condition.apply(criteria.getQuoteId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<ClientCriteria> copyFiltersAre(ClientCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getName(), copy.getName()) &&
                condition.apply(criteria.getSector(), copy.getSector()) &&
                condition.apply(criteria.getClientSince(), copy.getClientSince()) &&
                condition.apply(criteria.getHealth(), copy.getHealth()) &&
                condition.apply(criteria.getTotalSpend(), copy.getTotalSpend()) &&
                condition.apply(criteria.getUserId(), copy.getUserId()) &&
                condition.apply(criteria.getProjectId(), copy.getProjectId()) &&
                condition.apply(criteria.getTicketId(), copy.getTicketId()) &&
                condition.apply(criteria.getQuoteId(), copy.getQuoteId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
