package com.ix.manufacturinglab.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ix.manufacturinglab.constants.CommonExceptionConstants;
import com.ix.manufacturinglab.constants.ManufacturingLabConstants;
import com.ix.manufacturinglab.dto.UseCaseRequestDTO;
import com.ix.manufacturinglab.dto.UseCaseResponseDTO;
import com.ix.manufacturinglab.entity.UseCase;
import com.ix.manufacturinglab.entity.UseCaseArtifact;
import com.ix.manufacturinglab.entity.UseCaseContent;
import com.ix.manufacturinglab.entity.UseCaseSpeaker;
import com.ix.manufacturinglab.entity.UseCaseTag;
import com.ix.manufacturinglab.enums.UseCaseStatus;
import com.ix.manufacturinglab.exception.CommonException;
import com.ix.manufacturinglab.repository.UseCaseContentRepository;
import com.ix.manufacturinglab.repository.UseCaseRepository;
import com.ix.manufacturinglab.service.UseCaseService;

/**
 * Implementation of UseCaseService that persists data across multiple entity tables.
 */
@Service
public class UseCaseServiceImpl implements UseCaseService {

    private static final Logger logger = LoggerFactory.getLogger(UseCaseServiceImpl.class);

    private final UseCaseRepository useCaseRepository;
    private final UseCaseContentRepository useCaseContentRepository;

    public UseCaseServiceImpl(UseCaseRepository useCaseRepository,
                              UseCaseContentRepository useCaseContentRepository) {
        this.useCaseRepository = useCaseRepository;
        this.useCaseContentRepository = useCaseContentRepository;
    }

