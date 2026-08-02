package net.jojoaddison.consultancy.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * A DTO for the {@link net.jojoaddison.consultancy.domain.QuoteLine} entity.
 */
@Schema(description = "A quantity of a catalogue item on a quote (rate snapshotted at add time).")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class QuoteLineDTO implements Serializable {

    private Long id;

    @NotNull
    @Min(value = 1)
    private Integer quantity;

    @NotNull
    @DecimalMin(value = "0")
    private BigDecimal rate;

    @NotNull
    private ServiceItemDTO item;

    @NotNull
    private QuoteDTO quote;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public ServiceItemDTO getItem() {
        return item;
    }

    public void setItem(ServiceItemDTO item) {
        this.item = item;
    }

    public QuoteDTO getQuote() {
        return quote;
    }

    public void setQuote(QuoteDTO quote) {
        this.quote = quote;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof QuoteLineDTO)) {
            return false;
        }

        QuoteLineDTO quoteLineDTO = (QuoteLineDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, quoteLineDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "QuoteLineDTO{" +
            "id=" + getId() +
            ", quantity=" + getQuantity() +
            ", rate=" + getRate() +
            ", item=" + getItem() +
            ", quote=" + getQuote() +
            "}";
    }
}
