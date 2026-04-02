package com.ix.manufacturinglab.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import com.ix.manufacturinglab.dto.ArtifactDTO;
import com.ix.manufacturinglab.dto.SpeakerDTO;
import com.ix.manufacturinglab.dto.UseCaseRequestDTO;
import com.ix.manufacturinglab.dto.UseCaseResponseDTO;
import com.ix.manufacturinglab.dto.FaqDTO;
import com.ix.manufacturinglab.entity.UseCase;
import com.ix.manufacturinglab.entity.UseCaseArtifact;
import com.ix.manufacturinglab.entity.UseCaseContent;
import com.ix.manufacturinglab.entity.UseCaseSpeaker;
import com.ix.manufacturinglab.entity.UseCaseTag;
import com.ix.manufacturinglab.entity.UseCaseFaq;
import com.ix.manufacturinglab.entity.ValueChain;
import com.ix.manufacturinglab.entity.SubIndustry;
import com.ix.manufacturinglab.entity.Industry;
import com.ix.manufacturinglab.repository.UseCaseContentRepository;
import com.ix.manufacturinglab.repository.UseCaseRepository;
import com.ix.manufacturinglab.repository.UseCaseSpeakerRepository;
import com.ix.manufacturinglab.repository.UseCaseArtifactRepository;
import com.ix.manufacturinglab.repository.UseCaseTagRepository;
import com.ix.manufacturinglab.repository.ValueChainRepository;
import com.ix.manufacturinglab.repository.SubIndustryRepository;
import com.ix.manufacturinglab.repository.IndustryRepository;
import com.ix.manufacturinglab.storage.CloudStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ix.manufacturinglab.constants.CommonExceptionConstants;
import com.ix.manufacturinglab.constants.ManufacturingLabConstants;
import com.ix.manufacturinglab.enums.UseCaseStatus;
import com.ix.manufacturinglab.exception.CommonException;
import com.ix.manufacturinglab.service.UseCaseService;
import org.springframework.web.multipart.MultipartFile;

/**
 * Implementation of UseCaseService that persists data across multiple entity tables.
 */
@Service
public class UseCaseServiceImpl implements UseCaseService {

    private static final Logger logger = LoggerFactory.getLogger(UseCaseServiceImpl.class);

    private final UseCaseRepository useCaseRepository;
    private final UseCaseContentRepository useCaseContentRepository;
    private final UseCaseSpeakerRepository useCaseSpeakerRepository;
    private final UseCaseArtifactRepository useCaseArtifactRepository;
    private final UseCaseTagRepository useCaseTagRepository;
    private final ValueChainRepository valueChainRepository;
    private final SubIndustryRepository subIndustryRepository;
    private final IndustryRepository industryRepository;
    private final CloudStorageService cloudStorageService;
    @Value("${storage.path.artifacts.client.testimonials}")
    private String clientTestimonialsPath;

    public UseCaseServiceImpl(UseCaseRepository useCaseRepository,
                              UseCaseContentRepository useCaseContentRepository,
                              UseCaseSpeakerRepository useCaseSpeakerRepository,
                              UseCaseArtifactRepository useCaseArtifactRepository,
                              UseCaseTagRepository useCaseTagRepository,
                              ValueChainRepository valueChainRepository,
                              SubIndustryRepository subIndustryRepository,
                              IndustryRepository industryRepository,
                              CloudStorageService cloudStorageService) {
        this.useCaseRepository = useCaseRepository;
        this.useCaseContentRepository = useCaseContentRepository;
        this.useCaseSpeakerRepository = useCaseSpeakerRepository;
        this.useCaseArtifactRepository = useCaseArtifactRepository;
        this.useCaseTagRepository = useCaseTagRepository;
        this.valueChainRepository = valueChainRepository;
        this.subIndustryRepository = subIndustryRepository;
        this.industryRepository = industryRepository;
        this.cloudStorageService = cloudStorageService;
    }

