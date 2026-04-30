package com.homeprotectors.backend.service;

import com.homeprotectors.backend.config.JwtProperties;
import com.homeprotectors.backend.dto.auth.AuthTokenResponse;
import com.homeprotectors.backend.entity.User;
import com.homeprotectors.backend.exception.ApiException;
import com.homeprotectors.backend.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Service
public class JwtTokenService {

    private static final String CLAIM_TOKEN_TYPE = "tokenType";
    private static final String CLAIM_TOKEN_VERSION = "tokenVersion";
    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";

    private final JwtProperties jwtProperties;
    private final UserRepository userRepository;
    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public JwtTokenService(JwtProperties jwtProperties, UserRepository userRepository) {
        this.jwtProperties = jwtProperties;
        this.userRepository = userRepository;
        this.privateKey = loadPrivateKey(jwtProperties.privateKeyPemPath());
        this.publicKey = loadPublicKey(jwtProperties.publicKeyPemPath());
    }

    public AuthTokenResponse issueTokens(UUID userId) {
        User user = requireUser(userId);
        return new AuthTokenResponse(
                userId.toString(),
                createToken(userId, user.getTokenVersion(), TOKEN_TYPE_ACCESS, Instant.now().plus(jwtProperties.accessTtlMin(), ChronoUnit.MINUTES)),
                createToken(userId, user.getTokenVersion(), TOKEN_TYPE_REFRESH, Instant.now().plus(jwtProperties.refreshTtlDays(), ChronoUnit.DAYS)),
                "Bearer",
                jwtProperties.accessTtlMin() * 60
        );
    }

    public UUID parseAccessToken(String token) {
        return parseToken(token, TOKEN_TYPE_ACCESS);
    }

    public AuthTokenResponse refresh(String refreshToken) {
        UUID userId = parseToken(refreshToken, TOKEN_TYPE_REFRESH);
        return issueTokens(userId);
    }

    private UUID parseToken(String token, String expectedTokenType) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .requireIssuer(jwtProperties.iss())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Set<String> audiences = claims.getAudience();
            if (audiences == null || !audiences.contains(jwtProperties.aud())) {
                throw new ApiException("INVALID_TOKEN_AUDIENCE", "Token audience is invalid.");
            }

            String tokenType = claims.get(CLAIM_TOKEN_TYPE, String.class);
            if (!expectedTokenType.equals(tokenType)) {
                throw new ApiException("INVALID_TOKEN_TYPE", "Token type is invalid.");
            }

            UUID userId = UUID.fromString(claims.getSubject());
            User user = requireUser(userId);
            Number tokenVersion = claims.get(CLAIM_TOKEN_VERSION, Number.class);
            if (tokenVersion == null) {
                throw new ApiException("INVALID_TOKEN_VERSION", "Token version is missing.");
            }
            if (tokenVersion.longValue() != user.getTokenVersion()) {
                throw new ApiException("STALE_TOKEN", "Token is no longer valid.");
            }

            return userId;
        } catch (ApiException e) {
            throw e;
        } catch (SignatureException e) {
            throw new ApiException("INVALID_TOKEN_SIGNATURE", "Token signature is invalid.");
        } catch (JwtException | IllegalArgumentException e) {
            throw new ApiException("INVALID_TOKEN", "Token is invalid or expired.");
        }
    }

    private String createToken(UUID userId, Long tokenVersion, String tokenType, Instant expiresAt) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(userId.toString())
                .issuer(jwtProperties.iss())
                .audience().add(jwtProperties.aud()).and()
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .claim(CLAIM_TOKEN_TYPE, tokenType)
                .claim(CLAIM_TOKEN_VERSION, tokenVersion)
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    private User requireUser(UUID userId) {
        return userRepository.findByPublicId(userId)
                .orElseThrow(() -> new ApiException("USER_NOT_FOUND", "User not found."));
    }

    private PrivateKey loadPrivateKey(String pemPath) {
        try {
            byte[] der = decodePem(Path.of(pemPath), "PRIVATE KEY");
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(der));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load JWT private key.", e);
        }
    }

    private PublicKey loadPublicKey(String pemPath) {
        try {
            byte[] der = decodePem(Path.of(pemPath), "PUBLIC KEY");
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(der));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load JWT public key.", e);
        }
    }

    private byte[] decodePem(Path path, String keyType) throws Exception {
        String pem = Files.readString(path);
        String normalized = pem
                .replace("-----BEGIN " + keyType + "-----", "")
                .replace("-----END " + keyType + "-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(normalized);
    }
}
