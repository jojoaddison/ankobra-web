package net.jojoaddison.consultancy.service;

import java.util.Optional;
import net.jojoaddison.consultancy.domain.Quote;
import net.jojoaddison.consultancy.repository.QuoteRepository;
import net.jojoaddison.consultancy.service.dto.QuoteDTO;
import net.jojoaddison.consultancy.service.mapper.QuoteMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link net.jojoaddison.consultancy.domain.Quote}.
 */
@Service
@Transactional
public class QuoteService {

    private static final Logger LOG = LoggerFactory.getLogger(QuoteService.class);

    private final QuoteRepository quoteRepository;

    private final QuoteMapper quoteMapper;

    public QuoteService(QuoteRepository quoteRepository, QuoteMapper quoteMapper) {
        this.quoteRepository = quoteRepository;
        this.quoteMapper = quoteMapper;
    }

    /**
     * Save a quote.
     *
     * @param quoteDTO the entity to save.
     * @return the persisted entity.
     */
    public QuoteDTO save(QuoteDTO quoteDTO) {
        LOG.debug("Request to save Quote : {}", quoteDTO);
        Quote quote = quoteMapper.toEntity(quoteDTO);
        quote = quoteRepository.save(quote);
        return quoteMapper.toDto(quote);
    }

    /**
     * Update a quote.
     *
     * @param quoteDTO the entity to save.
     * @return the persisted entity.
     */
    public QuoteDTO update(QuoteDTO quoteDTO) {
        LOG.debug("Request to update Quote : {}", quoteDTO);
        Quote quote = quoteMapper.toEntity(quoteDTO);
        quote = quoteRepository.save(quote);
        return quoteMapper.toDto(quote);
    }

    /**
     * Partially update a quote.
     *
     * @param quoteDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<QuoteDTO> partialUpdate(QuoteDTO quoteDTO) {
        LOG.debug("Request to partially update Quote : {}", quoteDTO);

        return quoteRepository
            .findById(quoteDTO.getId())
            .map(existingQuote -> {
                quoteMapper.partialUpdate(existingQuote, quoteDTO);

                return existingQuote;
            })
            .map(quoteRepository::save)
            .map(quoteMapper::toDto);
    }

    /**
     * Get all the quotes with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<QuoteDTO> findAllWithEagerRelationships(Pageable pageable) {
        return quoteRepository.findAllWithEagerRelationships(pageable).map(quoteMapper::toDto);
    }

    /**
     * Get one quote by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<QuoteDTO> findOne(Long id) {
        LOG.debug("Request to get Quote : {}", id);
        return quoteRepository.findOneWithEagerRelationships(id).map(quoteMapper::toDto);
    }

    /**
     * Delete the quote by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Quote : {}", id);
        quoteRepository.deleteById(id);
    }
}
