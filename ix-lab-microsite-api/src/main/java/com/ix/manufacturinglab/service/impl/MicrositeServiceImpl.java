package com.ix.manufacturinglab.service.impl;

import com.ix.manufacturinglab.dto.MicrositeDataDTO;
import com.ix.manufacturinglab.entity.Industry;
import com.ix.manufacturinglab.entity.SubIndustry;
import com.ix.manufacturinglab.entity.ValueChain;
import com.ix.manufacturinglab.repository.IndustryRepository;
import com.ix.manufacturinglab.repository.SubIndustryRepository;
import com.ix.manufacturinglab.repository.ValueChainRepository;
import com.ix.manufacturinglab.service.MicrositeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of MicrositeService using static/in-memory data
 * for Industry, SubIndustry, and ValueChain.
 */
@Service
public class MicrositeServiceImpl implements MicrositeService {

    private static final Logger logger = LoggerFactory.getLogger(MicrositeServiceImpl.class);

    private List<Industry> industries;
    private List<SubIndustry> subIndustries;
    private List<ValueChain> valueChains;

    private final IndustryRepository industryRepository;
    private final SubIndustryRepository subIndustryRepository;

    private final ValueChainRepository valueChainRepository;


    public MicrositeServiceImpl(IndustryRepository industryRepository, SubIndustryRepository subIndustryRepository, ValueChainRepository valueChainRepository) {
        this.industryRepository = industryRepository;
        this.subIndustryRepository = subIndustryRepository;
        this.valueChainRepository = valueChainRepository;
    }

    public List<Industry> getAllIndustries() {
        return industryRepository.findAll();
    }

    @Override
    public List<SubIndustry> getAllSubIndustries() {
        logger.info("Fetching all sub-industries");
        return subIndustryRepository.findAll();
    }

    @Override
    public List<SubIndustry> getSubIndustriesByIndustryId(Long industryId) {
        logger.info("Fetching sub-industries for industryId: {}", industryId);
        /*
        return subIndustries.stream()
                .filter(si -> si.getIndustryId().equals(industryId))
                .collect(Collectors.toList());
         */
        return subIndustryRepository.findByIndustryId(industryId);
    }


    @Override
    public List<ValueChain> getAllValueChains() {
        logger.info("Fetching all value chains");
        return valueChainRepository.findAll();
    }

    @Override
    public List<ValueChain> getValueChainsByIndustryAndSubIndustry(Long industryId, Long subIndustryId) {
        logger.info("Fetching value chains for industryId: {} and subIndustryId: {}", industryId, subIndustryId);
        return valueChains.stream()
                .filter(vc -> {
                    boolean matches = true;
                    if (industryId != null) {
                        matches = vc.getIndustryId().equals(industryId);
                    }
                    if (subIndustryId != null) {
                        matches = matches && vc.getSubIndustryId().equals(subIndustryId);
                    }
                    return matches;
                })
                .collect(Collectors.toList());
    }

    @Override
    public MicrositeDataDTO getAllMicrositeData() {
        logger.info("Fetching all microsite data (industries, sub-industries, value chains)");
        List<Industry> industries = industryRepository.findAll();
        List<SubIndustry> subIndustries = subIndustryRepository.findAll();
        List<ValueChain> valueChains = valueChainRepository.findAll();
        return MicrositeDataDTO.builder()
                .industries(industries)
                .subIndustries(subIndustries)
                .valueChains(valueChains)
                .build();
    }
}
