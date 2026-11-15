package kurban.softwareProducts.auth.service;

import io.jsonwebtoken.Claims;
import kurban.softwareProducts.common.exception.base.UnauthorizedException;
import kurban.softwareProducts.user.model.User;
import kurban.softwareProducts.user.repository.UserRepository;
import kurban.softwareProducts.userSession.model.SessionStatus;
import kurban.softwareProducts.userSession.model.UserSession;
import kurban.softwareProducts.userSession.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;

    @Transactional
    public TokenPair issueTokens(String username, String deviceId){
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("Пользователь не найден"));

        String accessToken = jwtService.generateAccessToken(username, user.getRole().name(), user.getId());
        String refreshToken = jwtService.generateRefreshToken(username);

        Instant accessExp = jwtService.extract(accessToken, "access")
                .getExpiration()
                .toInstant();

        Instant refreshExp = jwtService.extract(refreshToken, "refresh")
                .getExpiration()
                .toInstant();

        UserSession session = UserSession.builder()
                .username(username)
                .deviceId(deviceId)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiry(accessExp)
                .refreshTokenExpiry(refreshExp)
                .status(SessionStatus.ACTIVE)
                .build();
        userSessionRepository.save(session);
        return new TokenPair(accessToken, refreshToken);
    }

    @Transactional
    public TokenPair refreshTokens(String refreshToken){
        Claims claims = jwtService.extract(refreshToken, "refresh");
        String username = claims.getSubject();

        UserSession session = userSessionRepository.findByRefreshToken(refreshToken)
                .orElseThrow(UnauthorizedException::new);
        if(session.getStatus() != SessionStatus.ACTIVE){
            if(session.getStatus() == SessionStatus.USED){
                session.setStatus(SessionStatus.REVOKED);
                userSessionRepository.save(session);
            }
            throw new UnauthorizedException("Некорректный статус сессии");
        }
        if(session.getRefreshTokenExpiry().isBefore(Instant.now())){
            throw new UnauthorizedException("Токен обовления истёк");
        }
        if(!session.getUsername().equals(username)){
            throw new UnauthorizedException("Несовпадение владельца токена и сессии");
        }
        session.setStatus(SessionStatus.USED);
        userSessionRepository.save(session);

        return issueTokens(username, session.getDeviceId());
    }

    public record TokenPair(String accessToken, String refreshToken){}
}
