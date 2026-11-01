package com.mediscan.backend.repository;

import com.mediscan.backend.model.Scan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ScanRepository extends MongoRepository<Scan, String> {

    Page<Scan> findByUserIdOrderByScannedAtDesc(String userId, Pageable pageable);

    Optional<Scan> findByIdAndUserId(String id, String userId);

    long countByUserId(String userId);

    void deleteByIdAndUserId(String id, String userId);
}