    @Override
    @Transactional
    public UseCaseResponseDTO createUseCaseAndSaveAsDraft(UseCaseRequestDTO requestDTO) {
        logger.info(ManufacturingLabConstants.LOG_SAVING_DRAFT, requestDTO.getTitle());

        // 1. Build UseCase entity
        UseCase useCase = UseCase.builder()
                .valueChainId(requestDTO.getValueChainId())
                .title(requestDTO.getTitle())
                .ownerEid(requestDTO.getOwnerEId())
                .status(requestDTO.getStatus() != null ? requestDTO.getStatus() : UseCaseStatus.DRAFT.name())
                .approverId(requestDTO.getApproverId())
                .isUpdatedUsecase(false)
                .createdDate(LocalDateTime.now())
                .isActive(false)
                .creatorId(requestDTO.getCreatorId())
                .build();

        // 2. Add tags (cascade will persist)
        addTags(useCase, requestDTO);

        // 3. Add speakers (cascade will persist)
        addSpeakers(useCase, requestDTO);

        // 4. Add artifacts (cascade will persist)
        addArtifacts(useCase, requestDTO);

        // 5. Add UseCaseFaqs(cascade will persist)
        addUseCaseFaqs(useCase, requestDTO);

        // 6. Save UseCase (cascade saves speakers, tags, artifacts)
        useCase = useCaseRepository.save(useCase);

        // 7. Save UseCaseContent separately
        UseCaseContent content = UseCaseContent.builder()
                .usecaseId(useCase.getUsecaseId())
                .description(requestDTO.getDescription())
                .businessProblem(requestDTO.getBusinessProblem())
                .solution(requestDTO.getSolutions())
                .toolsAndTechnologies(requestDTO.getToolsAndTechnologies())
                .keyResults(requestDTO.getKeyResults())
                .valueDelivered(requestDTO.getValueDelivered())
                .duration(requestDTO.getDuration())
                .thumbnailUrl(requestDTO.getThumbnailImageUrl())
                .narrationGuide(requestDTO.getNarrationGuide())
                .bannerUrl(requestDTO.getBannerUrl())
                .build();
        useCaseContentRepository.save(content);

        return buildResponseDTO(useCase, content, requestDTO);
    }

