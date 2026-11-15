package kurban.softwareProducts.auth.web.response;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class RegisterResponse {
    private String accessToken;
    private String refreshToken;
}
