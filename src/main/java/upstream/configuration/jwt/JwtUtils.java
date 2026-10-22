package upstream.configuration.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import upstream.configuration.services.UserDetailsImpl;
import upstream.user.Authority;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JwtUtils {
	private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

	@Value("${dp1.game.app.jwtSecret}")
	private String jwtSecret;

	@Value("${dp1.game.app.jwtExpirationMs}")
	private int jwtExpirationMs;

	private SecretKey getSigningKey() {
		byte[] keyBytes = (this.jwtSecret != null ? this.jwtSecret.trim() : "").getBytes(StandardCharsets.UTF_8);
		if (keyBytes.length < 64) {
			try {
				MessageDigest md = MessageDigest.getInstance("SHA-512");
				keyBytes = md.digest(keyBytes);
			} catch (NoSuchAlgorithmException e) {
				throw new IllegalStateException("SHA-512 algorithm not available", e);
			}
		}
		return Keys.hmacShaKeyFor(keyBytes);
	}

	public String generateJwtToken(Authentication authentication) {
		UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
		Map<String, Object> claims = new HashMap<>();
		claims.put("authorities",
				userPrincipal.getAuthorities().stream().map(auth -> auth.getAuthority()).toList());

		return Jwts.builder()
				.claims(claims)
				.subject(userPrincipal.getUsername())
				.issuedAt(Date.from(Instant.now()))
				.expiration(Date.from(Instant.now().plusMillis(jwtExpirationMs)))
				.signWith(getSigningKey(), Jwts.SIG.HS512)
				.compact();
	}

	public String generateTokenFromUsername(String username, Authority authority) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("authorities", List.of(authority.name()));
		return Jwts.builder()
				.claims(claims)
				.subject(username)
				.issuedAt(Date.from(Instant.now()))
				.expiration(Date.from(Instant.now().plusMillis(jwtExpirationMs)))
				.signWith(getSigningKey(), Jwts.SIG.HS512)
				.compact();
	}

	public String getUserNameFromJwtToken(String token) {
		return Jwts.parser()
				.verifyWith(getSigningKey())
				.build()
				.parseSignedClaims(token)
				.getPayload()
				.getSubject();
	}

	public boolean validateJwtToken(String authToken) {
		if (authToken == null || authToken.trim().isEmpty()) {
			logger.error("JWT claims string is empty");
			return false;
		}

		try {
			Jwts.parser()
					.verifyWith(getSigningKey())
					.build()
					.parseSignedClaims(authToken);
			return true;
		} catch (SecurityException e) {
			logger.error("Invalid JWT signature: {}", e.getMessage());
		} catch (MalformedJwtException e) {
			logger.error("Invalid JWT token: {}", e.getMessage());
		} catch (ExpiredJwtException e) {
			logger.error("JWT token is expired: {}", e.getMessage());
		} catch (UnsupportedJwtException e) {
			logger.error("JWT token is unsupported: {}", e.getMessage());
		} catch (IllegalArgumentException e) {
			logger.error("JWT claims string is empty: {}", e.getMessage());
		}

		return false;
	}
}
