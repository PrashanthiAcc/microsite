package com.ix.manufacturinglab.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

import com.azure.storage.blob.BlobContainerClient;
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
import com.ix.manufacturinglab.repository.UseCaseFaqRepository;
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

    private final UseCaseFaqRepository useCaseFaqRepository;
    private final SubIndustryRepository subIndustryRepository;
    private final IndustryRepository industryRepository;
    private final CloudStorageService cloudStorageService;
    private final BlobContainerClient blobContainerClient;
    @Value("${storage.path.artifacts.client.testimonials}")
    private String clientTestimonialsPath;

    @Value("${storage.path.artifacts.demo.videos}")
    private String demoVideosPath;

    @Value("${storage.path.artifacts.client.credentials}")
    private String clientCredentialsPath;
    @Value("${storage.path.banner_Url}")
    private String bannerurlPath;

    @Value("${storage.path.thumbnail_Url}")
    private String thumbnailurlPath;


    public UseCaseServiceImpl(UseCaseRepository useCaseRepository,
                              UseCaseContentRepository useCaseContentRepository,
                              UseCaseSpeakerRepository useCaseSpeakerRepository,
                              UseCaseArtifactRepository useCaseArtifactRepository,
                              UseCaseTagRepository useCaseTagRepository,
                              ValueChainRepository valueChainRepository,
                              SubIndustryRepository subIndustryRepository,
                              IndustryRepository industryRepository,
                              UseCaseFaqRepository useCaseFaqRepository,
                              CloudStorageService cloudStorageService,
                              BlobContainerClient blobContainerClient) {
        this.useCaseRepository = useCaseRepository;
        this.useCaseContentRepository = useCaseContentRepository;
        this.useCaseSpeakerRepository = useCaseSpeakerRepository;
        this.useCaseArtifactRepository = useCaseArtifactRepository;
        this.useCaseTagRepository = useCaseTagRepository;
        this.valueChainRepository = valueChainRepository;
        this.subIndustryRepository = subIndustryRepository;
        this.industryRepository = industryRepository;
        this.useCaseFaqRepository = useCaseFaqRepository;
        this.cloudStorageService = cloudStorageService;
        this.blobContainerClient = blobContainerClient;
    }

    @Override
    @Transactional
    public UseCaseResponseDTO createUseCaseAndSaveAsDraft(UseCaseRequestDTO requestDTO) {
        logger.debug(ManufacturingLabConstants.LOG_SAVING_DRAFT, requestDTO.getTitle());

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
        logger.debug(ManufacturingLabConstants.LOG_SUBMITTING_FOR_APPROVAL, requestDTO.getTitle());

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
        logger.debug(ManufacturingLabConstants.LOG_FETCHING_USE_CASE, usecaseId);

        UseCase useCase = useCaseRepository.findById(usecaseId)
                .orElseThrow(() -> new CommonException(CommonExceptionConstants.NOT_FOUND,
                        ManufacturingLabConstants.USE_CASE_NOT_FOUND + usecaseId));

        UseCaseContent content = useCaseContentRepository.findByUsecaseId(usecaseId).orElse(null);

        return buildResponseFromEntities(useCase, content);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UseCaseResponseDTO> getAllUseCases(int page, int size) {

        logger.info("START getAllUseCases Service | page={}, size={}", page, size);

            Pageable pageable = PageRequest.of(page - 1, size);
            Page<UseCase> useCases = useCaseRepository.findAll(pageable);
            List<UseCase> useCaseList = useCases.getContent();

            if (useCaseList.isEmpty()) {
                return new PageImpl<>(Collections.emptyList(), pageable, 0);
            }

            List<Integer> ids = useCaseList.stream().map(UseCase::getUsecaseId).toList();

            List<UseCaseContent> contents = useCaseContentRepository.findAllByUsecaseIdIn(ids);
            List<UseCaseTag> tags = useCaseTagRepository.findAllByUseCase_UsecaseIdIn(ids);
            List<UseCaseSpeaker> speakers = useCaseSpeakerRepository.findAllByUseCase_UsecaseIdIn(ids);
            List<UseCaseArtifact> artifacts = useCaseArtifactRepository.findAllByUseCase_UsecaseIdIn(ids);
            List<UseCaseFaq> faqs = useCaseFaqRepository.findAllByUseCase_UsecaseIdIn(ids);

            Map<Integer, UseCaseContent> contentMap = new HashMap<>();
            for (UseCaseContent c : contents) {
                contentMap.put(c.getUsecaseId(), c);
            }

            Map<Integer, List<UseCaseTag>> tagMap = new HashMap<>();
            for (UseCaseTag t : tags) {
                Integer id = t.getUseCase().getUsecaseId();
                tagMap.computeIfAbsent(id, k -> new ArrayList<>()).add(t);
            }

            Map<Integer, List<UseCaseSpeaker>> speakerMap = new HashMap<>();
            for (UseCaseSpeaker s : speakers) {
                Integer id = s.getUseCase().getUsecaseId();
                speakerMap.computeIfAbsent(id, k -> new ArrayList<>()).add(s);
            }

            Map<Integer, List<UseCaseArtifact>> artifactMap = new HashMap<>();
            for (UseCaseArtifact a : artifacts) {
                Integer id = a.getUseCase().getUsecaseId();
                artifactMap.computeIfAbsent(id, k -> new ArrayList<>()).add(a);
            }

            Map<Integer, List<UseCaseFaq>> faqMap = new HashMap<>();
            for (UseCaseFaq f : faqs) {
                Integer id = f.getUseCase().getUsecaseId();
                faqMap.computeIfAbsent(id, k -> new ArrayList<>()).add(f);
            }

            List<UseCaseResponseDTO> responses = new ArrayList<>();

            for (UseCase useCase : useCaseList) {

                Integer id = useCase.getUsecaseId();

                useCase.setTags(tagMap.getOrDefault(id, Collections.emptyList()));
                useCase.setSpeakers(speakerMap.getOrDefault(id, Collections.emptyList()));
                useCase.setArtifacts(artifactMap.getOrDefault(id, Collections.emptyList()));
                useCase.setFaqs(faqMap.getOrDefault(id, Collections.emptyList()));

                responses.add(buildResponseFromEntities(useCase, contentMap.get(id)));
            }
            return new PageImpl<>(responses, pageable, useCases.getTotalElements());

    }

    @Override
    @Transactional
    public UseCaseResponseDTO updateUseCaseandSaveasDraft(Integer usecaseId, UseCaseRequestDTO requestDTO) {

        logger.debug(ManufacturingLabConstants.LOG_UPDATING_USE_CASE, usecaseId);

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
        logger.debug(ManufacturingLabConstants.LOG_DELETING_USE_CASE, usecaseId);

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

    private UseCaseResponseDTO buildResponseFromEntities(UseCase useCase, UseCaseContent content)  {
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
        logger.info("Processing usecaseId={}", useCase.getUsecaseId());

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

        logger.debug("Archiving use case with id {}", usecaseId);

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

        logger.debug(ManufacturingLabConstants.LOG_UPDATING_USE_CASE, usecaseId);

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

        logger.debug(ManufacturingLabConstants.LOG_UPDATING_USE_CASE, usecaseId);

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
    public UseCaseResponseDTO createUseCaseAndSaveAsDraftWithBlob(UseCaseRequestDTO requestDTO, List<MultipartFile> clientTestimonials,
                                                                  List<MultipartFile> demoVideos, List<MultipartFile> clientCredentials,
                                                                  MultipartFile thumbnailUrl,MultipartFile bannerUrl) {
        logger.debug(ManufacturingLabConstants.LOG_SAVING_DRAFT, requestDTO.getTitle());
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
        // addArtifacts(useCase, requestDTO);
// 5. Add UseCaseFaqs(cascade will persist)
        addUseCaseFaqs(useCase, requestDTO);
// 6. Save UseCase (cascade saves speakers, tags, artifacts)
        useCase = useCaseRepository.save(useCase);

        String thumbnailSasUrl = null;
        String bannerSasUrl = null;

        if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
            String blobPath = thumbnailurlPath.replace("{usecase-id}", String.valueOf(useCase.getUsecaseId()))
                    + "/" + thumbnailUrl.getOriginalFilename();
            logger.debug("Uploading thumbnailUrl to blob storage at path: {}", blobPath);
             thumbnailSasUrl = cloudStorageService.uploadFile(thumbnailUrl, blobPath);
            logger.debug("thumbnailUrl uploaded successfully. Blob URL: {}", thumbnailSasUrl);

        }
        if (bannerUrl != null && !bannerUrl.isEmpty()) {
            String blobPath = bannerurlPath.replace("{usecase-id}", String.valueOf(useCase.getUsecaseId()))
                    + "/" + bannerUrl.getOriginalFilename();
            logger.debug("Uploading bannerUrl to blob storage at path: {}", blobPath);
             bannerSasUrl = cloudStorageService.uploadFile(bannerUrl, blobPath);
            logger.debug("bannerUrl uploaded successfully. Blob URL: {}", bannerSasUrl);
        }

        if (clientTestimonials != null && !clientTestimonials.isEmpty()) {

            for (MultipartFile file : clientTestimonials) {

                if (file.isEmpty()) continue;

                String blobPath = clientTestimonialsPath
                        .replace("{usecase-id}", String.valueOf(useCase.getUsecaseId()))
                        + "/" + file.getOriginalFilename();

                String sasUrl = cloudStorageService.uploadFile(file, blobPath);


                UseCaseArtifact artifact = UseCaseArtifact.builder()
                        .useCase(useCase)
                        .artifactType("CLIENT_TESTIMONIAL")
                        .url(sasUrl)
                        .artifactName(file.getOriginalFilename())
                        .build();

                useCase.getArtifacts().add(artifact);
            }
        }

        if (demoVideos != null && !demoVideos.isEmpty()) {

            for (MultipartFile file : demoVideos) {

                if (file.isEmpty()) continue;

                String blobPath = demoVideosPath
                        .replace("{usecase-id}", String.valueOf(useCase.getUsecaseId()))
                        + "/" + file.getOriginalFilename();

                String sasUrl = cloudStorageService.uploadFile(file, blobPath);

                UseCaseArtifact artifact = UseCaseArtifact.builder()
                        .useCase(useCase)
                        .artifactType("DEMO_VIDEO")
                        .url(sasUrl)
                        .artifactName(file.getOriginalFilename())
                        .build();

                useCase.getArtifacts().add(artifact);
            }
        }

        if (clientCredentials != null && !clientCredentials.isEmpty()) {

            for (MultipartFile file : clientCredentials) {

                if (file.isEmpty()) continue;

                String blobPath = clientCredentialsPath
                        .replace("{usecase-id}", String.valueOf(useCase.getUsecaseId()))
                        + "/" + file.getOriginalFilename();

                String sasUrl = cloudStorageService.uploadFile(file, blobPath);

                UseCaseArtifact artifact = UseCaseArtifact.builder()
                        .useCase(useCase)
                        .artifactType("CLIENT_CREDENTIAL")
                        .url(sasUrl)
                        .artifactName(file.getOriginalFilename())
                        .build();

                useCase.getArtifacts().add(artifact);
            }
        }
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
                .thumbnailUrl(thumbnailSasUrl)
                .narrationGuide(requestDTO.getNarrationGuide())
                .bannerUrl(bannerSasUrl)
                .build();
        useCaseContentRepository.save(content);
        return buildResponseDTO(useCase, content, requestDTO);
    }

    @Override
    @Transactional
    public UseCaseResponseDTO createUseCaseAndSubmitForApprovalwithBlob(UseCaseRequestDTO requestDTO, List<MultipartFile> clientTestimonials,
                                                                        List<MultipartFile> demoVideos, List<MultipartFile> clientCredentials,
                                                                        MultipartFile thumbnailUrl,MultipartFile bannerUrl) {
        logger.debug(ManufacturingLabConstants.LOG_SUBMITTING_FOR_APPROVAL, requestDTO.getTitle());

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
        //addArtifacts(useCase, requestDTO);

        // 5. Add UseCaseFaqs(cascade will persist)
        addUseCaseFaqs(useCase, requestDTO);

        // 6. Save UseCase (cascade saves speakers, tags, artifacts)
        useCase = useCaseRepository.save(useCase);

        String thumbnailSasUrl = null;
        String bannerSasUrl = null;

        if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
            String blobPath = thumbnailurlPath.replace("{usecase-id}", String.valueOf(useCase.getUsecaseId()))
                    + "/" + thumbnailUrl.getOriginalFilename();
            logger.debug("Uploading thumbnailUrl to blob storage at path: {}", blobPath);
            thumbnailSasUrl = cloudStorageService.uploadFile(thumbnailUrl, blobPath);
            logger.debug("thumbnailUrl uploaded successfully. Blob URL: {}", thumbnailSasUrl);

        }
        if (bannerUrl != null && !bannerUrl.isEmpty()) {
            String blobPath = bannerurlPath.replace("{usecase-id}", String.valueOf(useCase.getUsecaseId()))
                    + "/" + bannerUrl.getOriginalFilename();
            logger.debug("Uploading bannerUrl to blob storage at path: {}", blobPath);
            bannerSasUrl = cloudStorageService.uploadFile(bannerUrl, blobPath);
            logger.debug("bannerUrl uploaded successfully. Blob URL: {}", bannerSasUrl);
        }

        if (clientTestimonials != null && !clientTestimonials.isEmpty()) {

            for (MultipartFile file : clientTestimonials) {

                if (file.isEmpty()) continue;

                String blobPath = clientTestimonialsPath
                        .replace("{usecase-id}", String.valueOf(useCase.getUsecaseId()))
                        + "/" + file.getOriginalFilename();

                String sasUrl = cloudStorageService.uploadFile(file, blobPath);

                UseCaseArtifact artifact = UseCaseArtifact.builder()
                        .useCase(useCase)
                        .artifactType("CLIENT_TESTIMONIAL")
                        .url(sasUrl)
                        .artifactName(file.getOriginalFilename())
                        .build();

                useCase.getArtifacts().add(artifact);
            }
        }

        if (demoVideos != null && !demoVideos.isEmpty()) {

            for (MultipartFile file : demoVideos) {

                if (file.isEmpty()) continue;

                String blobPath = demoVideosPath
                        .replace("{usecase-id}", String.valueOf(useCase.getUsecaseId()))
                        + "/" + file.getOriginalFilename();

                String sasUrl = cloudStorageService.uploadFile(file, blobPath);

                UseCaseArtifact artifact = UseCaseArtifact.builder()
                        .useCase(useCase)
                        .artifactType("DEMO_VIDEO")
                        .url(sasUrl)
                        .artifactName(file.getOriginalFilename())
                        .build();

                useCase.getArtifacts().add(artifact);
            }
        }

        if (clientCredentials != null && !clientCredentials.isEmpty()) {

            for (MultipartFile file : clientCredentials) {

                if (file.isEmpty()) continue;

                String blobPath = clientCredentialsPath
                        .replace("{usecase-id}", String.valueOf(useCase.getUsecaseId()))
                        + "/" + file.getOriginalFilename();

                String sasUrl = cloudStorageService.uploadFile(file, blobPath);

                UseCaseArtifact artifact = UseCaseArtifact.builder()
                        .useCase(useCase)
                        .artifactType("CLIENT_CREDENTIAL")
                        .url(sasUrl)
                        .artifactName(file.getOriginalFilename())
                        .build();

                useCase.getArtifacts().add(artifact);
            }
        }
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
                .thumbnailUrl(thumbnailSasUrl)
                .narrationGuide(requestDTO.getNarrationGuide())
                .bannerUrl(bannerSasUrl)
                .build();
        useCaseContentRepository.save(content);

        return buildResponseDTO(useCase, content, requestDTO);
    }

    @Override
    @Transactional
    public UseCaseResponseDTO updateUseCaseandSaveasDraftWithBlob(Integer usecaseId, UseCaseRequestDTO requestDTO,
                                                                  List<MultipartFile> clientTestimonials, List<MultipartFile> demoVideos,
                                                                  List<MultipartFile> clientCredentials, MultipartFile thumbnailUrl, MultipartFile bannerUrl) {

        // Fetch existing use case
        UseCase existingUseCase = useCaseRepository.findById(usecaseId)
                .orElseThrow(() -> new CommonException(
                        CommonExceptionConstants.NOT_FOUND,
                        "Use case not found with id: " + usecaseId));

        String status = existingUseCase.getStatus();
        Integer parentId = existingUseCase.getParentUsecaseId();
        UseCase useCase;

        if ("DRAFT".equalsIgnoreCase(status) || ("IN_REVIEW".equalsIgnoreCase(status) && parentId == null)) {
            useCase = existingUseCase;

        } else if ("APPROVED".equalsIgnoreCase(status) && parentId == null) {
            useCase = new UseCase();
            useCase.setValueChainId(existingUseCase.getValueChainId());
            useCase.setCreatorId(existingUseCase.getCreatorId());
            useCase.setOwnerEid(existingUseCase.getOwnerEid());
            useCase.setCreatedDate(LocalDateTime.now());
            useCase.setParentUsecaseId(existingUseCase.getUsecaseId());
            useCase.setStatus("DRAFT");
            useCase.setIsUpdatedUsecase(true);
            useCase.setIsActive(false);

        } else {
            throw new CommonException(CommonExceptionConstants.CONFLICT,
                    "Use case can't be modified in current state");
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

        String thumbnailFolder = thumbnailurlPath.replace("{usecase-id}", String.valueOf(useCase.getUsecaseId()));
        String bannerFolder = bannerurlPath.replace("{usecase-id}", String.valueOf(useCase.getUsecaseId()));
        String testimonialsFolder = clientTestimonialsPath.replace("{usecase-id}", String.valueOf(useCase.getUsecaseId()));
        String demoVideosFolder = demoVideosPath.replace("{usecase-id}", String.valueOf(useCase.getUsecaseId()));
        String credentialsFolder = clientCredentialsPath.replace("{usecase-id}", String.valueOf(useCase.getUsecaseId()));


        if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {


            String existingThumbnail = content.getThumbnailUrl();

            if (existingThumbnail != null && !existingThumbnail.isEmpty()) {
                cloudStorageService.deleteAllFilesInFolder(thumbnailFolder);
            }
            String blobPath = thumbnailFolder + "/" + thumbnailUrl.getOriginalFilename();
            String thumbnaisasUrl = cloudStorageService.updateFile(thumbnailUrl, blobPath);
            content.setThumbnailUrl(thumbnaisasUrl);
        }

        if (bannerUrl != null && !bannerUrl.isEmpty()) {
            String existingBanner = content.getBannerUrl();

            if (existingBanner != null && !existingBanner.isEmpty()) {
                cloudStorageService.deleteAllFilesInFolder(bannerFolder);
            }
            String blobPath = bannerFolder + "/" + bannerUrl.getOriginalFilename();
            String bannersasUrl = cloudStorageService.updateFile(bannerUrl, blobPath);
            content.setBannerUrl(bannersasUrl);
        }
        if (existingUseCase.getUsecaseId().equals(useCase.getUsecaseId())) {
            useCase.getTags().clear();
            useCase.getSpeakers().clear();
            useCase.getArtifacts().clear();
            useCase.getFaqs().clear();
        }

        addArtifactsFromFiles(useCase, testimonialsFolder, clientTestimonials, "CLIENT_TESTIMONIAL");
        addArtifactsFromFiles(useCase, demoVideosFolder, demoVideos, "DEMO_VIDEO");
        addArtifactsFromFiles(useCase, credentialsFolder, clientCredentials, "CLIENT_CREDENTIAL");

        content.setDescription(requestDTO.getDescription());
        content.setBusinessProblem(requestDTO.getBusinessProblem());
        content.setSolution(requestDTO.getSolutions());
        content.setToolsAndTechnologies(requestDTO.getToolsAndTechnologies());
        content.setKeyResults(requestDTO.getKeyResults());
        content.setValueDelivered(requestDTO.getValueDelivered());
        content.setDuration(requestDTO.getDuration());
        content.setNarrationGuide(requestDTO.getNarrationGuide());

        useCaseContentRepository.save(content);

        addTags(useCase, requestDTO);
        addSpeakers(useCase, requestDTO);
        addUseCaseFaqs(useCase, requestDTO);

        useCaseRepository.save(useCase);

        return buildResponseFromEntities(useCase, content);
    }

    private void addArtifactsFromFiles(UseCase useCase, String folderPath, List<MultipartFile> files, String artifactType) {
        if (files == null || files.isEmpty()) return;

        cloudStorageService.deleteAllFilesInFolder(folderPath);

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            String sasUrl = cloudStorageService.updateFile(file, folderPath + "/" + file.getOriginalFilename());

            UseCaseArtifact artifact = UseCaseArtifact.builder()
                    .useCase(useCase)
                    .artifactType(artifactType)
                    .url(sasUrl)
                    .artifactName(file.getOriginalFilename())
                    .build();

            useCase.getArtifacts().add(artifact);

            logger.debug("File {} uploaded for usecase {} -> {}", file.getOriginalFilename(), useCase.getUsecaseId(), sasUrl);
        }
    }

    @Override
    @Transactional
    public UseCaseResponseDTO updateUseCaseAndSubmitForApprovalwithBlob(Integer usecaseId, UseCaseRequestDTO requestDTO, List<MultipartFile> clientTestimonials,
                                                                        List<MultipartFile> demoVideos, List<MultipartFile> clientCredentials,
                                                                        MultipartFile thumbnailUrl,MultipartFile bannerUrl) {

        logger.debug(ManufacturingLabConstants.LOG_UPDATING_USE_CASE, usecaseId);

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

        // Resolve paths from @Value properties
        String thumbnailFolder = thumbnailurlPath.replace("{usecase-id}", String.valueOf(useCase.getUsecaseId()));
        String bannerFolder = bannerurlPath.replace("{usecase-id}", String.valueOf(useCase.getUsecaseId()));
        String testimonialsFolder = clientTestimonialsPath.replace("{usecase-id}", String.valueOf(useCase.getUsecaseId()));
        String demoVideosFolder = demoVideosPath.replace("{usecase-id}", String.valueOf(useCase.getUsecaseId()));
        String credentialsFolder = clientCredentialsPath.replace("{usecase-id}", String.valueOf(useCase.getUsecaseId()));


        if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {


            String existingThumbnail = content.getThumbnailUrl();

            if (existingThumbnail != null && !existingThumbnail.isEmpty()) {
                cloudStorageService.deleteAllFilesInFolder(thumbnailFolder);
            }
            String blobPath = thumbnailFolder + "/" + thumbnailUrl.getOriginalFilename();
            String sasUrl = cloudStorageService.updateFile(thumbnailUrl, blobPath);
            content.setThumbnailUrl(sasUrl);
        }

        if (bannerUrl != null && !bannerUrl.isEmpty()) {
            String existingBanner = content.getBannerUrl();

            if (existingBanner != null && !existingBanner.isEmpty()) {
                cloudStorageService.deleteAllFilesInFolder(bannerFolder);
            }
            String blobPath = bannerFolder + "/" + bannerUrl.getOriginalFilename();
            String sasUrl = cloudStorageService.updateFile(bannerUrl, blobPath);
            content.setBannerUrl(sasUrl);
        }

        if (existingUseCase.getUsecaseId().equals(useCase.getUsecaseId())) {
            useCase.getTags().clear();
            useCase.getSpeakers().clear();
            useCase.getArtifacts().clear();
            useCase.getFaqs().clear();
        }

        // Upload artifacts
        addArtifactsFromFiles(useCase, testimonialsFolder, clientTestimonials, "CLIENT_TESTIMONIAL");
        addArtifactsFromFiles(useCase, demoVideosFolder, demoVideos, "DEMO_VIDEO");
        addArtifactsFromFiles(useCase, credentialsFolder, clientCredentials, "CLIENT_CREDENTIAL");

        content.setDescription(requestDTO.getDescription());
        content.setBusinessProblem(requestDTO.getBusinessProblem());
        content.setSolution(requestDTO.getSolutions());
        content.setToolsAndTechnologies(requestDTO.getToolsAndTechnologies());
        content.setKeyResults(requestDTO.getKeyResults());
        content.setValueDelivered(requestDTO.getValueDelivered());
        content.setDuration(requestDTO.getDuration());
        content.setNarrationGuide(requestDTO.getNarrationGuide());

        useCaseContentRepository.save(content);

        addTags(useCase, requestDTO);
        addSpeakers(useCase, requestDTO);
        addUseCaseFaqs(useCase, requestDTO);

        useCaseRepository.save(useCase);

        return buildResponseFromEntities(useCase, content);
    }
}
