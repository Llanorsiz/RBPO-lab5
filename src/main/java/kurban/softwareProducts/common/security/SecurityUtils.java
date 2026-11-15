package kurban.softwareProducts.common.security;

import kurban.softwareProducts.auth.AuthUserDetails;
import kurban.softwareProducts.common.exception.base.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
    public static AuthUserDetails getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof AuthUserDetails)) {
            throw new UnauthorizedException();
        }
        return (AuthUserDetails) auth.getPrincipal();
    }

    public static Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public static boolean isAdmin() {
        return "ADMIN".equals(getCurrentUser().getRole());
    }
}
