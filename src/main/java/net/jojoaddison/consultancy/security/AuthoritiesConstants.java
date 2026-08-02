package net.jojoaddison.consultancy.security;

/**
 * Constants for Spring Security authorities.
 */
public final class AuthoritiesConstants {

    public static final String ADMIN = "ROLE_ADMIN";

    public static final String USER = "ROLE_USER";

    /** Jojo Addison staff. Consultants see all clients' delivery data; plain users (clients) are scoped to their own. */
    public static final String CONSULTANT = "ROLE_CONSULTANT";

    public static final String ANONYMOUS = "ROLE_ANONYMOUS";

    private AuthoritiesConstants() {}
}
