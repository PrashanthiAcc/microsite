package com.ix.manufacturinglab.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.ix.manufacturinglab.dto.*;
import com.ix.manufacturinglab.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.ix.manufacturinglab.repository.UseCaseContentRepository;
import com.ix.manufacturinglab.repository.UseCaseRepository;
import com.ix.manufacturinglab.repository.UseCaseSpeakerRepository;
import com.ix.manufacturinglab.repository.UseCaseArtifactRepository;
import com.ix.manufacturinglab.repository.UseCaseTagRepository;
import com.ix.manufacturinglab.service.UseCaseService;

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

    public UseCaseServiceImpl(UseCaseRepository useCaseRepository,
                              UseCaseContentRepository useCaseContentRepository,
                              UseCaseSpeakerRepository useCaseSpeakerRepository,
                              UseCaseArtifactRepository useCaseArtifactRepository,
                              UseCaseTagRepository useCaseTagRepository) {
        this.useCaseRepository = useCaseRepository;
        this.useCaseContentRepository = useCaseContentRepository;
        this.useCaseSpeakerRepository = useCaseSpeakerRepository;
        this.useCaseArtifactRepository = useCaseArtifactRepository;
        this.useCaseTagRepository = useCaseTagRepository;
    }

    @Override
    @Transactional
    public UseCaseResponseDTO saveAsDraft(UseCaseRequestDTO requestDTO) {
        logger.info(ManufacturingLabConstants.LOG_SAVING_DRAFT, requestDTO.getTitle());

        // 1. Build UseCase entity
        UseCase useCase = UseCase.builder()
                .valueChainId(requestDTO.getValueChainId())
                .industryId(requestDTO.getIndustryId())
                .subIndustryId(requestDTO.getSubIndustryId())
                .title(requestDTO.getTitle())
                .ownerEid(String.valueOf(requestDTO.getOwnerId()))
                .status(requestDTO.getStatus() != null ? requestDTO.getStatus() : UseCaseStatus.DRAFT.name())
                .approverId(requestDTO.getApproverId())
                .isUpdatedUsecase(false)
                .createdDate(LocalDateTime.now())
                .isActive(requestDTO.getIsActive() != null ? requestDTO.getIsActive() : false)
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
    public UseCaseResponseDTO submitForApproval(UseCaseRequestDTO requestDTO) {
        logger.info(ManufacturingLabConstants.LOG_SUBMITTING_FOR_APPROVAL, requestDTO.getTitle());

        // 1. Build UseCase entity
        UseCase useCase = UseCase.builder()
                .valueChainId(requestDTO.getValueChainId())
                .industryId(requestDTO.getIndustryId())
                .subIndustryId(requestDTO.getSubIndustryId())
                .title(requestDTO.getTitle())
                .ownerEid(String.valueOf(requestDTO.getOwnerId()))
                .status(requestDTO.getStatus() != null ? requestDTO.getStatus() : UseCaseStatus.IN_REVIEW.name())
                .approverId(requestDTO.getApproverId())
                .isUpdatedUsecase(false)
                .createdDate(LocalDateTime.now())
                .isActive(requestDTO.getIsActive() != null ? requestDTO.getIsActive() : false)
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
    public Page<UseCaseResponseDTO> getAllActiveUseCases(int page, int size) {

        logger.info("Fetching active use cases with pagination");

        Pageable pageable = PageRequest.of(page-1, size);

        Page<UseCase> useCases = useCaseRepository.findByIsActiveTrue(pageable);

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
    public UseCaseResponseDTO updateUseCase(Integer usecaseId, UseCaseRequestDTO requestDTO) {
        logger.info(ManufacturingLabConstants.LOG_UPDATING_USE_CASE, usecaseId);

        UseCase useCase = useCaseRepository.findById(usecaseId)
                .orElseThrow(() -> new CommonException(CommonExceptionConstants.NOT_FOUND,
                        ManufacturingLabConstants.USE_CASE_NOT_FOUND + usecaseId));

        // Update UseCase fields
        useCase.setValueChainId(requestDTO.getValueChainId());
        useCase.setTitle(requestDTO.getTitle());
        useCase.setOwnerEid(String.valueOf(requestDTO.getOwnerId()));
        useCase.setStatus(requestDTO.getStatus() != null ? requestDTO.getStatus() : useCase.getStatus());
        useCase.setApproverId(requestDTO.getApproverId());
        useCase.setIsUpdatedUsecase(true);
        useCase.setUpdatedDate(LocalDateTime.now());
        if (requestDTO.getIsActive() != null) {
            useCase.setIsActive(requestDTO.getIsActive());
        }

        // Update UseCaseContent
        UseCaseContent content = useCaseContentRepository.findByUsecaseId(usecaseId)
                .orElse(UseCaseContent.builder().usecaseId(usecaseId).build());
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

        // Replace tags (orphanRemoval deletes old ones)
        useCase.getTags().clear();
        addTags(useCase, requestDTO);

        // Replace speakers (orphanRemoval deletes old ones)
        useCase.getSpeakers().clear();
        addSpeakers(useCase, requestDTO);

        // Replace artifacts (orphanRemoval deletes old ones)
        useCase.getArtifacts().clear();
        addArtifacts(useCase, requestDTO);

        useCase.getFaqs().clear();
        addUseCaseFaqs(useCase, requestDTO);

        useCaseRepository.save(useCase);

        return buildResponseDTO(useCase, content, requestDTO);
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
        return UseCaseResponseDTO.builder()
                .usecaseId(useCase.getUsecaseId())
                .industryId(requestDTO.getIndustryId())
                .subIndustryId(requestDTO.getSubIndustryId())
                .valueChainId(useCase.getValueChainId())
                .title(useCase.getTitle())
                .thumbnailImageUrl(content.getThumbnailUrl())
                .tag(useCase.getTags().stream().map(UseCaseTag::getTag).toList())
                .description(content.getDescription())
                .duration(content.getDuration())
                .ownerId(requestDTO.getOwnerId())
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
                .build();
    }

    private UseCaseResponseDTO buildResponseFromEntities(UseCase useCase, UseCaseContent content) {
        UseCaseResponseDTO.UseCaseResponseDTOBuilder builder = UseCaseResponseDTO.builder()
                .usecaseId(useCase.getUsecaseId())
                .industryId(useCase.getIndustryId())
                .subIndustryId((useCase.getSubIndustryId()))
                .valueChainId(useCase.getValueChainId())
                .title(useCase.getTitle())
                .ownerId(parseInteger(useCase.getOwnerEid()))
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
                    .thumbnailImageUrl(content.getThumbnailUrl());
        }

        List<UseCaseTag> tags = useCase.getTags();

        if (tags != null && !tags.isEmpty()) {
            List<String> tagList = tags.stream()
                    .map(UseCaseTag::getTag)
                    .toList(); builder.tag(tagList);
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
                            .map(a -> new ArtifactDTO(a.getArtifactType(), a.getUrl(),a.getArtifactName()))
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
    public void archiveUseCase(Integer usecaseId) {

        logger.info("Archiving use case with id {}", usecaseId);

        UseCase useCase = useCaseRepository.findById(usecaseId)
                .orElseThrow(() -> new CommonException(
                        CommonExceptionConstants.NOT_FOUND,
                        ManufacturingLabConstants.USE_CASE_NOT_FOUND + usecaseId));

        useCase.setStatus("ARCHIVE");
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

}
