package com.mediscan.backend.service;

import com.mediscan.backend.dto.common.PageResponse;
import com.mediscan.backend.dto.medicine.MedicineRequest;
import com.mediscan.backend.dto.medicine.MedicineResponse;

public interface MedicineService {

    PageResponse<MedicineResponse> getAllMedicines(int page, int size);

    MedicineResponse getMedicineById(String id);

    PageResponse<MedicineResponse> searchByName(String name, int page, int size);

    MedicineResponse createMedicine(MedicineRequest request);

    MedicineResponse updateMedicine(String id, MedicineRequest request);

    void deleteMedicine(String id);
}
