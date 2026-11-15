package kurban.softwareProducts.auth.service;

import kurban.softwareProducts.auth.service.TokenService.TokenPair;
import kurban.softwareProducts.auth.web.request.LoginRequest;
import kurban.softwareProducts.auth.web.request.RefreshRequest;
import kurban.softwareProducts.auth.web.request.RegisterRequest;
import kurban.softwareProducts.auth.web.response.LoginResponse;
import kurban.softwareProducts.auth.web.response.RefreshResponse;
import kurban.softwareProducts.auth.web.response.RegisterResponse;
import kurban.softwareProducts.common.exception.base.BadRequestException;
import kurban.softwareProducts.common.exception.base.ConflictException;
import kurban.softwareProducts.common.exception.base.UnauthorizedException;
import kurban.softwareProducts.user.mapper.UserMapper;
import kurban.softwareProducts.user.model.User;
import kurban.softwareProducts.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final UserMapper userMapper;
    private final TokenService tokenService;

    @Transactional
    public RegisterResponse register(RegisterRequest request){
        if(!StringUtils.hasText(request.getUsername())){
            throw new BadRequestException("Имя пользователя пустое");
        }
        if(!StringUtils.hasText((request.getPassword()))){
            throw new BadRequestException("Пароль пустой");
        }
        if(userRepository.existsByUsername(request.getUsername())){
            throw new ConflictException("Имя уже существует");
        }
        if(request.getPassword().length()<8){
            throw new BadRequestException("Длина пароля не подходит");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(encoder.encode(request.getPassword()));
        userRepository.save(user);

        TokenPair tokenPair = tokenService.issueTokens(user.getUsername(), request.getDeviceId());

        return RegisterResponse.builder()
                .accessToken(tokenPair.accessToken())
                .refreshToken(tokenPair.refreshToken())
                .build();

    }

    @Transactional
    public LoginResponse login(LoginRequest request){
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UnauthorizedException("Неверные данные для входа"));
        if(!encoder.matches(request.getPassword(), user.getPassword())){
            throw new UnauthorizedException("Неверные данные для входа");
        }

        TokenPair tokenPair = tokenService.issueTokens(user.getUsername(), request.getDeviceId());

        return LoginResponse.builder()
                .accessToken(tokenPair.accessToken())
                .refreshToken(tokenPair.refreshToken())
                .build();
    }

    @Transactional
    public RefreshResponse refresh(RefreshRequest request){
        TokenPair tokenPair = tokenService.refreshTokens(request.getRefreshToken());

        return RefreshResponse.builder()
                .accessToken(tokenPair.accessToken())
                .refreshToken(tokenPair.refreshToken())
                .build();
    }
}
