package net.jojoaddison.consultancy.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A consultant / team member. Referenced as project lead and ticket owner.
 */
@Entity
@Table(name = "team_member")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class TeamMember implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(max = 120)
    @Column(name = "name", length = 120, nullable = false)
    private String name;

    @Size(max = 5)
    @Column(name = "initials", length = 5)
    private String initials;

    @Size(max = 80)
    @Column(name = "role", length = 80)
    private String role;

    @Size(max = 160)
    @Column(name = "qualification", length = 160)
    private String qualification;

    @Lob
    @Column(name = "bio")
    private String bio;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(unique = true)
    private User user;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public TeamMember id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public TeamMember name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInitials() {
        return this.initials;
    }

    public TeamMember initials(String initials) {
        this.setInitials(initials);
        return this;
    }

    public void setInitials(String initials) {
        this.initials = initials;
    }

    public String getRole() {
        return this.role;
    }

    public TeamMember role(String role) {
        this.setRole(role);
        return this;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getQualification() {
        return this.qualification;
    }

    public TeamMember qualification(String qualification) {
        this.setQualification(qualification);
        return this;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public String getBio() {
        return this.bio;
    }

    public TeamMember bio(String bio) {
        this.setBio(bio);
        return this;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public TeamMember user(User user) {
        this.setUser(user);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TeamMember)) {
            return false;
        }
        return getId() != null && getId().equals(((TeamMember) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "TeamMember{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", initials='" + getInitials() + "'" +
            ", role='" + getRole() + "'" +
            ", qualification='" + getQualification() + "'" +
            ", bio='" + getBio() + "'" +
            "}";
    }
}
