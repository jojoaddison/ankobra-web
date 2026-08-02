package net.jojoaddison.consultancy.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link net.jojoaddison.consultancy.domain.TeamMember} entity.
 */
@Schema(description = "A consultant / team member. Referenced as project lead and ticket owner.")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class TeamMemberDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 120)
    private String name;

    @Size(max = 5)
    private String initials;

    @Size(max = 80)
    private String role;

    @Size(max = 160)
    private String qualification;

    @Lob
    private String bio;

    private UserDTO user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInitials() {
        return initials;
    }

    public void setInitials(String initials) {
        this.initials = initials;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TeamMemberDTO)) {
            return false;
        }

        TeamMemberDTO teamMemberDTO = (TeamMemberDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, teamMemberDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "TeamMemberDTO{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", initials='" + getInitials() + "'" +
            ", role='" + getRole() + "'" +
            ", qualification='" + getQualification() + "'" +
            ", bio='" + getBio() + "'" +
            ", user=" + getUser() +
            "}";
    }
}
