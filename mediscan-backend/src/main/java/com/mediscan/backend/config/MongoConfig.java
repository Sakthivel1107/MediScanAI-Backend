package com.mediscan.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * Enables {@code @CreatedDate}/{@code @LastModifiedDate} auditing support for
 * MongoDB documents (User, Medicine). Connection details themselves come purely
 * from {@code spring.data.mongodb.uri}, sourced from the MONGODB_URI env variable.
 */
@Configuration
@EnableMongoAuditing
public class MongoConfig {
}
