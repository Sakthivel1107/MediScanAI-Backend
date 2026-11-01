package com.mediscan.backend.service;

import com.mediscan.backend.model.User;

/**
 * Issues application JWTs for authenticated users.
 */
public interface JwtService {

    /**
     * @return the signed JWT and its time-to-live in milliseconds
     */
    String generateToken(User user);

    long getExpirationMs();
}
