package com.hs.security;

public record JwtClaims(Integer userId, String tenantId, String role, String username) {
}
