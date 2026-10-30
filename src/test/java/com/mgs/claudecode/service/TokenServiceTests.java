package com.mgs.claudecode.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.List;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import com.mgs.claudecode.config.AppSecurityProperties;
import com.mgs.claudecode.config.SecurityConfig;
import com.mgs.claudecode.dto.TokenResponse;

class TokenServiceTests {

	private static final AppSecurityProperties PROPERTIES = properties("test-secret-0123456789abcdef0123456789",
			"test-issuer");

	private final SecurityConfig config = new SecurityConfig();

	private final SecretKey key = config.jwtSecretKey(PROPERTIES);

	private final TokenService service = new TokenService(config.jwtEncoder(key), PROPERTIES);

	private final JwtDecoder decoder = config.jwtDecoder(key, PROPERTIES);

	@Test
	void issuesSignedTokenWithSubjectRolesAndExpiry() {
		TokenResponse response = service.issue(UsernamePasswordAuthenticationToken.authenticated("admin", null,
				AuthorityUtils.createAuthorityList("ROLE_ADMIN")));

		assertThat(response.tokenType()).isEqualTo("Bearer");
		assertThat(response.expiresIn()).isEqualTo(900);
		Jwt jwt = decoder.decode(response.accessToken());
		assertThat(jwt.getSubject()).isEqualTo("admin");
		assertThat(jwt.getClaimAsString("iss")).isEqualTo("test-issuer");
		assertThat(jwt.getClaimAsStringList("roles")).containsExactly("ADMIN");
		assertThat(Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt())).isEqualTo(Duration.ofMinutes(15));
	}

	@Test
	void dropsNonRoleAuthorities() {
		TokenResponse response = service.issue(UsernamePasswordAuthenticationToken.authenticated("user", null,
				AuthorityUtils.createAuthorityList("ROLE_USER", "SCOPE_read")));

		assertThat(decoder.decode(response.accessToken()).getClaimAsStringList("roles")).containsExactly("USER");
	}

	@Test
	void tokenSignedWithAnotherSecretIsRejected() {
		AppSecurityProperties other = properties("another-secret-0123456789abcdef01234567", "test-issuer");
		SecretKey otherKey = config.jwtSecretKey(other);
		String token = new TokenService(config.jwtEncoder(otherKey), other)
			.issue(UsernamePasswordAuthenticationToken.authenticated("admin", null,
					AuthorityUtils.createAuthorityList("ROLE_ADMIN")))
			.accessToken();

		assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
	}

	@Test
	void tokenFromAnotherIssuerIsRejected() {
		AppSecurityProperties other = properties("test-secret-0123456789abcdef0123456789", "other-issuer");
		String token = new TokenService(config.jwtEncoder(key), other)
			.issue(UsernamePasswordAuthenticationToken.authenticated("admin", null,
					AuthorityUtils.createAuthorityList("ROLE_ADMIN")))
			.accessToken();

		assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
	}

	@Test
	void rejectsShortSecret() {
		assertThatThrownBy(() -> new AppSecurityProperties.Jwt("too-short", "issuer", Duration.ofHours(1)))
			.isInstanceOf(IllegalArgumentException.class);
	}

	private static AppSecurityProperties properties(String secret, String issuer) {
		return new AppSecurityProperties(new AppSecurityProperties.Jwt(secret, issuer, Duration.ofMinutes(15)),
				List.of());
	}

}
