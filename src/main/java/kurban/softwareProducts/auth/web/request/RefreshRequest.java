package kurban.softwareProducts.auth.web.request;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class RefreshRequest {
    private String refreshToken;
}