    @Override
    @Transactional
    public UseCaseResponseDTO createUseCase(UseCaseRequestDTO requestDTO) {
        logger.info(ManufacturingLabConstants.LOG_CREATING_USE_CASE, requestDTO.getTitle());

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
                .isActive(requestDTO.getIsActive() != null ? requestDTO.getIsActive() : true)
                .creatorId(requestDTO.getCreatorId())
                .build();

        // 2. Add tags (cascade will persist)
        addTags(useCase, requestDTO);

        // 3. Add speakers (cascade will persist)
        addSpeakers(useCase, requestDTO);

        // 4. Add artifacts (cascade will persist)
        addArtifacts(useCase, requestDTO);

        // 5. Save UseCase (cascade saves speakers, tags, artifacts)
        useCase = useCaseRepository.save(useCase);

        // 6. Save UseCaseContent separately
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
    public List<UseCaseResponseDTO> getAllActiveUseCases() {
        logger.info("Fetching all active use cases");

        List<UseCase> useCases = useCaseRepository.findByIsActiveTrue();
        List<UseCaseResponseDTO> responses = new ArrayList<>();

        for (UseCase useCase : useCases) {
            UseCaseContent content = useCaseContentRepository.findByUsecaseId(useCase.getUsecaseId()).orElse(null);
            responses.add(buildResponseFromEntities(useCase, content));
        }

        return responses;
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
        if (dto.getTag() != null && !dto.getTag().isBlank()) {
            UseCaseTag tag = UseCaseTag.builder()
                    .useCase(useCase)
                    .tag(dto.getTag())
                    .build();
            useCase.getTags().add(tag);
        }
    }

    private void addSpeakers(UseCase useCase, UseCaseRequestDTO dto) {
        if (dto.getPrimarySpeakerEid() != null && !dto.getPrimarySpeakerEid().isBlank()) {
            useCase.getSpeakers().add(UseCaseSpeaker.builder()
                    .useCase(useCase)
                    .speakerEid(dto.getPrimarySpeakerEid())
                    .speakerType("PRIMARY")
                    .build());
        }
        if (dto.getSecondarySpeakerEid() != null && !dto.getSecondarySpeakerEid().isBlank()) {
            useCase.getSpeakers().add(UseCaseSpeaker.builder()
                    .useCase(useCase)
                    .speakerEid(dto.getSecondarySpeakerEid())
                    .speakerType("SECONDARY")
                    .build());
        }
        if (dto.getTertiarySpeakerEid() != null && !dto.getTertiarySpeakerEid().isBlank()) {
            useCase.getSpeakers().add(UseCaseSpeaker.builder()
                    .useCase(useCase)
                    .speakerEid(dto.getTertiarySpeakerEid())
                    .speakerType("TERTIARY")
                    .build());
        }
    }

    private void addArtifacts(UseCase useCase, UseCaseRequestDTO dto) {
        if (dto.getOneSliderUrl() != null && !dto.getOneSliderUrl().isBlank()) {
            useCase.getArtifacts().add(UseCaseArtifact.builder()
                    .useCase(useCase)
                    .artifactType("ONE_SLIDER")
                    .url(dto.getOneSliderUrl())
                    .build());
        }
        if (dto.getMultiSliderUrl() != null && !dto.getMultiSliderUrl().isBlank()) {
            useCase.getArtifacts().add(UseCaseArtifact.builder()
                    .useCase(useCase)
                    .artifactType("MULTI_SLIDER")
                    .url(dto.getMultiSliderUrl())
                    .build());
        }
        if (dto.getDemoVideoUrl() != null && !dto.getDemoVideoUrl().isBlank()) {
            useCase.getArtifacts().add(UseCaseArtifact.builder()
                    .useCase(useCase)
                    .artifactType("DEMO_VIDEO")
                    .url(dto.getDemoVideoUrl())
                    .build());
        }
        if (dto.getClientTestimonialDemoUrl() != null && !dto.getClientTestimonialDemoUrl().isBlank()) {
            useCase.getArtifacts().add(UseCaseArtifact.builder()
                    .useCase(useCase)
                    .artifactType("CLIENT_TESTIMONIAL")
                    .url(dto.getClientTestimonialDemoUrl())
                    .build());
        }
        if (dto.getNarrationVideoUrl() != null && !dto.getNarrationVideoUrl().isBlank()) {
            useCase.getArtifacts().add(UseCaseArtifact.builder()
                    .useCase(useCase)
                    .artifactType("NARRATION")
                    .url(dto.getNarrationVideoUrl())
                    .build());
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
                .tag(requestDTO.getTag())
                .description(content.getDescription())
                .duration(content.getDuration())
                .ownerId(requestDTO.getOwnerId())
                .primarySpeakerEid(requestDTO.getPrimarySpeakerEid())
                .secondarySpeakerEid(requestDTO.getSecondarySpeakerEid())
                .tertiarySpeakerEid(requestDTO.getTertiarySpeakerEid())
                .oneSliderUrl(requestDTO.getOneSliderUrl())
                .multiSliderUrl(requestDTO.getMultiSliderUrl())
                .demoVideoUrl(requestDTO.getDemoVideoUrl())
                .clientTestimonialDemoUrl(requestDTO.getClientTestimonialDemoUrl())
                .narrationVideoUrl(requestDTO.getNarrationVideoUrl())
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

        // Content fields
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

        // Tags
        List<UseCaseTag> tags = useCase.getTags();
        if (tags != null && !tags.isEmpty()) {
            builder.tag(tags.get(0).getTag());
        }

        // Speakers
        List<UseCaseSpeaker> speakers = useCase.getSpeakers();
        if (speakers != null) {
            for (UseCaseSpeaker speaker : speakers) {
                switch (speaker.getSpeakerType()) {
                    case "PRIMARY" -> builder.primarySpeakerEid(speaker.getSpeakerEid());
                    case "SECONDARY" -> builder.secondarySpeakerEid(speaker.getSpeakerEid());
                    case "TERTIARY" -> builder.tertiarySpeakerEid(speaker.getSpeakerEid());
                }
            }
        }

        // Artifacts
        List<UseCaseArtifact> artifacts = useCase.getArtifacts();
        if (artifacts != null) {
            for (UseCaseArtifact artifact : artifacts) {
                switch (artifact.getArtifactType()) {
                    case "ONE_SLIDER" -> builder.oneSliderUrl(artifact.getUrl());
                    case "MULTI_SLIDER" -> builder.multiSliderUrl(artifact.getUrl());
                    case "DEMO_VIDEO" -> builder.demoVideoUrl(artifact.getUrl());
                    case "CLIENT_TESTIMONIAL" -> builder.clientTestimonialDemoUrl(artifact.getUrl());
                    case "NARRATION" -> builder.narrationVideoUrl(artifact.getUrl());
                }
            }
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
}
