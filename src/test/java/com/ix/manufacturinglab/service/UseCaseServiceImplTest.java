/*package com.ix.manufacturinglab.service;

import com.ix.common.exception.ConnectorException;
import com.ix.manufacturinglab.dto.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for UseCaseServiceImpl.
 */
/*
@RunWith(MockitoJUnitRunner.class)
public class UseCaseServiceImplTest {

    @Mock
    private UseCaseRepository useCaseRepository;

    @InjectMocks
    private UseCaseServiceImpl useCaseService;

    private UseCase sampleEntity;
    private UseCaseRequestDTO sampleRequest;

    @Before
    public void setUp() {
        sampleEntity = new UseCase();
        sampleEntity.setId(1L);
        sampleEntity.setTitle("Test Use Case");
        sampleEntity.setDescription("Test Description");
        sampleEntity.setCategory("Digital Twin");
        sampleEntity.setStatus("DRAFT");
        sampleEntity.setPriority("HIGH");
        sampleEntity.setIndustrySegment("Manufacturing");
        sampleEntity.setCreatedBy("testUser");
        sampleEntity.setCreatedAt(LocalDateTime.now());
        sampleEntity.setUpdatedAt(LocalDateTime.now());
        sampleEntity.setIsActive(true);

        sampleRequest = UseCaseRequestDTO.builder()
                .title("Test Use Case")
                .description("Test Description")
                .category("Digital Twin")
                .priority("HIGH")
                .industrySegment("Manufacturing")
                .build();
    }

    @Test
    public void testCreateUseCase_Success() {
        when(useCaseRepository.existsByTitleIgnoreCase("Test Use Case")).thenReturn(false);
        when(useCaseRepository.save(any(UseCase.class))).thenReturn(sampleEntity);

        UseCaseResponseDTO result = useCaseService.createUseCase(sampleRequest, "testUser");

        assertNotNull(result);
        assertEquals("Test Use Case", result.getTitle());
        assertEquals("DRAFT", result.getStatus());
    }

    @Test(expected = ConnectorException.class)
    public void testCreateUseCase_DuplicateTitle() {
        when(useCaseRepository.existsByTitleIgnoreCase("Test Use Case")).thenReturn(true);

        useCaseService.createUseCase(sampleRequest, "testUser");
    }

    @Test
    public void testGetUseCaseById_Success() {
        when(useCaseRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(sampleEntity));

        UseCaseResponseDTO result = useCaseService.getUseCaseById(1L);

        assertNotNull(result);
        assertEquals(Long.valueOf(1L), result.getId());
    }

    @Test(expected = ConnectorException.class)
    public void testGetUseCaseById_NotFound() {
        when(useCaseRepository.findByIdAndIsActiveTrue(999L)).thenReturn(Optional.empty());

        useCaseService.getUseCaseById(999L);
    }

    @Test
    public void testGetAllUseCases_Success() {
        Page<UseCase> page = new PageImpl<>(Collections.singletonList(sampleEntity));
        when(useCaseRepository.findByIsActiveTrue(any(Pageable.class))).thenReturn(page);

        Page<UseCaseResponseDTO> result = useCaseService.getAllUseCases(0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    public void testUpdateUseCase_Success() {
        UseCaseUpdateRequestDTO updateDTO = UseCaseUpdateRequestDTO.builder()
                .title("Updated Title")
                .build();

        when(useCaseRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(sampleEntity));
        when(useCaseRepository.save(any(UseCase.class))).thenReturn(sampleEntity);

        UseCaseResponseDTO result = useCaseService.updateUseCase(1L, updateDTO, "updater");

        assertNotNull(result);
    }

    @Test
    public void testDeleteUseCase_Success() {
        when(useCaseRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(sampleEntity));
        when(useCaseRepository.save(any(UseCase.class))).thenReturn(sampleEntity);

        useCaseService.deleteUseCase(1L, "deleter");

        // No exception means success (soft delete)
    }

    @Test
    public void testUpdateUseCaseStatus_Success() {
        UseCaseStatusUpdateDTO statusDTO = UseCaseStatusUpdateDTO.builder()
                .status("APPROVED")
                .build();

        when(useCaseRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(sampleEntity));
        when(useCaseRepository.save(any(UseCase.class))).thenReturn(sampleEntity);

        UseCaseResponseDTO result = useCaseService.updateUseCaseStatus(1L, statusDTO, "reviewer");

        assertNotNull(result);
    }

    @Test
    public void testSearchUseCases_Success() {
        Page<UseCase> page = new PageImpl<>(Collections.singletonList(sampleEntity));
        when(useCaseRepository.searchByKeyword(eq("test"), any(Pageable.class))).thenReturn(page);

        Page<UseCaseResponseDTO> result = useCaseService.searchUseCases("test", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    public void testGetUseCaseCountByStatus() {
        when(useCaseRepository.countByStatusAndIsActiveTrue("DRAFT")).thenReturn(5L);

        long count = useCaseService.getUseCaseCountByStatus("DRAFT");

        assertEquals(5L, count);
    }

    @Test
    public void testGetUseCaseCountByCategory() {
        when(useCaseRepository.countByCategoryAndIsActiveTrue("Digital Twin")).thenReturn(3L);

        long count = useCaseService.getUseCaseCountByCategory("Digital Twin");

        assertEquals(3L, count);
    }
}
*/