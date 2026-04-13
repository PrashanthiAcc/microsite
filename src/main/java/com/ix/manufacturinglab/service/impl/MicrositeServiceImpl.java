package com.ix.manufacturinglab.service.impl;

import com.ix.manufacturinglab.constants.CommonExceptionConstants;
import com.ix.manufacturinglab.constants.ManufacturingLabConstants;
import com.ix.manufacturinglab.dto.MicrositeDataDTO;
import com.ix.manufacturinglab.dto.SubIndustryDTO;
import com.ix.manufacturinglab.entity.Industry;
import com.ix.manufacturinglab.entity.SubIndustry;
import com.ix.manufacturinglab.entity.ValueChain;
import com.ix.manufacturinglab.exception.CommonException;
import com.ix.manufacturinglab.repository.IndustryRepository;
import com.ix.manufacturinglab.repository.SubIndustryRepository;
import com.ix.manufacturinglab.repository.ValueChainRepository;
import com.ix.manufacturinglab.service.MicrositeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.ix.manufacturinglab.constants.ManufacturingLabConstants.INDUSTRY_NOT_FOUND;
import static com.ix.manufacturinglab.constants.ManufacturingLabConstants.INDUSTRY_ID_NOT_FOUND;
import static com.ix.manufacturinglab.constants.ManufacturingLabConstants.SUB_INDUSTRY_NOT_FOUND;

/**
 * Implementation using database (JPA repositories)
 * for Industry, SubIndustry, and ValueChain.
 */
@Service
public class MicrositeServiceImpl implements MicrositeService {

    private static final Logger logger = LoggerFactory.getLogger(MicrositeServiceImpl.class);

    private final IndustryRepository industryRepository;
    private final SubIndustryRepository subIndustryRepository;
    private final ValueChainRepository valueChainRepository;


    public MicrositeServiceImpl(IndustryRepository industryRepository, SubIndustryRepository subIndustryRepository, ValueChainRepository valueChainRepository) {
        this.industryRepository = industryRepository;
        this.subIndustryRepository = subIndustryRepository;
        this.valueChainRepository = valueChainRepository;
    }

    public List<Industry> getAllIndustries() {
        return industryRepository.findByIsActiveTrue();
    }

    @Override
    public Industry createIndustry(Industry industry) throws CommonException {

        try {
            industry.setIsActive(true);
            industry.setLastUpdated(LocalDateTime.now());
            return industryRepository.save(industry);
        } catch (Exception e) {
            logger.error("Exception occurred while creating industry", e);
            throw new CommonException(
                    CommonExceptionConstants.BAD_REQUEST,
                    ManufacturingLabConstants.CREATE_INDUSTRY_GENERIC_ERROR_MESSAGE);
        }
    }

    @Override
    public Industry updateIndustry(Long id, Industry industry) throws CommonException {

        try {
            Optional<Industry> optionalIndustry = industryRepository.findById(id);
            if (optionalIndustry.isEmpty()) throw new CommonException(
                    CommonExceptionConstants.BAD_REQUEST,
                    INDUSTRY_NOT_FOUND);
            Industry existingIndustry = optionalIndustry.get();
            existingIndustry.setIndustryName(industry.getIndustryName());
            existingIndustry.setUpdatedById(industry.getUpdatedById());
            existingIndustry.setLastUpdated(LocalDateTime.now());
            existingIndustry.setIsActive(true);
            return industryRepository.save(existingIndustry);
        } catch (CommonException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error updating industry", e);
            throw new CommonException(
                    CommonExceptionConstants.BAD_REQUEST,
                    ManufacturingLabConstants.UPDATE_INDUSTRY_GENERIC_ERROR_MESSAGE);
        }
    }

    @Override
    public String deleteIndustry(Long id, Integer updatedById) throws CommonException {

        try {
            Optional<Industry> optionalIndustry = industryRepository.findById(id);
            if (optionalIndustry.isEmpty()) {
                throw new CommonException(
                        CommonExceptionConstants.BAD_REQUEST,
                        INDUSTRY_NOT_FOUND);
            }
            Industry industry = optionalIndustry.get();
            String industryName = industry.getIndustryName();
            // Soft delete: mark inactive and update timestamp
            industry.setIsActive(false);
            industry.setUpdatedById(updatedById);
            industry.setLastUpdated(LocalDateTime.now());
            industryRepository.save(industry);
            return industryName;
        } catch (CommonException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting industry", e);
            throw new CommonException(
                    CommonExceptionConstants.BAD_REQUEST,
                    ManufacturingLabConstants.DELETE_INDUSTRY_GENERIC_ERROR_MESSAGE);
        }
    }

    @Override
    public List<SubIndustry> getAllSubIndustries() {
        logger.debug("Fetching all sub-industries");
        return subIndustryRepository.findAllByActiveIndustry();
    }

    @Override
    public List<SubIndustry> getSubIndustriesByIndustryId(Long industryId) {
        logger.debug("Fetching sub-industries for industryId: {}", industryId);

        // Fetch sub-industries ONLY if industry is active
        List<SubIndustry> subIndustries = subIndustryRepository.findActiveSubIndustriesByIndustryId(industryId);
        if (subIndustries.isEmpty()) {
            throw new CommonException(CommonExceptionConstants.NOT_FOUND, INDUSTRY_ID_NOT_FOUND + industryId);
        }

        return subIndustries;
    }

