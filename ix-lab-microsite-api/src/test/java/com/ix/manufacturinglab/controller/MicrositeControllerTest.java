/*package com.ix.manufacturinglab.controller;

import com.ix.manufacturinglab.dto.IndustryDTO;
import com.ix.manufacturinglab.dto.MicrositeDataDTO;
import com.ix.manufacturinglab.dto.SubIndustryDTO;
import com.ix.manufacturinglab.dto.ValueChainDTO;
import com.ix.manufacturinglab.service.MicrositeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for MicrositeController.
 */
/*
@RunWith(MockitoJUnitRunner.class)
public class MicrositeControllerTest {

    @Mock
    private MicrositeService micrositeService;

    @InjectMocks
    private MicrositeController micrositeController;

    private List<IndustryDTO> sampleIndustries;
    private List<SubIndustryDTO> sampleSubIndustries;
    private List<ValueChainDTO> sampleValueChains;

    @Before
    public void setUp() {
        sampleIndustries = Arrays.asList(
                IndustryDTO.builder().industryId(1L).industryName("Automotive").build(),
                IndustryDTO.builder().industryId(2L).industryName("Aerospace & Defense").build()
        );

        sampleSubIndustries = Arrays.asList(
                SubIndustryDTO.builder().subIndustryId(1L).subIndustryName("Passenger Vehicles").industryId(1L).build(),
                SubIndustryDTO.builder().subIndustryId(2L).subIndustryName("Commercial Aviation").industryId(2L).build()
        );

        sampleValueChains = Arrays.asList(
                ValueChainDTO.builder().valueChainId(1L).valueChainName("Design & Engineering").industryId(1L).subIndustryId(1L).build(),
                ValueChainDTO.builder().valueChainId(2L).valueChainName("Airframe Design").industryId(2L).subIndustryId(2L).build()
        );
    }
/*
    @Test
    public void testGetAllIndustries_Success() {
        when(micrositeService.getAllIndustries()).thenReturn(sampleIndustries);

        ResponseEntity<Object> response = micrositeController.getAllIndustries();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        List<IndustryDTO> body = (List<IndustryDTO>) response.getBody();
        assertEquals(2, body.size());
    }



    @Test
    public void testGetAllSubIndustries_Success() {
        when(micrositeService.getAllSubIndustries()).thenReturn(sampleSubIndustries);

        ResponseEntity<Object> response = micrositeController.getAllSubIndustries();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void testGetSubIndustriesByIndustryId_Success() {
        List<SubIndustryDTO> filtered = Arrays.asList(
                SubIndustryDTO.builder().subIndustryId(1L).subIndustryName("Passenger Vehicles").industryId(1L).build()
        );
        when(micrositeService.getSubIndustriesByIndustryId(1L)).thenReturn(filtered);

        ResponseEntity<Object> response = micrositeController.getSubIndustriesByIndustryId(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<SubIndustryDTO> body = (List<SubIndustryDTO>) response.getBody();
        assertEquals(1, body.size());
        assertEquals("Passenger Vehicles", body.get(0).getSubIndustryName());
    }

    @Test
    public void testGetAllValueChains_Success() {
        when(micrositeService.getAllValueChains()).thenReturn(sampleValueChains);

        ResponseEntity<Object> response = micrositeController.getAllValueChains();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void testGetValueChainsByFilter_Success() {
        List<ValueChainDTO> filtered = Arrays.asList(
                ValueChainDTO.builder().valueChainId(1L).valueChainName("Design & Engineering").industryId(1L).subIndustryId(1L).build()
        );
        when(micrositeService.getValueChainsByIndustryAndSubIndustry(1L, 1L)).thenReturn(filtered);

        ResponseEntity<Object> response = micrositeController.getValueChainsByFilter(1L, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<ValueChainDTO> body = (List<ValueChainDTO>) response.getBody();
        assertEquals(1, body.size());
    }

    @Test
    public void testGetAllMicrositeData_Success() {
        MicrositeDataDTO data = MicrositeDataDTO.builder()
                .industries(sampleIndustries)
                .subIndustries(sampleSubIndustries)
                .valueChains(sampleValueChains)
                .build();
        when(micrositeService.getAllMicrositeData()).thenReturn(data);

        ResponseEntity<Object> response = micrositeController.getAllMicrositeData();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        MicrositeDataDTO body = (MicrositeDataDTO) response.getBody();
        assertNotNull(body);
        assertEquals(2, body.getIndustries().size());
        assertEquals(2, body.getSubIndustries().size());
        assertEquals(2, body.getValueChains().size());
    }
}
*/