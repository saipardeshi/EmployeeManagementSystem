package com.mainapp.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class JwtUtil {

	private static final String HMAC_SHA256 = "HmacSHA256";
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@Value("${app.jwt.secret:StaffSyncSecureSecretKeyForHmacSha256TokenValidation2026!}")
	private String secretKey;

	@Value("${app.jwt.expiration-ms:86400000}") // 24 hours
	private long expirationMs;

	public String generateToken(String username, String role) {
		try {
			long now = System.currentTimeMillis();
			long exp = now + expirationMs;

			// Header
			String headerJson = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
			String encodedHeader = base64UrlEncode(headerJson.getBytes(StandardCharsets.UTF_8));

			// Payload
			Map<String, Object> payloadMap = Map.of(
					"sub", username,
					"role", role,
					"iat", now / 1000,
					"exp", exp / 1000);
			String payloadJson = OBJECT_MAPPER.writeValueAsString(payloadMap);
			String encodedPayload = base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8));

			// Signature
			String dataToSign = encodedHeader + "." + encodedPayload;
			String signature = sign(dataToSign);

			return dataToSign + "." + signature;
		} catch (Exception ex) {
			throw new RuntimeException("Failed to generate JWT token", ex);
		}
	}

	public boolean validateToken(String token) {
		if (token == null || token.isBlank()) {
			return false;
		}
		try {
			String[] parts = token.split("\\.");
			if (parts.length != 3) {
				return false;
			}

			String dataToSign = parts[0] + "." + parts[1];
			String expectedSignature = sign(dataToSign);

			// Constant-time comparison to prevent timing attacks
			if (!MessageDigest.isEqual(
					parts[2].getBytes(StandardCharsets.UTF_8),
					expectedSignature.getBytes(StandardCharsets.UTF_8))) {
				return false;
			}

			// Verify expiration
			Map<String, Object> claims = parsePayload(parts[1]);
			if (claims == null || !claims.containsKey("exp")) {
				return false;
			}

			long exp = ((Number) claims.get("exp")).longValue();
			long nowSeconds = System.currentTimeMillis() / 1000;
			return exp > nowSeconds;
		} catch (Exception ex) {
			return false;
		}
	}

	public String getUsername(String token) {
		try {
			String[] parts = token.split("\\.");
			if (parts.length >= 2) {
				Map<String, Object> claims = parsePayload(parts[1]);
				return claims != null ? (String) claims.get("sub") : null;
			}
		} catch (Exception ignored) {
		}
		return null;
	}

	public String getRole(String token) {
		try {
			String[] parts = token.split("\\.");
			if (parts.length >= 2) {
				Map<String, Object> claims = parsePayload(parts[1]);
				return claims != null ? (String) claims.get("role") : null;
			}
		} catch (Exception ignored) {
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> parsePayload(String encodedPayload) {
		try {
			byte[] decoded = Base64.getUrlDecoder().decode(encodedPayload);
			return OBJECT_MAPPER.readValue(decoded, Map.class);
		} catch (Exception ex) {
			return null;
		}
	}

	private String sign(String data) throws Exception {
		Mac mac = Mac.getInstance(HMAC_SHA256);
		SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
		mac.init(secretKeySpec);
		byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
		return base64UrlEncode(rawHmac);
	}

	private String base64UrlEncode(byte[] bytes) {
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}
}
