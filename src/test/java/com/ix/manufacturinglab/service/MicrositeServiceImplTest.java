package com.ix.manufacturinglab.service;

import com.ix.manufacturinglab.dto.IndustryDTO;
import com.ix.manufacturinglab.dto.MicrositeDataDTO;
import com.ix.manufacturinglab.dto.SubIndustryDTO;
import com.ix.manufacturinglab.dto.ValueChainDTO;
import com.ix.manufacturinglab.entity.Industry;
import com.ix.manufacturinglab.service.impl.MicrositeServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for MicrositeServiceImpl.
 */
/*
@RunWith(MockitoJUnitRunner.class)
public class MicrositeServiceImplTest {

    private MicrositeServiceImpl micrositeService;

    @Before
    public void setUp() {
        micrositeService = new MicrositeServiceImpl();
        micrositeService.initStaticData();
    }

    @Test
    public void testGetAllIndustries() {
        List<Industry> industries = micrositeService.getAllIndustries();

        assertNotNull(industries);
        assertEquals(8, industries.size());
        assertEquals("Automotive", industries.get(0).getIndustryName());
    }

    @Test
    public void testGetAllSubIndustries() {
        List<SubIndustryDTO> subIndustries = micrositeService.getAllSubIndustries();

        assertNotNull(subIndustries);
        assertEquals(24, subIndustries.size());
    }

    @Test
    public void testGetSubIndustriesByIndustryId_Automotive() {
        List<SubIndustryDTO> subIndustries = micrositeService.getSubIndustriesByIndustryId(1L);

        assertNotNull(subIndustries);
        assertEquals(4, subIndustries.size());
        assertTrue(subIndustries.stream().allMatch(si -> si.getIndustryId().equals(1L)));
    }

    @Test
    public void testGetSubIndustriesByIndustryId_AerospaceDefense() {
        List<SubIndustryDTO> subIndustries = micrositeService.getSubIndustriesByIndustryId(2L);

        assertNotNull(subIndustries);
        assertEquals(3, subIndustries.size());
    }

    @Test
    public void testGetSubIndustriesByIndustryId_NonExistent() {
        List<SubIndustryDTO> subIndustries = micrositeService.getSubIndustriesByIndustryId(999L);

        assertNotNull(subIndustries);
        assertTrue(subIndustries.isEmpty());
    }

    @Test
    public void testGetAllValueChains() {
        List<ValueChainDTO> valueChains = micrositeService.getAllValueChains();

        assertNotNull(valueChains);
        assertEquals(29, valueChains.size());
    }

    @Test
    public void testGetValueChainsByIndustryAndSubIndustry() {
        // Automotive > Passenger Vehicles
        List<ValueChainDTO> valueChains = micrositeService.getValueChainsByIndustryAndSubIndustry(1L, 1L);

        assertNotNull(valueChains);
        assertEquals(5, valueChains.size());
        assertTrue(valueChains.stream().allMatch(vc ->
                vc.getIndustryId().equals(1L) && vc.getSubIndustryId().equals(1L)));
    }

    @Test
    public void testGetValueChainsByIndustryOnly() {
        // All value chains for Automotive (industryId=1)
        List<ValueChainDTO> valueChains = micrositeService.getValueChainsByIndustryAndSubIndustry(1L, null);

        assertNotNull(valueChains);
        assertEquals(8, valueChains.size()); // 5 Passenger + 3 EV
        assertTrue(valueChains.stream().allMatch(vc -> vc.getIndustryId().equals(1L)));
    }

    @Test
    public void testGetValueChainsBySubIndustryOnly() {
        // All value chains for Commercial Aviation (subIndustryId=5)
        List<ValueChainDTO> valueChains = micrositeService.getValueChainsByIndustryAndSubIndustry(null, 5L);

        assertNotNull(valueChains);
        assertEquals(4, valueChains.size());
        assertTrue(valueChains.stream().allMatch(vc -> vc.getSubIndustryId().equals(5L)));
    }

    @Test
    public void testGetAllMicrositeData() {
        MicrositeDataDTO data = micrositeService.getAllMicrositeData();

        assertNotNull(data);
        assertNotNull(data.getIndustries());
        assertNotNull(data.getSubIndustries());
        assertNotNull(data.getValueChains());
        assertEquals(8, data.getIndustries().size());
        assertEquals(24, data.getSubIndustries().size());
        assertEquals(29, data.getValueChains().size());
    }
}

 */
