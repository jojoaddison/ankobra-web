package net.jojoaddison.consultancy.service;

import java.util.Optional;
import net.jojoaddison.consultancy.domain.ServiceItem;
import net.jojoaddison.consultancy.repository.ServiceItemRepository;
import net.jojoaddison.consultancy.service.dto.ServiceItemDTO;
import net.jojoaddison.consultancy.service.mapper.ServiceItemMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link net.jojoaddison.consultancy.domain.ServiceItem}.
 */
@Service
@Transactional
public class ServiceItemService {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceItemService.class);

    private final ServiceItemRepository serviceItemRepository;

    private final ServiceItemMapper serviceItemMapper;

    public ServiceItemService(ServiceItemRepository serviceItemRepository, ServiceItemMapper serviceItemMapper) {
        this.serviceItemRepository = serviceItemRepository;
        this.serviceItemMapper = serviceItemMapper;
    }

    /**
     * Save a serviceItem.
     *
     * @param serviceItemDTO the entity to save.
     * @return the persisted entity.
     */
    public ServiceItemDTO save(ServiceItemDTO serviceItemDTO) {
        LOG.debug("Request to save ServiceItem : {}", serviceItemDTO);
        ServiceItem serviceItem = serviceItemMapper.toEntity(serviceItemDTO);
        serviceItem = serviceItemRepository.save(serviceItem);
        return serviceItemMapper.toDto(serviceItem);
    }

    /**
     * Update a serviceItem.
     *
     * @param serviceItemDTO the entity to save.
     * @return the persisted entity.
     */
    public ServiceItemDTO update(ServiceItemDTO serviceItemDTO) {
        LOG.debug("Request to update ServiceItem : {}", serviceItemDTO);
        ServiceItem serviceItem = serviceItemMapper.toEntity(serviceItemDTO);
        serviceItem = serviceItemRepository.save(serviceItem);
        return serviceItemMapper.toDto(serviceItem);
    }

    /**
     * Partially update a serviceItem.
     *
     * @param serviceItemDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<ServiceItemDTO> partialUpdate(ServiceItemDTO serviceItemDTO) {
        LOG.debug("Request to partially update ServiceItem : {}", serviceItemDTO);

        return serviceItemRepository
            .findById(serviceItemDTO.getId())
            .map(existingServiceItem -> {
                serviceItemMapper.partialUpdate(existingServiceItem, serviceItemDTO);

                return existingServiceItem;
            })
            .map(serviceItemRepository::save)
            .map(serviceItemMapper::toDto);
    }

    /**
     * Get one serviceItem by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<ServiceItemDTO> findOne(Long id) {
        LOG.debug("Request to get ServiceItem : {}", id);
        return serviceItemRepository.findById(id).map(serviceItemMapper::toDto);
    }

    /**
     * Delete the serviceItem by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete ServiceItem : {}", id);
        serviceItemRepository.deleteById(id);
    }
}