    @Override
    @Transactional
    public SubIndustryDTO createSubIndustry(SubIndustryDTO subIndustryDTO) {

        logger.debug("Creating sub-industry with name {}", subIndustryDTO.getSubIndustryName());

        Industry industry = industryRepository.findById(subIndustryDTO.getIndustryId())
                .orElseThrow(() -> new CommonException(CommonExceptionConstants.NOT_FOUND,
                        "Industry does not exist with id " + subIndustryDTO.getIndustryId()));


        if (Boolean.FALSE.equals(industry.getIsActive())) {
            throw new CommonException(
                    CommonExceptionConstants.CONFLICT,
                    "Cannot create sub-industry because the industry is inactive"
            );
        }


        SubIndustry subIndustry = new SubIndustry();
        subIndustry.setSubIndustryName(subIndustryDTO.getSubIndustryName());
        subIndustry.setIndustryId(subIndustryDTO.getIndustryId());
        subIndustry.setUpdatedById(subIndustryDTO.getUpdatedById());
        subIndustry.setLastUpdated(LocalDateTime.now());

        SubIndustry savedSubIndustry = subIndustryRepository.save(subIndustry);

        return SubIndustryDTO.builder()
                .subIndustryId(savedSubIndustry.getSubIndustryId())
                .subIndustryName(savedSubIndustry.getSubIndustryName())
                .industryId(savedSubIndustry.getIndustryId())
                .build();
    }

    @Override
    @Transactional
    public SubIndustryDTO updateSubIndustry(Long subIndustryId, SubIndustryDTO subIndustryDTO) {

        logger.debug("Updating Sub-Industry with id {}", subIndustryId);

        SubIndustry subIndustry = subIndustryRepository.findById(subIndustryId)
                .orElseThrow(() -> new CommonException(
                        CommonExceptionConstants.NOT_FOUND,
                        SUB_INDUSTRY_NOT_FOUND + subIndustryId
                ));

        if (subIndustryDTO.getIndustryId() != null) {

            Industry industry = industryRepository.findById(subIndustryDTO.getIndustryId())
                    .orElseThrow(() -> new CommonException(
                            CommonExceptionConstants.NOT_FOUND,
                            "Industry does not exist with id " + subIndustryDTO.getIndustryId()
                    ));

            if (Boolean.FALSE.equals(industry.getIsActive())) {
                throw new CommonException(
                        CommonExceptionConstants.CONFLICT,
                        "Cannot update sub-industry because the industry is inactive"
                );
            }

            subIndustry.setIndustryId(subIndustryDTO.getIndustryId());
        }

        subIndustry.setSubIndustryName(subIndustryDTO.getSubIndustryName());
        subIndustry.setUpdatedById(subIndustryDTO.getUpdatedById());
        subIndustry.setLastUpdated(LocalDateTime.now());

        subIndustryRepository.save(subIndustry);

        return SubIndustryDTO.builder()
                .subIndustryId(subIndustry.getSubIndustryId())
                .subIndustryName(subIndustry.getSubIndustryName())
                .industryId(subIndustry.getIndustryId())
                .build();
    }

    @Override
    @Transactional
    public void deleteSubIndustry(Long subIndustryId) {

        logger.debug("Deleting sub-industry with id {}", subIndustryId);

        SubIndustry subIndustry = subIndustryRepository.findById(subIndustryId)
                .orElseThrow(() -> new CommonException(
                        CommonExceptionConstants.NOT_FOUND,
                        "Sub-industry does not exist with id " + subIndustryId
                ));

        boolean isMapped = valueChainRepository.existsBySubIndustryId(subIndustryId);

        if (isMapped) {
            throw new CommonException(
                    CommonExceptionConstants.CONFLICT, "Value chains are associated with this sub-industry");
        }

        subIndustryRepository.delete(subIndustry);
    }

    @Override
    public List<ValueChain> getAllValueChains() {
        logger.debug("Fetching all value chains");
        return valueChainRepository.findAllByActiveIndustry();
    }

    @Override
    public List<ValueChain> getValueChainsByIndustryAndSubIndustry(Long industryId, Long subIndustryId) {
        logger.debug("Fetching value chains for industryId: {} and subIndustryId: {}", industryId, subIndustryId);
        return valueChainRepository.findFilteredValueChains(industryId, subIndustryId);
    }

    @Override
    public MicrositeDataDTO getAllMicrositeData() {
        logger.debug("Fetching all microsite data (industries, sub-industries, value chains)");
        List<Industry> industries = industryRepository.findByIsActiveTrue();
        List<SubIndustry> subIndustries = subIndustryRepository.findAllByActiveIndustry();
        List<ValueChain> valueChains = valueChainRepository.findAllByActiveIndustry();
        return MicrositeDataDTO.builder()
                .industries(industries)
                .subIndustries(subIndustries)
                .valueChains(valueChains)
                .build();
    }
}
