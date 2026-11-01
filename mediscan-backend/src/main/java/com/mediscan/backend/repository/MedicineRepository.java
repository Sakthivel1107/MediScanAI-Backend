package com.mediscan.backend.repository;

import com.mediscan.backend.model.Medicine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MedicineRepository extends MongoRepository<Medicine, String> {

    Page<Medicine> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
