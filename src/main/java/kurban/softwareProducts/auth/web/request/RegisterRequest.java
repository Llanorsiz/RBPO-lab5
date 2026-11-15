package kurban.softwareProducts.auth.web.request;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class RegisterRequest {
    private String username;
    private String password;
    private String deviceId;
}
