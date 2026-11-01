package com.mediscan.backend.service.impl;

import com.mediscan.backend.dto.common.PageResponse;
import com.mediscan.backend.dto.medicine.MedicineRequest;
import com.mediscan.backend.dto.medicine.MedicineResponse;
import com.mediscan.backend.exception.ResourceNotFoundException;
import com.mediscan.backend.model.Medicine;
import com.mediscan.backend.repository.MedicineRepository;
import com.mediscan.backend.service.MedicineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicineServiceImpl implements MedicineService {

    private final MedicineRepository medicineRepository;

    @Override
    public PageResponse<MedicineResponse> getAllMedicines(int page, int size) {
        Page<Medicine> result = medicineRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name")));
        return toPageResponse(result, page, size);
    }

    @Override
    public MedicineResponse getMedicineById(String id) {
        return toResponse(findEntityById(id));
    }

    @Override
    public PageResponse<MedicineResponse> searchByName(String name, int page, int size) {
        Page<Medicine> result = medicineRepository.findByNameContainingIgnoreCase(
                name, PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name")));
        return toPageResponse(result, page, size);
    }

    @Override
    public MedicineResponse createMedicine(MedicineRequest request) {
        Medicine medicine = Medicine.builder()
                .name(request.getName())
                .genericName(request.getGenericName())
                .purpose(request.getPurpose())
                .dosage(request.getDosage())
                .warnings(request.getWarnings())
                .sideEffects(request.getSideEffects())
                .manufacturer(request.getManufacturer())
                .build();

        Medicine saved = medicineRepository.save(medicine);
        log.info("Medicine created: {}", saved.getId());
        return toResponse(saved);
    }

    @Override
    public MedicineResponse updateMedicine(String id, MedicineRequest request) {
        Medicine existing = findEntityById(id);

        existing.setName(request.getName());
        existing.setGenericName(request.getGenericName());
        existing.setPurpose(request.getPurpose());
        existing.setDosage(request.getDosage());
        existing.setWarnings(request.getWarnings());
        existing.setSideEffects(request.getSideEffects());
        existing.setManufacturer(request.getManufacturer());

        Medicine saved = medicineRepository.save(existing);
        log.info("Medicine updated: {}", saved.getId());
        return toResponse(saved);
    }

    @Override
    public void deleteMedicine(String id) {
        findEntityById(id); // ensures existence
        medicineRepository.deleteById(id);
        log.info("Medicine deleted: {}", id);
    }

    private Medicine findEntityById(String id) {
        return medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found"));
    }

    private PageResponse<MedicineResponse> toPageResponse(Page<Medicine> page, int pageNum, int size) {
        List<MedicineResponse> items = page.getContent().stream().map(this::toResponse).toList();
        return PageResponse.<MedicineResponse>builder()
                .items(items)
                .page(pageNum)
                .size(size)
                .totalItems(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    private MedicineResponse toResponse(Medicine medicine) {
        return MedicineResponse.builder()
                .id(medicine.getId())
                .name(medicine.getName())
                .genericName(medicine.getGenericName())
                .purpose(medicine.getPurpose())
                .dosage(medicine.getDosage())
                .warnings(medicine.getWarnings())
                .sideEffects(medicine.getSideEffects())
                .manufacturer(medicine.getManufacturer())
                .createdAt(medicine.getCreatedAt())
                .updatedAt(medicine.getUpdatedAt())
                .build();
    }
}