    @Override
    @Transactional
    public UseCaseResponseDTO createUseCaseAndSubmitForApproval(UseCaseRequestDTO requestDTO) {
        logger.info(ManufacturingLabConstants.LOG_SUBMITTING_FOR_APPROVAL, requestDTO.getTitle());

        // 1. Build UseCase entity
        UseCase useCase = UseCase.builder()
                .valueChainId(requestDTO.getValueChainId())
                .title(requestDTO.getTitle())
                .ownerEid(String.valueOf(requestDTO.getOwnerEId()))
                .status(requestDTO.getStatus() != null ? requestDTO.getStatus() : UseCaseStatus.IN_REVIEW.name())
                .approverId(requestDTO.getApproverId())
                .isUpdatedUsecase(false)
                .createdDate(LocalDateTime.now())
                .isActive(false)
                .creatorId(requestDTO.getCreatorId())
                .build();

        // 2. Add tags (cascade will persist)
        addTags(useCase, requestDTO);

        // 3. Add speakers (cascade will persist)
        addSpeakers(useCase, requestDTO);

        // 4. Add artifacts (cascade will persist)
        addArtifacts(useCase, requestDTO);

        // 5. Add UseCaseFaqs(cascade will persist)
        addUseCaseFaqs(useCase, requestDTO);

        // 6. Save UseCase (cascade saves speakers, tags, artifacts)
        useCase = useCaseRepository.save(useCase);

        // 7. Save UseCaseContent separately
        UseCaseContent content = UseCaseContent.builder()
                .usecaseId(useCase.getUsecaseId())
                .description(requestDTO.getDescription())
                .businessProblem(requestDTO.getBusinessProblem())
                .solution(requestDTO.getSolutions())
                .toolsAndTechnologies(requestDTO.getToolsAndTechnologies())
                .keyResults(requestDTO.getKeyResults())
                .valueDelivered(requestDTO.getValueDelivered())
                .duration(requestDTO.getDuration())
                .thumbnailUrl(requestDTO.getThumbnailImageUrl())
                .narrationGuide(requestDTO.getNarrationGuide())
                .bannerUrl(requestDTO.getBannerUrl())
                .build();
        useCaseContentRepository.save(content);

        return buildResponseDTO(useCase, content, requestDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public UseCaseResponseDTO getUseCaseById(Integer usecaseId) {
        logger.info(ManufacturingLabConstants.LOG_FETCHING_USE_CASE, usecaseId);

        UseCase useCase = useCaseRepository.findById(usecaseId)
                .orElseThrow(() -> new CommonException(CommonExceptionConstants.NOT_FOUND,
                        ManufacturingLabConstants.USE_CASE_NOT_FOUND + usecaseId));

        UseCaseContent content = useCaseContentRepository.findByUsecaseId(usecaseId).orElse(null);

        return buildResponseFromEntities(useCase, content);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UseCaseResponseDTO> getAllUseCases(int page, int size) {

        logger.info("Fetching use cases with pagination");

        Pageable pageable = PageRequest.of(page - 1, size);

        Page<UseCase> useCases = useCaseRepository.findAll(pageable);

        List<UseCaseResponseDTO> responses = new ArrayList<>();

        for (UseCase useCase : useCases.getContent()) {
            UseCaseContent content = useCaseContentRepository
                    .findByUsecaseId(useCase.getUsecaseId())
                    .orElse(null);

            responses.add(buildResponseFromEntities(useCase, content));
        }

        return new PageImpl<>(responses, pageable, useCases.getTotalElements());
    }

    @Override
    @Transactional
    public UseCaseResponseDTO updateUseCaseandSaveasDraft(Integer usecaseId, UseCaseRequestDTO requestDTO) {

        logger.info(ManufacturingLabConstants.LOG_UPDATING_USE_CASE, usecaseId);

        // Fetch existing record
        UseCase existingUseCase = useCaseRepository.findById(usecaseId)
                .orElseThrow(() -> new CommonException(
                        CommonExceptionConstants.NOT_FOUND,
                        ManufacturingLabConstants.USE_CASE_NOT_FOUND + usecaseId));

        String status = existingUseCase.getStatus();
        Integer parentId = existingUseCase.getParentUsecaseId();

        UseCase useCase;

        if ("DRAFT".equalsIgnoreCase(status) || "IN_REVIEW".equalsIgnoreCase(status) && parentId == null) {

            useCase = existingUseCase;

        } else if ("APPROVED".equalsIgnoreCase(status) && parentId == null) {

            useCase = new UseCase();

            useCase.setValueChainId(existingUseCase.getValueChainId());
            useCase.setCreatorId(existingUseCase.getCreatorId());

            useCase.setOwnerEid(existingUseCase.getOwnerEid());
            useCase.setCreatedDate(LocalDateTime.now());
            useCase.setCreatorId(existingUseCase.getCreatorId());

            useCase.setParentUsecaseId(existingUseCase.getUsecaseId());
            useCase.setStatus("DRAFT");
            useCase.setIsUpdatedUsecase(true);
            useCase.setIsActive(false);

        } else {
            throw new CommonException(CommonExceptionConstants.CONFLICT, "Use case can't be modified in current state"
            );
        }

        useCase.setValueChainId(requestDTO.getValueChainId());
        useCase.setTitle(requestDTO.getTitle());
        useCase.setOwnerEid(String.valueOf(requestDTO.getOwnerEId()));
        useCase.setCreatorId(requestDTO.getCreatorId());
        useCase.setStatus("DRAFT");
        useCase.setApproverId(requestDTO.getApproverId());
        useCase.setIsUpdatedUsecase(true);
        useCase.setUpdatedDate(LocalDateTime.now());

        useCase.setIsActive(false);

        useCase = useCaseRepository.save(useCase);

        UseCaseContent content;

        if (existingUseCase.getUsecaseId().equals(useCase.getUsecaseId())) {

            content = useCaseContentRepository.findByUsecaseId(useCase.getUsecaseId())
                    .orElse(UseCaseContent.builder().usecaseId(useCase.getUsecaseId()).build());
        } else {

            content = new UseCaseContent();
            content.setUsecaseId(useCase.getUsecaseId());
        }

        content.setDescription(requestDTO.getDescription());
        content.setBusinessProblem(requestDTO.getBusinessProblem());
        content.setSolution(requestDTO.getSolutions());
        content.setToolsAndTechnologies(requestDTO.getToolsAndTechnologies());
        content.setKeyResults(requestDTO.getKeyResults());
        content.setValueDelivered(requestDTO.getValueDelivered());
        content.setDuration(requestDTO.getDuration());
        content.setThumbnailUrl(requestDTO.getThumbnailImageUrl());
        content.setBannerUrl(requestDTO.getBannerUrl());
        content.setNarrationGuide(requestDTO.getNarrationGuide());

        useCaseContentRepository.save(content);

        if (existingUseCase.getUsecaseId().equals(useCase.getUsecaseId())) {

            useCase.getTags().clear();
            useCase.getSpeakers().clear();
            useCase.getArtifacts().clear();
            useCase.getFaqs().clear();
        }

        addTags(useCase, requestDTO);
        addSpeakers(useCase, requestDTO);
        addArtifacts(useCase, requestDTO);
        addUseCaseFaqs(useCase, requestDTO);

        useCaseRepository.save(useCase);

        return buildResponseFromEntities(useCase, content);
    }

    @Override
    @Transactional
    public void deleteUseCase(Integer usecaseId) {
        logger.info(ManufacturingLabConstants.LOG_DELETING_USE_CASE, usecaseId);

        UseCase useCase = useCaseRepository.findById(usecaseId)
                .orElseThrow(() -> new CommonException(CommonExceptionConstants.NOT_FOUND,
                        ManufacturingLabConstants.USE_CASE_NOT_FOUND + usecaseId));

        useCase.setIsActive(false);
        useCase.setUpdatedDate(LocalDateTime.now());
        useCaseRepository.save(useCase);
    }

    // ==================== PRIVATE HELPERS ====================

    private void addTags(UseCase useCase, UseCaseRequestDTO dto) {

        if (dto.getTags() != null && !dto.getTags().isEmpty()) {

            for (String tagValue : dto.getTags()) {

                if (tagValue != null && !tagValue.isBlank()) {

                    UseCaseTag tag = UseCaseTag.builder()
                            .useCase(useCase)
                            .tag(tagValue)
                            .build();

                    useCase.getTags().add(tag);
                }
            }
        }
    }

    private void addSpeakers(UseCase useCase, UseCaseRequestDTO dto) {

        if (dto.getSpeakers() != null && !dto.getSpeakers().isEmpty()) {

            Set<String> uniqueEids = new HashSet<>();

            for (SpeakerDTO speakerDTO : dto.getSpeakers()) {

                if (speakerDTO.getSpeakerEid() != null && !speakerDTO.getSpeakerEid().isBlank()) {

                    boolean isAdded = uniqueEids.add(speakerDTO.getSpeakerEid());

                    if (!isAdded) {
                        throw new CommonException(
                                CommonExceptionConstants.BAD_REQUEST,
                                "Primary, Secondary, and Tertiary speaker EIDs must be unique"
                        );
                    }
                }
            }

            for (SpeakerDTO speakerDTO : dto.getSpeakers()) {

                if (speakerDTO.getSpeakerEid() != null && !speakerDTO.getSpeakerEid().isBlank()) {

                    UseCaseSpeaker speaker = UseCaseSpeaker.builder().speakerEid(speakerDTO.getSpeakerEid())
                            .speakerType(speakerDTO.getSpeakerType())
                            .build();

                    useCase.addSpeaker(speaker);
                }
            }
        }
    }

    private void addArtifacts(UseCase useCase, UseCaseRequestDTO dto) {

        if (dto.getArtifacts() != null && !dto.getArtifacts().isEmpty()) {

            for (ArtifactDTO artifactDTO : dto.getArtifacts()) {

                if (artifactDTO.getUrl() != null && !artifactDTO.getUrl().isBlank()) {

                    UseCaseArtifact artifact = UseCaseArtifact.builder()
                            .useCase(useCase)
                            .artifactType(artifactDTO.getArtifactType())
                            .url(artifactDTO.getUrl())
                            .artifactName(artifactDTO.getArtifactName())
                            .build();

                    useCase.getArtifacts().add(artifact);
                }
            }
        }
    }

    private void addUseCaseFaqs(UseCase useCase, UseCaseRequestDTO dto) {

        Integer updatedBy = dto.getCreatorId();

        if (dto.getFaq() != null && !dto.getFaq().isEmpty()) {
            for (FaqDTO faqDTO : dto.getFaq()) {
                if (faqDTO.getQuestion() != null && !faqDTO.getQuestion().isBlank()
                        && faqDTO.getAnswer() != null && !faqDTO.getAnswer().isBlank()) {
                    UseCaseFaq faq = UseCaseFaq.builder()
                            .useCase(useCase)
                            .question(faqDTO.getQuestion())
                            .answer(faqDTO.getAnswer())
                            .updatedBy(updatedBy)
                            .lastUpdated(LocalDateTime.now())
                            .build();
                    useCase.addFaq(faq);
                }
            }
        }
    }

    private UseCaseResponseDTO buildResponseDTO(UseCase useCase, UseCaseContent content, UseCaseRequestDTO requestDTO) {
        ValueChain valueChain = valueChainRepository.findById(useCase.getValueChainId().longValue()).orElse(null);

        Long industryId = null;
        Long subIndustryId = null;

        if (valueChain != null) {

            SubIndustry subIndustry = subIndustryRepository.findById(valueChain.getSubIndustryId()).orElse(null);

            if (subIndustry != null) {
                subIndustryId = subIndustry.getSubIndustryId();

                Industry industry = industryRepository.findById(subIndustry.getIndustryId()).orElse(null);

                if (industry != null) {
                    industryId = industry.getIndustryId();
                }
            }
        }
        return UseCaseResponseDTO.builder()
                .usecaseId(useCase.getUsecaseId())
                .industryId(industryId)
                .subIndustryId(subIndustryId)
                .valueChainId(useCase.getValueChainId())
                .title(useCase.getTitle())
                .thumbnailImageUrl(content.getThumbnailUrl())
                .tag(useCase.getTags().stream().map(UseCaseTag::getTag).toList())
                .description(content.getDescription())
                .duration(content.getDuration())
                .ownerEId(String.valueOf(requestDTO.getOwnerEId()))
                .speakers(useCase.getSpeakers().stream().map(s -> new SpeakerDTO(s.getSpeakerEid(), s.getSpeakerType())).toList())
                .artifacts(useCase.getArtifacts().stream().map(a -> new ArtifactDTO(a.getArtifactType(), a.getUrl(), a.getArtifactName())).toList())
                .faqs(useCase.getFaqs() == null ? List.of() :
                        useCase.getFaqs().stream()
                                .map(f -> FaqDTO.builder()
                                        .question(f.getQuestion())
                                        .answer(f.getAnswer())
                                        .updatedBy(f.getUpdatedBy())
                                        .lastUpdated(f.getLastUpdated())
                                        .build())
                                .toList()
                )
                .businessProblem(content.getBusinessProblem())
                .solutions(content.getSolution())
                .valueDelivered(content.getValueDelivered())
                .toolsAndTechnologies(content.getToolsAndTechnologies())
                .keyResults(content.getKeyResults())
                .status(useCase.getStatus())
                .approverId(useCase.getApproverId())
                .approvedDate(useCase.getApprovedDate())
                .createdDate(useCase.getCreatedDate())
                .updatedDate(useCase.getUpdatedDate())
                .isActive(useCase.getIsActive())
                .creatorId(useCase.getCreatorId())
                .narrationGuide(content.getNarrationGuide())
                .bannerUrl(content.getBannerUrl())
                .creatorId(useCase.getCreatorId())
                .build();
    }

    private UseCaseResponseDTO buildResponseFromEntities(UseCase useCase, UseCaseContent content) {
        ValueChain valueChain = valueChainRepository.findById(useCase.getValueChainId().longValue()).orElse(null);

        Long industryId = null;
        Long subIndustryId = null;

        if (valueChain != null) {

            SubIndustry subIndustry = subIndustryRepository.findById(valueChain.getSubIndustryId()).orElse(null);

            if (subIndustry != null) {
                subIndustryId = subIndustry.getSubIndustryId();

                Industry industry = industryRepository.findById(subIndustry.getIndustryId()).orElse(null);

                if (industry != null) {
                    industryId = industry.getIndustryId();
                }
            }
        }

        UseCaseResponseDTO.UseCaseResponseDTOBuilder builder = UseCaseResponseDTO.builder()
                .usecaseId(useCase.getUsecaseId())
                .industryId(industryId)
                .subIndustryId(subIndustryId)
                .valueChainId(useCase.getValueChainId())
                .title(useCase.getTitle())
                .ownerEId(useCase.getOwnerEid())
                .status(useCase.getStatus())
                .approverId(useCase.getApproverId())
                .approvedDate(useCase.getApprovedDate())
                .createdDate(useCase.getCreatedDate())
                .updatedDate(useCase.getUpdatedDate())
                .isActive(useCase.getIsActive())
                .creatorId(useCase.getCreatorId());


        if (content != null) {
            builder.description(content.getDescription())
                    .businessProblem(content.getBusinessProblem())
                    .solutions(content.getSolution())
                    .toolsAndTechnologies(content.getToolsAndTechnologies())
                    .keyResults(content.getKeyResults())
                    .valueDelivered(content.getValueDelivered())
                    .duration(content.getDuration())
                    .thumbnailImageUrl(content.getThumbnailUrl())
                    .narrationGuide(content.getNarrationGuide())
                    .bannerUrl(content.getBannerUrl());
        }

        List<UseCaseTag> tags = useCase.getTags();

        if (tags != null && !tags.isEmpty()) {
            List<String> tagList = tags.stream()
                    .map(UseCaseTag::getTag)
                    .toList();
            builder.tag(tagList);
        }


        if (useCase.getSpeakers() != null) {
            builder.speakers(
                    useCase.getSpeakers()
                            .stream()
                            .map(s -> new SpeakerDTO(s.getSpeakerEid(), s.getSpeakerType()))
                            .toList()
            );
        }

        // Artifacts
        if (useCase.getArtifacts() != null) {
            builder.artifacts(
                    useCase.getArtifacts()
                            .stream()
                            .map(a -> new ArtifactDTO(a.getArtifactType(), a.getUrl(), a.getArtifactName()))
                            .toList()
            );
        }

        // FAQs
        if (useCase.getFaqs() != null && !useCase.getFaqs().isEmpty()) {
            builder.faqs(
                    useCase.getFaqs().stream()
                            .map(f -> FaqDTO.builder()
                                    .question(f.getQuestion())
                                    .answer(f.getAnswer())
                                    .updatedBy(f.getUpdatedBy())   // change to String.valueOf(...) if DTO expects String
                                    .build()
                            )
                            .toList()
            );
        }

        return builder.build();
    }

    private Integer parseInteger(String value) {
        try {
            return value != null ? Integer.parseInt(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    @Transactional
    public void archiveApprovedUseCase(Integer usecaseId) {

        logger.info("Archiving use case with id {}", usecaseId);

        UseCase useCase = useCaseRepository.findById(usecaseId)
                .orElseThrow(() -> new CommonException(
                        CommonExceptionConstants.NOT_FOUND,
                        ManufacturingLabConstants.USE_CASE_NOT_FOUND + usecaseId));

        if (!"APPROVED".equalsIgnoreCase(useCase.getStatus())) {
            throw new CommonException(
                    CommonExceptionConstants.BAD_REQUEST,
                    "Only APPROVED use cases can be archived"
            );
        }

        useCase.setStatus("ARCHIVED");
        useCase.setUpdatedDate(LocalDateTime.now());

        useCaseRepository.save(useCase);
    }

    @Transactional
    public void discardDraftUseCase(Integer usecaseId) {

        UseCase useCase = useCaseRepository
                .findByUsecaseIdAndStatus(usecaseId, "DRAFT")
                .orElseThrow(() ->
                        new CommonException("400", "Only draft use cases can be discarded or use case not found")
                );

        useCaseContentRepository.deleteByUsecaseId(usecaseId);
        useCaseArtifactRepository.deleteByUseCase_UsecaseId(usecaseId);
        useCaseTagRepository.deleteByUseCase_UsecaseId(usecaseId);
        useCaseSpeakerRepository.deleteByUseCase_UsecaseId(usecaseId);
        useCaseRepository.delete(useCase);
    }

    @Override
    @Transactional
    public UseCaseResponseDTO updateUseCaseandsubmitForApproval(Integer usecaseId, UseCaseRequestDTO requestDTO) {

        logger.info(ManufacturingLabConstants.LOG_UPDATING_USE_CASE, usecaseId);

        UseCase existingUseCase = useCaseRepository.findById(usecaseId)
                .orElseThrow(() -> new CommonException(
                        CommonExceptionConstants.NOT_FOUND,
                        ManufacturingLabConstants.USE_CASE_NOT_FOUND + usecaseId));

        String status = existingUseCase.getStatus();
        Integer parentId = existingUseCase.getParentUsecaseId();

        UseCase useCase;

        if (("DRAFT".equalsIgnoreCase(status) || "IN_REVIEW".equalsIgnoreCase(status)) && parentId == null) {

            useCase = existingUseCase;

        } else if ("APPROVED".equalsIgnoreCase(status) && parentId == null) {

            useCase = new UseCase();


            useCase.setValueChainId(existingUseCase.getValueChainId());
            useCase.setCreatorId(existingUseCase.getCreatorId());
            useCase.setOwnerEid(existingUseCase.getOwnerEid());

            useCase.setCreatedDate(LocalDateTime.now());
            useCase.setCreatorId(existingUseCase.getCreatorId());

            useCase.setParentUsecaseId(existingUseCase.getUsecaseId());
            useCase.setStatus("IN_REVIEW");
            useCase.setIsUpdatedUsecase(true);
            useCase.setIsActive(false);
        } else {
            throw new CommonException(CommonExceptionConstants.CONFLICT, "Use case cannot be modified in current state"
            );
        }

        useCase.setTitle(requestDTO.getTitle());
        useCase.setValueChainId(requestDTO.getValueChainId());
        useCase.setOwnerEid(String.valueOf(requestDTO.getOwnerEId()));
        useCase.setApproverId(requestDTO.getApproverId());
        useCase.setCreatorId(requestDTO.getCreatorId());
        useCase.setStatus("IN_REVIEW");
        useCase.setApproverId(requestDTO.getApproverId());

        useCase.setIsActive(false);
        useCase.setIsUpdatedUsecase(true);
        useCase.setStatus("IN_REVIEW");
        useCase.setUpdatedDate(LocalDateTime.now());

        useCase = useCaseRepository.save(useCase);

        UseCaseContent content;

        if (existingUseCase.getUsecaseId().equals(useCase.getUsecaseId())) {
            content = useCaseContentRepository.findByUsecaseId(useCase.getUsecaseId())
                    .orElse(new UseCaseContent());
            content.setUsecaseId(useCase.getUsecaseId());
        } else {
            content = new UseCaseContent();
            content.setUsecaseId(useCase.getUsecaseId());
        }

        content.setDescription(requestDTO.getDescription());
        content.setBusinessProblem(requestDTO.getBusinessProblem());
        content.setSolution(requestDTO.getSolutions());
        content.setToolsAndTechnologies(requestDTO.getToolsAndTechnologies());
        content.setKeyResults(requestDTO.getKeyResults());
        content.setValueDelivered(requestDTO.getValueDelivered());
        content.setDuration(requestDTO.getDuration());
        content.setThumbnailUrl(requestDTO.getThumbnailImageUrl());
        content.setBannerUrl(requestDTO.getBannerUrl());
        content.setNarrationGuide(requestDTO.getNarrationGuide());

        useCaseContentRepository.save(content);

        if (existingUseCase.getUsecaseId().equals(useCase.getUsecaseId())) {
            useCase.getTags().clear();
            useCase.getSpeakers().clear();
            useCase.getArtifacts().clear();
            useCase.getFaqs().clear();
        }

        addTags(useCase, requestDTO);
        addSpeakers(useCase, requestDTO);
        addArtifacts(useCase, requestDTO);
        addUseCaseFaqs(useCase, requestDTO);

        useCaseRepository.save(useCase);

        return buildResponseFromEntities(useCase, content);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<UseCaseResponseDTO> getAllArchiveUseCases(int page, int size) {

        return useCaseRepository
                .findByStatus("ARCHIVED", PageRequest.of(page - 1, size))
                .map(useCase -> buildResponseFromEntities(
                        useCase,
                        useCaseContentRepository.findByUsecaseId(useCase.getUsecaseId()).orElse(null)
                ));
    }

    @Override
    @Transactional
    public UseCaseResponseDTO updateUseCaseBySuperAdmin(Integer usecaseId,
                                                        UseCaseRequestDTO requestDTO) {

        logger.info(ManufacturingLabConstants.LOG_UPDATING_USE_CASE, usecaseId);

        UseCase existingUseCase = useCaseRepository.findById(usecaseId)
                .orElseThrow(() -> new CommonException(
                        CommonExceptionConstants.NOT_FOUND,
                        ManufacturingLabConstants.USE_CASE_NOT_FOUND + usecaseId));

        String status = existingUseCase.getStatus();
        Integer parentId = existingUseCase.getParentUsecaseId();

        UseCase useCase;

        if ("DRAFT".equalsIgnoreCase(status)) {

            useCase = existingUseCase;
        } else if ("APPROVED".equalsIgnoreCase(status)) {

            existingUseCase.setIsActive(false);
            useCaseRepository.save(existingUseCase);

            useCase = new UseCase();

            useCase.setValueChainId(existingUseCase.getValueChainId());
            useCase.setCreatorId(existingUseCase.getCreatorId());
            useCase.setOwnerEid(existingUseCase.getOwnerEid());
            useCase.setCreatedDate(LocalDateTime.now());
            useCase.setParentUsecaseId(existingUseCase.getUsecaseId());
            useCase.setApprovedDate(LocalDateTime.now());
            useCase.setUpdatedDate(LocalDateTime.now());
        } else {
            throw new CommonException(
                    CommonExceptionConstants.CONFLICT,
                    "Use case cannot be modified in current state"
            );
        }


        useCase.setTitle(requestDTO.getTitle());
        useCase.setValueChainId(requestDTO.getValueChainId());
        useCase.setOwnerEid(String.valueOf(requestDTO.getOwnerEId()));
        useCase.setApproverId(requestDTO.getCreatorId());
        useCase.setCreatorId(requestDTO.getCreatorId());

        useCase.setStatus("APPROVED");
        useCase.setApprovedDate(LocalDateTime.now());

        useCase.setIsActive(true);
        useCase.setIsUpdatedUsecase(true);
        useCase.setUpdatedDate(LocalDateTime.now());

        useCase = useCaseRepository.save(useCase);

        UseCaseContent content;

        if (existingUseCase.getUsecaseId().equals(useCase.getUsecaseId())) {
            content = useCaseContentRepository.findByUsecaseId(useCase.getUsecaseId())
                    .orElse(new UseCaseContent());
            content.setUsecaseId(useCase.getUsecaseId());
        } else {
            content = new UseCaseContent();
            content.setUsecaseId(useCase.getUsecaseId());
        }

        content.setDescription(requestDTO.getDescription());
        content.setBusinessProblem(requestDTO.getBusinessProblem());
        content.setSolution(requestDTO.getSolutions());
        content.setToolsAndTechnologies(requestDTO.getToolsAndTechnologies());
        content.setKeyResults(requestDTO.getKeyResults());
        content.setValueDelivered(requestDTO.getValueDelivered());
        content.setDuration(requestDTO.getDuration());
        content.setThumbnailUrl(requestDTO.getThumbnailImageUrl());
        content.setBannerUrl(requestDTO.getBannerUrl());
        content.setNarrationGuide(requestDTO.getNarrationGuide());

        useCaseContentRepository.save(content);

        if (existingUseCase.getUsecaseId().equals(useCase.getUsecaseId())) {
            useCase.getTags().clear();
            useCase.getSpeakers().clear();
            useCase.getArtifacts().clear();
            useCase.getFaqs().clear();
        }

        addTags(useCase, requestDTO);
        addSpeakers(useCase, requestDTO);
        addArtifacts(useCase, requestDTO);
        addUseCaseFaqs(useCase, requestDTO);

        useCaseRepository.save(useCase);

        return buildResponseFromEntities(useCase, content);
    }

    @Override
    @Transactional
    public UseCaseResponseDTO createUseCaseAndSaveAsDraftWithBlob(UseCaseRequestDTO requestDTO, MultipartFile clientTestimonials) {
        logger.info(ManufacturingLabConstants.LOG_SAVING_DRAFT, requestDTO.getTitle());
// 1. Build UseCase entity
        UseCase useCase = UseCase.builder()
                .valueChainId(requestDTO.getValueChainId())
                .title(requestDTO.getTitle())
                .ownerEid(requestDTO.getOwnerEId())
                .status(requestDTO.getStatus() != null ? requestDTO.getStatus() : UseCaseStatus.DRAFT.name())
                .approverId(requestDTO.getApproverId())
                .isUpdatedUsecase(false)
                .createdDate(LocalDateTime.now())
                .isActive(false)
                .creatorId(requestDTO.getCreatorId())
                .build();
// 2. Add tags (cascade will persist)
        addTags(useCase, requestDTO);
// 3. Add speakers (cascade will persist)
        addSpeakers(useCase, requestDTO);
// 4. Add artifacts (cascade will persist)
        addArtifacts(useCase, requestDTO);
// 5. Add UseCaseFaqs(cascade will persist)
        addUseCaseFaqs(useCase, requestDTO);
// 6. Save UseCase (cascade saves speakers, tags, artifacts)
        useCase = useCaseRepository.save(useCase);
// 6a. Upload client testimonials file to Azure Blob Storage
        if (clientTestimonials != null && !clientTestimonials.isEmpty()) {
            String blobPath = clientTestimonialsPath.replace("{usecase-id}", String.valueOf(useCase.getUsecaseId()))
                    + "/" + clientTestimonials.getOriginalFilename();
            logger.info("Uploading client testimonials to blob storage at path: {}", blobPath);
            String blobUrl = cloudStorageService.uploadFile(clientTestimonials, blobPath);
            logger.info("Client testimonials uploaded successfully. Blob URL: {}", blobUrl);

        }
// 7. Save UseCaseContent separately
        UseCaseContent content = UseCaseContent.builder()
                .usecaseId(useCase.getUsecaseId())
                .description(requestDTO.getDescription())
                .businessProblem(requestDTO.getBusinessProblem())
                .solution(requestDTO.getSolutions())
                .toolsAndTechnologies(requestDTO.getToolsAndTechnologies())
                .keyResults(requestDTO.getKeyResults())
                .valueDelivered(requestDTO.getValueDelivered())
                .duration(requestDTO.getDuration())
                .thumbnailUrl(requestDTO.getThumbnailImageUrl())
                .narrationGuide(requestDTO.getNarrationGuide())
                .bannerUrl(requestDTO.getBannerUrl())
                .build();
        useCaseContentRepository.save(content);
        return buildResponseDTO(useCase, content, requestDTO);
    }

}
