package com.mgs.claudecode.service;

import java.time.Instant;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.mgs.claudecode.config.AppSecurityProperties;
import com.mgs.claudecode.config.SecurityConfig;
import com.mgs.claudecode.dto.TokenResponse;

@Service
public class TokenService {

	private static final String ROLE_PREFIX = "ROLE_";

	private final JwtEncoder encoder;

	private final AppSecurityProperties.Jwt properties;

	public TokenService(JwtEncoder encoder, AppSecurityProperties properties) {
		this.encoder = encoder;
		this.properties = properties.jwt();
	}

	public TokenResponse issue(Authentication authentication) {
		Instant now = Instant.now();
		List<String> roles = authentication.getAuthorities()
			.stream()
			.map(GrantedAuthority::getAuthority)
			.filter(authority -> authority.startsWith(ROLE_PREFIX))
			.map(authority -> authority.substring(ROLE_PREFIX.length()))
			.toList();
		JwtClaimsSet claims = JwtClaimsSet.builder()
			.issuer(properties.issuer())
			.issuedAt(now)
			.expiresAt(now.plus(properties.expiration()))
			.subject(authentication.getName())
			.claim(SecurityConfig.ROLES_CLAIM, roles)
			.build();
		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
		return new TokenResponse(token, "Bearer", properties.expiration().toSeconds());
	}

}
