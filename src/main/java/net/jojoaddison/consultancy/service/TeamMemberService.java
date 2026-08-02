package net.jojoaddison.consultancy.service;

import java.util.Optional;
import net.jojoaddison.consultancy.domain.TeamMember;
import net.jojoaddison.consultancy.repository.TeamMemberRepository;
import net.jojoaddison.consultancy.service.dto.TeamMemberDTO;
import net.jojoaddison.consultancy.service.mapper.TeamMemberMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link net.jojoaddison.consultancy.domain.TeamMember}.
 */
@Service
@Transactional
public class TeamMemberService {

    private static final Logger LOG = LoggerFactory.getLogger(TeamMemberService.class);

    private final TeamMemberRepository teamMemberRepository;

    private final TeamMemberMapper teamMemberMapper;

    public TeamMemberService(TeamMemberRepository teamMemberRepository, TeamMemberMapper teamMemberMapper) {
        this.teamMemberRepository = teamMemberRepository;
        this.teamMemberMapper = teamMemberMapper;
    }

    /**
     * Save a teamMember.
     *
     * @param teamMemberDTO the entity to save.
     * @return the persisted entity.
     */
    public TeamMemberDTO save(TeamMemberDTO teamMemberDTO) {
        LOG.debug("Request to save TeamMember : {}", teamMemberDTO);
        TeamMember teamMember = teamMemberMapper.toEntity(teamMemberDTO);
        teamMember = teamMemberRepository.save(teamMember);
        return teamMemberMapper.toDto(teamMember);
    }

    /**
     * Update a teamMember.
     *
     * @param teamMemberDTO the entity to save.
     * @return the persisted entity.
     */
    public TeamMemberDTO update(TeamMemberDTO teamMemberDTO) {
        LOG.debug("Request to update TeamMember : {}", teamMemberDTO);
        TeamMember teamMember = teamMemberMapper.toEntity(teamMemberDTO);
        teamMember = teamMemberRepository.save(teamMember);
        return teamMemberMapper.toDto(teamMember);
    }

    /**
     * Partially update a teamMember.
     *
     * @param teamMemberDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<TeamMemberDTO> partialUpdate(TeamMemberDTO teamMemberDTO) {
        LOG.debug("Request to partially update TeamMember : {}", teamMemberDTO);

        return teamMemberRepository
            .findById(teamMemberDTO.getId())
            .map(existingTeamMember -> {
                teamMemberMapper.partialUpdate(existingTeamMember, teamMemberDTO);

                return existingTeamMember;
            })
            .map(teamMemberRepository::save)
            .map(teamMemberMapper::toDto);
    }

    /**
     * Get all the teamMembers.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<TeamMemberDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all TeamMembers");
        return teamMemberRepository.findAll(pageable).map(teamMemberMapper::toDto);
    }

    /**
     * Get all the teamMembers with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<TeamMemberDTO> findAllWithEagerRelationships(Pageable pageable) {
        return teamMemberRepository.findAllWithEagerRelationships(pageable).map(teamMemberMapper::toDto);
    }

    /**
     * Get one teamMember by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<TeamMemberDTO> findOne(Long id) {
        LOG.debug("Request to get TeamMember : {}", id);
        return teamMemberRepository.findOneWithEagerRelationships(id).map(teamMemberMapper::toDto);
    }

    /**
     * Delete the teamMember by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete TeamMember : {}", id);
        teamMemberRepository.deleteById(id);
    }
}
