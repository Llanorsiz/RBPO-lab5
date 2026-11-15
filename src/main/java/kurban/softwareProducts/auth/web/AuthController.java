package kurban.softwareProducts.auth.web;

import kurban.softwareProducts.auth.service.AuthService;
import kurban.softwareProducts.auth.web.request.LoginRequest;
import kurban.softwareProducts.auth.web.request.RefreshRequest;
import kurban.softwareProducts.auth.web.request.RegisterRequest;
import kurban.softwareProducts.auth.web.response.LoginResponse;
import kurban.softwareProducts.auth.web.response.RefreshResponse;
import kurban.softwareProducts.auth.web.response.RegisterResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @GetMapping("/csrf")
    public CsrfToken getCsrfToken(CsrfToken token){
        return token;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/register")
    public RegisterResponse register(@RequestBody RegisterRequest request){
        return authService.register(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request){
        return authService.login(request);
    }

    @PostMapping
    public RefreshResponse refresh(@RequestBody RefreshRequest request){
        return authService.refresh(request);
    }
}