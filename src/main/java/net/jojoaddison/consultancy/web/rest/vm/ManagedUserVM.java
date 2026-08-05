package net.jojoaddison.consultancy.web.rest.vm;

import jakarta.validation.constraints.Size;
import net.jojoaddison.consultancy.service.dto.AdminUserDTO;

/**
 * View Model extending the AdminUserDTO, which is meant to be used in the user management UI.
 */
public class ManagedUserVM extends AdminUserDTO {

    /**
     * SEC-04 in docs/security-20260805-0936.md. Was generator-jhipster's default of 4, which is
     * exhaustible in seconds against an endpoint that had no throttling. Raising this only governs
     * passwords set from now on — any account created under the old floor keeps its short password until
     * it is reset, so a forced reset is part of applying this, not a consequence of it.
     */
    public static final int PASSWORD_MIN_LENGTH = 12;

    public static final int PASSWORD_MAX_LENGTH = 100;

    @Size(min = PASSWORD_MIN_LENGTH, max = PASSWORD_MAX_LENGTH)
    private String password;

    public ManagedUserVM() {
        // Empty constructor needed for Jackson.
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ManagedUserVM{" + super.toString() + "} ";
    }
}
