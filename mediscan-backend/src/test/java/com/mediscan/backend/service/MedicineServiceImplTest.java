package com.mediscan.backend.service;

import com.mediscan.backend.dto.common.PageResponse;
import com.mediscan.backend.dto.medicine.MedicineRequest;
import com.mediscan.backend.dto.medicine.MedicineResponse;
import com.mediscan.backend.exception.ResourceNotFoundException;
import com.mediscan.backend.model.Medicine;
import com.mediscan.backend.repository.MedicineRepository;
import com.mediscan.backend.service.impl.MedicineServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicineServiceImplTest {

    @Mock
    private MedicineRepository medicineRepository;

    @InjectMocks
    private MedicineServiceImpl medicineService;

    @Test
    void getMedicineById_shouldReturnMedicine_whenExists() {
        Medicine medicine = Medicine.builder().id("med-1").name("Paracetamol").build();
        when(medicineRepository.findById("med-1")).thenReturn(Optional.of(medicine));

        MedicineResponse response = medicineService.getMedicineById("med-1");

        assertThat(response.getName()).isEqualTo("Paracetamol");
    }

    @Test
    void getMedicineById_shouldThrowNotFound_whenMissing() {
        when(medicineRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> medicineService.getMedicineById("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void searchByName_shouldReturnPagedResults() {
        Medicine medicine = Medicine.builder().id("med-1").name("Paracetamol").build();
        Page<Medicine> page = new PageImpl<>(List.of(medicine));
        when(medicineRepository.findByNameContainingIgnoreCase(anyString(), any(Pageable.class))).thenReturn(page);

        PageResponse<MedicineResponse> response = medicineService.searchByName("para", 0, 20);

        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getTotalItems()).isEqualTo(1);
    }

    @Test
    void createMedicine_shouldPersistAndReturnResponse() {
        MedicineRequest request = new MedicineRequest();
        request.setName("Ibuprofen");
        request.setDosage("200mg");

        Medicine saved = Medicine.builder().id("med-2").name("Ibuprofen").dosage("200mg").build();
        when(medicineRepository.save(any(Medicine.class))).thenReturn(saved);

        MedicineResponse response = medicineService.createMedicine(request);

        assertThat(response.getId()).isEqualTo("med-2");
        assertThat(response.getName()).isEqualTo("Ibuprofen");
    }

    @Test
    void deleteMedicine_shouldThrowNotFound_whenMissing() {
        when(medicineRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> medicineService.deleteMedicine("missing"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(medicineRepository, org.mockito.Mockito.never()).deleteById(anyString());
    }
}
