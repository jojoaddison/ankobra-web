package net.jojoaddison.consultancy.service;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.jojoaddison.consultancy.domain.QuoteLine;
import net.jojoaddison.consultancy.repository.QuoteLineRepository;
import net.jojoaddison.consultancy.service.dto.QuoteLineDTO;
import net.jojoaddison.consultancy.service.mapper.QuoteLineMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link net.jojoaddison.consultancy.domain.QuoteLine}.
 */
@Service
@Transactional
public class QuoteLineService {

    private static final Logger LOG = LoggerFactory.getLogger(QuoteLineService.class);

    private final QuoteLineRepository quoteLineRepository;

    private final QuoteLineMapper quoteLineMapper;

    public QuoteLineService(QuoteLineRepository quoteLineRepository, QuoteLineMapper quoteLineMapper) {
        this.quoteLineRepository = quoteLineRepository;
        this.quoteLineMapper = quoteLineMapper;
    }

    /**
     * Save a quoteLine.
     *
     * @param quoteLineDTO the entity to save.
     * @return the persisted entity.
     */
    public QuoteLineDTO save(QuoteLineDTO quoteLineDTO) {
        LOG.debug("Request to save QuoteLine : {}", quoteLineDTO);
        QuoteLine quoteLine = quoteLineMapper.toEntity(quoteLineDTO);
        quoteLine = quoteLineRepository.save(quoteLine);
        return quoteLineMapper.toDto(quoteLine);
    }

    /**
     * Update a quoteLine.
     *
     * @param quoteLineDTO the entity to save.
     * @return the persisted entity.
     */
    public QuoteLineDTO update(QuoteLineDTO quoteLineDTO) {
        LOG.debug("Request to update QuoteLine : {}", quoteLineDTO);
        QuoteLine quoteLine = quoteLineMapper.toEntity(quoteLineDTO);
        quoteLine = quoteLineRepository.save(quoteLine);
        return quoteLineMapper.toDto(quoteLine);
    }

    /**
     * Partially update a quoteLine.
     *
     * @param quoteLineDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<QuoteLineDTO> partialUpdate(QuoteLineDTO quoteLineDTO) {
        LOG.debug("Request to partially update QuoteLine : {}", quoteLineDTO);

        return quoteLineRepository
            .findById(quoteLineDTO.getId())
            .map(existingQuoteLine -> {
                quoteLineMapper.partialUpdate(existingQuoteLine, quoteLineDTO);

                return existingQuoteLine;
            })
            .map(quoteLineRepository::save)
            .map(quoteLineMapper::toDto);
    }

    /**
     * Get all the quoteLines.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<QuoteLineDTO> findAll() {
        LOG.debug("Request to get all QuoteLines");
        return quoteLineRepository.findAll().stream().map(quoteLineMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Get all the quoteLines with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<QuoteLineDTO> findAllWithEagerRelationships(Pageable pageable) {
        return quoteLineRepository.findAllWithEagerRelationships(pageable).map(quoteLineMapper::toDto);
    }

    /**
     * Get one quoteLine by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<QuoteLineDTO> findOne(Long id) {
        LOG.debug("Request to get QuoteLine : {}", id);
        return quoteLineRepository.findOneWithEagerRelationships(id).map(quoteLineMapper::toDto);
    }

    /**
     * Delete the quoteLine by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete QuoteLine : {}", id);
        quoteLineRepository.deleteById(id);
    }
}
