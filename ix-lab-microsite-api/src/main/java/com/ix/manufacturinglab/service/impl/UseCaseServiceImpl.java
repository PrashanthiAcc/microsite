package com.ix.manufacturinglab.service.impl;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import com.ix.manufacturinglab.entity.Favourite;
import com.ix.manufacturinglab.repository.UseCaseContentRepository;
import com.ix.manufacturinglab.repository.UseCaseRepository;
import com.ix.manufacturinglab.repository.UseCaseSpeakerRepository;
import com.ix.manufacturinglab.repository.UseCaseArtifactRepository;
import com.ix.manufacturinglab.repository.UseCaseTagRepository;
import com.ix.manufacturinglab.repository.ValueChainRepository;
import com.ix.manufacturinglab.repository.SubIndustryRepository;
import com.ix.manufacturinglab.repository.IndustryRepository;
import com.ix.manufacturinglab.repository.UseCaseFaqRepository;
import com.ix.manufacturinglab.repository.FavouriteUseCaseRepository;
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

import static com.ix.manufacturinglab.enums.ArtifactType.DEMO_VIDEO;

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
    private final FavouriteUseCaseRepository favouriteUseCaseRepository;
    private final CloudStorageService cloudStorageService;
    private final BlobContainerClient blobContainerClient;
    @Value("${storage.path.artifacts.client.testimonials}")
    private String clientTestimonialsPath;
    @Value("${storage.path.artifacts.demo.videos}")
    private String demoVideosPath;
    @Value("${storage.path.artifacts.elevator_pitch}")
    private String elevatorPitchPath;
    @Value("${storage.path.artifacts.user_story}")
    private String userStoryPath;
    @Value("${storage.path.banner_image}")
    private String bannerurlPath;
    @Value("${storage.path.thumbnail_image}")
    private String thumbnailurlPath;

    @Value("${azure.blob.max-file-size-mb:100}")
    private int maxFileSizeMb;

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
                              BlobContainerClient blobContainerClient,
                              FavouriteUseCaseRepository favouriteUseCaseRepository) {
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
        this.favouriteUseCaseRepository = favouriteUseCaseRepository;
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

        // Batch fetch related data
        Map<Integer, UseCaseContent> contentMap = useCaseContentRepository.findAllByUsecaseIdIn(ids)
                .stream().collect(Collectors.toMap(UseCaseContent::getUsecaseId, Function.identity()));

        Map<Integer, List<UseCaseTag>> tagMap = useCaseTagRepository.findAllByUseCase_UsecaseIdIn(ids)
                .stream().collect(Collectors.groupingBy(tag -> tag.getUseCase().getUsecaseId()));

        Map<Integer, List<UseCaseSpeaker>> speakerMap = useCaseSpeakerRepository.findAllByUseCase_UsecaseIdIn(ids)
                .stream().collect(Collectors.groupingBy(speaker -> speaker.getUseCase().getUsecaseId()));

        Map<Integer, List<UseCaseArtifact>> artifactMap = useCaseArtifactRepository.findAllByUseCase_UsecaseIdIn(ids)
                .stream().collect(Collectors.groupingBy(artifact -> artifact.getUseCase().getUsecaseId()));

        Map<Integer, List<UseCaseFaq>> faqMap = useCaseFaqRepository.findAllByUseCase_UsecaseIdIn(ids)
                .stream().collect(Collectors.groupingBy(faq -> faq.getUseCase().getUsecaseId()));

        // Resolve industry hierarchy in fewer queries
        Set<Long> vcIds = useCaseList.stream().map(uc -> uc.getValueChainId().longValue()).collect(Collectors.toSet());
        Map<Long, ValueChain> vcMap = valueChainRepository.findAllById(vcIds).stream()
                .collect(Collectors.toMap(ValueChain::getValueChainId, Function.identity()));

        Set<Long> siIds = vcMap.values().stream().map(ValueChain::getSubIndustryId).collect(Collectors.toSet());
        Map<Long, SubIndustry> siMap = subIndustryRepository.findAllById(siIds).stream()
                .collect(Collectors.toMap(SubIndustry::getSubIndustryId, Function.identity()));

        Set<Long> indIds = siMap.values().stream().map(SubIndustry::getIndustryId).collect(Collectors.toSet());
        Map<Long, Industry> indMap = industryRepository.findAllById(indIds).stream()
                .collect(Collectors.toMap(Industry::getIndustryId, Function.identity()));

        // Build response
        List<UseCaseResponseDTO> responses = useCaseList.stream().map(useCase -> {
            Integer id = useCase.getUsecaseId();

            useCase.setTags(tagMap.getOrDefault(id, Collections.emptyList()));
            useCase.setSpeakers(speakerMap.getOrDefault(id, Collections.emptyList()));
            useCase.setArtifacts(artifactMap.getOrDefault(id, Collections.emptyList()));
            useCase.setFaqs(faqMap.getOrDefault(id, Collections.emptyList()));

            Long resolvedIndustryId = null, resolvedSubIndustryId = null;
            ValueChain vc = vcMap.get(useCase.getValueChainId().longValue());
            if (vc != null) {
                SubIndustry si = siMap.get(vc.getSubIndustryId());
                if (si != null) {
                    resolvedSubIndustryId = si.getSubIndustryId();
                    Industry ind = indMap.get(si.getIndustryId());
                    if (ind != null) resolvedIndustryId = ind.getIndustryId();
                }
            }

            return buildResponseFromEntities(useCase, contentMap.get(id), resolvedIndustryId, resolvedSubIndustryId);
        }).toList();

        return new PageImpl<>(responses, pageable, useCases.getTotalElements());
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
                                        .usecaseFaqId(f.getUsecaseFaqId())
                                        .usecaseId(f.getUseCase().getUsecaseId())
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
        return buildResponseFromEntities(useCase, content, industryId, subIndustryId);
    }

    private UseCaseResponseDTO buildResponseFromEntities(UseCase useCase, UseCaseContent content, Long industryId, Long subIndustryId) {
        logger.debug("Processing usecaseId={}", useCase.getUsecaseId());

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
                                    .usecaseFaqId(f.getUsecaseFaqId())
                                    .usecaseId(f.getUseCase().getUsecaseId())
                                    .question(f.getQuestion())
                                    .answer(f.getAnswer())
                                    .updatedBy(f.getUpdatedBy())
                                    .lastUpdated(f.getLastUpdated())
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
                        new CommonException("400", "Only draft use cases can be discarded or use case not found"));

        UseCaseContent useCaseContent = useCaseContentRepository.findByUsecaseId(usecaseId).orElse(null);

        if (useCaseContent.getThumbnailUrl() != null) {
            cloudStorageService.deleteFile(extractBlobPath(useCaseContent.getThumbnailUrl()));
        }

        if (useCaseContent.getBannerUrl() != null) {
            cloudStorageService.deleteFile(extractBlobPath(useCaseContent.getBannerUrl()));
        }

        cloudStorageService.deleteAllFilesInFolder("usecase/artifacts/Client Testimonials/" + usecaseId);

        cloudStorageService.deleteAllFilesInFolder("usecase/artifacts/Demo Videos/" + usecaseId);

        cloudStorageService.deleteAllFilesInFolder("usecase/artifacts/Client Credentials/user_story/" + usecaseId);

        cloudStorageService.deleteAllFilesInFolder("usecase/artifacts/Client Credentials/elevator_pitch/" + usecaseId);

        useCaseContentRepository.deleteByUsecaseId(usecaseId);
        useCaseTagRepository.deleteByUseCase_UsecaseId(usecaseId);
        useCaseSpeakerRepository.deleteByUseCase_UsecaseId(usecaseId);
        useCaseFaqRepository.deleteByUseCase_UsecaseId(usecaseId);
        useCaseRepository.delete(useCase);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<UseCaseResponseDTO> getAllArchiveUseCases(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<UseCase> useCases = useCaseRepository.findByStatus("ARCHIVED", pageable);
        List<UseCase> useCaseList = useCases.getContent();

        if (useCaseList.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        List<Integer> ids = useCaseList.stream().map(UseCase::getUsecaseId).toList();

        // Batch fetch related data
        Map<Integer, UseCaseContent> contentMap = useCaseContentRepository.findAllByUsecaseIdIn(ids)
                .stream().collect(Collectors.toMap(UseCaseContent::getUsecaseId, Function.identity()));

        Map<Integer, List<UseCaseTag>> tagMap = useCaseTagRepository.findAllByUseCase_UsecaseIdIn(ids)
                .stream().collect(Collectors.groupingBy(tag -> tag.getUseCase().getUsecaseId()));

        Map<Integer, List<UseCaseSpeaker>> speakerMap = useCaseSpeakerRepository.findAllByUseCase_UsecaseIdIn(ids)
                .stream().collect(Collectors.groupingBy(speaker -> speaker.getUseCase().getUsecaseId()));

        Map<Integer, List<UseCaseArtifact>> artifactMap = useCaseArtifactRepository.findAllByUseCase_UsecaseIdIn(ids)
                .stream().collect(Collectors.groupingBy(artifact -> artifact.getUseCase().getUsecaseId()));

        Map<Integer, List<UseCaseFaq>> faqMap = useCaseFaqRepository.findAllByUseCase_UsecaseIdIn(ids)
                .stream().collect(Collectors.groupingBy(faq -> faq.getUseCase().getUsecaseId()));

        // Resolve industry hierarchy in fewer queries
        Set<Long> vcIds = useCaseList.stream().map(uc -> uc.getValueChainId().longValue()).collect(Collectors.toSet());
        Map<Long, ValueChain> vcMap = valueChainRepository.findAllById(vcIds).stream()
                .collect(Collectors.toMap(ValueChain::getValueChainId, Function.identity()));

        Set<Long> siIds = vcMap.values().stream().map(ValueChain::getSubIndustryId).collect(Collectors.toSet());
        Map<Long, SubIndustry> siMap = subIndustryRepository.findAllById(siIds).stream()
                .collect(Collectors.toMap(SubIndustry::getSubIndustryId, Function.identity()));

        Set<Long> indIds = siMap.values().stream().map(SubIndustry::getIndustryId).collect(Collectors.toSet());
        Map<Long, Industry> indMap = industryRepository.findAllById(indIds).stream()
                .collect(Collectors.toMap(Industry::getIndustryId, Function.identity()));

        // Build response
        List<UseCaseResponseDTO> responses = useCaseList.stream().map(useCase -> {
            Integer id = useCase.getUsecaseId();

            useCase.setTags(tagMap.getOrDefault(id, Collections.emptyList()));
            useCase.setSpeakers(speakerMap.getOrDefault(id, Collections.emptyList()));
            useCase.setArtifacts(artifactMap.getOrDefault(id, Collections.emptyList()));
            useCase.setFaqs(faqMap.getOrDefault(id, Collections.emptyList()));

            Long resolvedIndustryId = null, resolvedSubIndustryId = null;
            ValueChain vc = vcMap.get(useCase.getValueChainId().longValue());
            if (vc != null) {
                SubIndustry si = siMap.get(vc.getSubIndustryId());
                if (si != null) {
                    resolvedSubIndustryId = si.getSubIndustryId();
                    Industry ind = indMap.get(si.getIndustryId());
                    if (ind != null) resolvedIndustryId = ind.getIndustryId();
                }
            }

            return buildResponseFromEntities(useCase, contentMap.get(id), resolvedIndustryId, resolvedSubIndustryId);
        }).toList();

        return new PageImpl<>(responses, pageable, useCases.getTotalElements());
    }

    private void addArtifactsFromFiles(UseCase useCase, String folderPath, List<MultipartFile> files, String artifactType) {
        if (files == null || files.isEmpty()) return;

        cloudStorageService.deleteAllFilesInFolder(folderPath);


        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
                validateFileSize(file);
                //String folderPath = "usecase/artifacts/Demo Videos/{usecase-id}";
                String sasUrl = cloudStorageService.uploadFileChunked(file, folderPath);

            //String sasUrl = cloudStorageService.updateFile(file, folderPath + "/" + file.getOriginalFilename());

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
    @Transactional
    private void handleArtifacts(UseCase useCase, String folderPath, String artifactType, List<String> incomingUrls, List<MultipartFile> newFiles) {

        if (incomingUrls == null) {
            //logger.info("Incoming URLs is null → skipping deletion for type: {}", artifactType);
        } else {

            if (incomingUrls.size() == 1 && incomingUrls.get(0).isBlank()) {
                incomingUrls = Collections.emptyList();
            }

            Set<String> incomingPaths = incomingUrls.stream()
                    .map(this::extractBlobPath)
                    .map(String::trim)
                    .collect(Collectors.toSet());

            Iterator<UseCaseArtifact> iterator = useCase.getArtifacts().iterator();

            while (iterator.hasNext()) {

                UseCaseArtifact artifact = iterator.next();

                if (!artifactType.equalsIgnoreCase(artifact.getArtifactType())) {
                    continue;
                }

                String dbPath = extractBlobPath(artifact.getUrl()).trim();

                if (incomingPaths.isEmpty() || !incomingPaths.contains(dbPath)) {

                    cloudStorageService.deleteFile(dbPath);
                    iterator.remove();
                    useCaseArtifactRepository.deleteById(artifact.getArtifactId());
                }
            }
        }

        if (newFiles != null) {
            for (MultipartFile file : newFiles) {

                if (file.isEmpty()) continue;

                validateFileSize(file);

                String sasUrl = cloudStorageService.uploadFileChunked(file, folderPath);

                UseCaseArtifact newArtifact = UseCaseArtifact.builder()
                        .useCase(useCase)
                        .artifactType(artifactType)
                        .url(sasUrl)
                        .artifactName(file.getOriginalFilename())
                        .build();

                useCase.getArtifacts().add(newArtifact);
            }
        }
    }

    private String extractBlobPath(String url) {
        if (url == null) return null;

        String cleanUrl = url.split("\\?")[0];

        String path = cleanUrl.substring(cleanUrl.indexOf(".net/") + 5);

        path = URLDecoder.decode(path, StandardCharsets.UTF_8).trim();

        if (path.startsWith("ixmicrositelab/")) {
            path = path.substring("ixmicrositelab/".length());
        }

        return path;
    }
    @Override
    @Transactional
    public UseCaseResponseDTO createUseCaseAndSubmitForApprovalwithBlob(UseCaseRequestDTO requestDTO, List<MultipartFile> clientTestimonials, List<MultipartFile> demoVideos, MultipartFile thumbnailUrl, MultipartFile bannerUrl, List<MultipartFile> elevatorPitch, List<MultipartFile> userStory, List<String> demoVideoLinks) {
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

        uploadArtifacts(useCase, clientTestimonials, "CLIENT_TESTIMONIAL", clientTestimonialsPath);
        uploadArtifacts(useCase, demoVideos, "DEMO_VIDEO", demoVideosPath);
        uploadArtifacts(useCase, elevatorPitch, "ELEVATOR_PITCH", elevatorPitchPath);
        uploadArtifacts(useCase, userStory, "USER_STORY", userStoryPath);

        saveDemoVideoLinks(useCase, demoVideoLinks);

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

    private String uploadFileToBlob(MultipartFile file, String folderPath, String existingFilePath) {
        if (file != null && !file.isEmpty()) {
            if (existingFilePath != null && !existingFilePath.isEmpty()) {
                cloudStorageService.deleteAllFilesInFolder(folderPath);
            }
            String blobPath = folderPath + "/" + file.getOriginalFilename();
            return cloudStorageService.updateFile(file, blobPath);
        }
        return null;
    }

    @Override
    @Transactional
    public UseCaseResponseDTO updateUseCaseandSaveasDraftWithBlob(Integer usecaseId, UseCaseRequestDTO requestDTO, List<MultipartFile> clientTestimonials,
                                                                  List<MultipartFile> demoVideos, MultipartFile thumbnailUrl, MultipartFile bannerUrl,
                                                                  List<MultipartFile> elevatorPitch, List<MultipartFile> userStory, List<String> clientTestimonialsUrls,
                                                                  List<String> demoVideosUrls,List<String> elevatorPitchUrls,List<String> userStoryUrls,
                                                                  String thumbnailUrls, String bannerUrls, List<String> demoVideoExistingLinks, List<String> demoVideoLinks){


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

        if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {

            if (content.getThumbnailUrl() != null && !content.getThumbnailUrl().isEmpty()) {
                cloudStorageService.deleteFileFromBlobPath(content.getThumbnailUrl());
            }
            String blobPath = thumbnailFolder + "/" + thumbnailUrl.getOriginalFilename();
            String thumbnailAsUrl = cloudStorageService.uploadFile(thumbnailUrl, blobPath);
            content.setThumbnailUrl(thumbnailAsUrl);
        } else {
            content.setThumbnailUrl(thumbnailUrls);
        }

        if (bannerUrl != null && !bannerUrl.isEmpty()) {

            if (content.getBannerUrl() != null && !content.getBannerUrl().isEmpty()) {
                cloudStorageService.deleteFileFromBlobPath(content.getBannerUrl());
            }
            String blobPath = bannerFolder + "/" + bannerUrl.getOriginalFilename();
            String bannersasUrl = cloudStorageService.uploadFile(bannerUrl, blobPath);
            content.setBannerUrl(bannersasUrl);
        } else {
            content.setBannerUrl(bannerUrls);
        }

        if (existingUseCase.getUsecaseId().equals(useCase.getUsecaseId())) {
            useCase.getTags().clear();
            useCase.getSpeakers().clear();
            //useCase.getArtifacts().clear();
            useCase.getFaqs().clear();
        }

        handleArtifacts(useCase, "usecase/artifacts/Client Testimonials/" + usecaseId, "CLIENT_TESTIMONIAL", clientTestimonialsUrls, clientTestimonials);
        handleArtifacts(useCase, "usecase/artifacts/Demo Videos/" + usecaseId, "DEMO_VIDEO", demoVideosUrls, demoVideos);
        handleArtifacts(useCase, "usecase/artifacts/Client Credentials/user_story/" + usecaseId, "USER_STORY", userStoryUrls, userStory);
        handleArtifacts(useCase, "usecase/artifacts/Client Credentials/elevator_pitch/" + usecaseId, "ELEVATOR_PITCH", elevatorPitchUrls, elevatorPitch);

        handleDemoVideoLinks(useCase, demoVideoExistingLinks, demoVideoLinks);


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

    @Override
    @Transactional
    public UseCaseResponseDTO createUseCaseAndSaveAsDraftWithBlob(UseCaseRequestDTO requestDTO, List<MultipartFile> clientTestimonials, List<MultipartFile> demoVideos, MultipartFile thumbnailUrl, MultipartFile bannerUrl, List<MultipartFile> elevatorPitch, List<MultipartFile> userStory, List<String> demoVideoLinks) {
        logger.debug(ManufacturingLabConstants.LOG_SAVING_DRAFT, requestDTO.getTitle());
// 1. Build UseCase entity
        UseCase useCase = UseCase.builder()
                .valueChainId(requestDTO.getValueChainId())
                .title(requestDTO.getTitle())
                .ownerEid(String.valueOf(requestDTO.getOwnerEId()))
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

        uploadArtifacts(useCase, clientTestimonials, "CLIENT_TESTIMONIAL", clientTestimonialsPath);
        uploadArtifacts(useCase, demoVideos, "DEMO_VIDEO", demoVideosPath);
        uploadArtifacts(useCase, elevatorPitch, "ELEVATOR_PITCH", elevatorPitchPath);
        uploadArtifacts(useCase, userStory, "USER_STORY", userStoryPath);
        saveDemoVideoLinks(useCase, demoVideoLinks);
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
    public String uploadDemoVideoInCheckMode(MultipartFile file) {
         if (file == null || file.isEmpty()) {
             throw new CommonException(CommonExceptionConstants.BAD_REQUEST, "No file provided for upload");
         }
        validateFileSize(file);
        String blobPath = "usecase/artifacts/Demo Videos/";
        String sasUrl = cloudStorageService.uploadFileChunked(file, blobPath);
        return sasUrl;
    }

    private void uploadArtifacts(UseCase useCase, List<MultipartFile> files, String artifactType, String folderPath) {
        if (files == null || files.isEmpty()) return;

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            validateFileSize(file);
            String blobPath = folderPath.replace("{usecase-id}", String.valueOf(useCase.getUsecaseId()))
                    + "/" + file.getOriginalFilename();

            //String sasUrl = cloudStorageService.uploadFile(file, blobPath);
            String sasUrl = cloudStorageService.uploadFileChunked(file, blobPath);

            UseCaseArtifact artifact = UseCaseArtifact.builder()
                    .useCase(useCase)
                    .artifactType(artifactType)
                    .url(sasUrl)
                    .artifactName(file.getOriginalFilename())
                    .build();
            useCase.getArtifacts().add(artifact);
        }
    }
    private void validateFileSize(MultipartFile file) {
        long maxBytes = (long) maxFileSizeMb * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new CommonException(CommonExceptionConstants.BAD_REQUEST,
                    "File size exceeds maximum allowed " + maxFileSizeMb + "MB: " + file.getOriginalFilename());
        }
    }
    @Override
    public UseCaseResponseDTO updateUseCaseAndSubmitForApprovalwithBlob(Integer usecaseId, UseCaseRequestDTO requestDTO, List<MultipartFile> clientTestimonials,
                                                                        List<MultipartFile> demoVideos, MultipartFile thumbnailUrl, MultipartFile bannerUrl,
                                                                        List<MultipartFile> elevatorPitch, List<MultipartFile> userStory, List<String> clientTestimonialsUrls,
                                                                        List<String> demoVideosUrls,List<String> elevatorPitchUrls,List<String> userStoryUrls, String thumbnailUrls,
                                                                        String bannerUrls, List<String> demoVideoExistingLinks, List<String> demoVideoLinks) {
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
            useCase.setParentUsecaseId(existingUseCase.getUsecaseId());
            useCase.setStatus("IN_REVIEW");
            useCase.setIsUpdatedUsecase(true);
            useCase.setIsActive(false);
        } else {
            throw new CommonException(CommonExceptionConstants.CONFLICT, "Use case cannot be modified in current state");
        }

        useCase.setTitle(requestDTO.getTitle());
        useCase.setValueChainId(requestDTO.getValueChainId());
        useCase.setOwnerEid(String.valueOf(requestDTO.getOwnerEId()));
        useCase.setApproverId(requestDTO.getApproverId());
        useCase.setCreatorId(requestDTO.getCreatorId());
        useCase.setStatus("IN_REVIEW");
        useCase.setIsActive(false);
        useCase.setIsUpdatedUsecase(true);
        useCase.setUpdatedDate(LocalDateTime.now());

        useCase = useCaseRepository.save(useCase);

        UseCaseContent content = useCaseContentRepository.findByUsecaseId(useCase.getUsecaseId())
                .orElse(new UseCaseContent());
        content.setUsecaseId(useCase.getUsecaseId());

        // Resolve paths from @Value properties
        String thumbnailFolder = thumbnailurlPath.replace("{usecase-id}", String.valueOf(useCase.getUsecaseId()));
        String bannerFolder = bannerurlPath.replace("{usecase-id}", String.valueOf(useCase.getUsecaseId()));
        //String testimonialsFolder = clientTestimonialsPath.replace("{usecase-id}", String.valueOf(useCase.getUsecaseId()));
        //String demoVideosFolder = demoVideosPath.replace("{usecase-id}", String.valueOf(useCase.getUsecaseId()));
        //String elevatorFolder = elevatorPitchPath.replace("{usecase-id}", String.valueOf(useCase.getUsecaseId()));
        //String userStoryFolder = userStoryPath.replace("{usecase-id}", String.valueOf(useCase.getUsecaseId()));

        if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {

            if (content.getThumbnailUrl() != null && !content.getThumbnailUrl().isEmpty()) {
                cloudStorageService.deleteFileFromBlobPath(content.getThumbnailUrl());
            }
            String blobPath = thumbnailFolder + "/" + thumbnailUrl.getOriginalFilename();
            String thumbnailAsUrl = cloudStorageService.uploadFile(thumbnailUrl, blobPath);
            content.setThumbnailUrl(thumbnailAsUrl);
        } else {
            content.setThumbnailUrl(thumbnailUrls);
        }

        if (bannerUrl != null && !bannerUrl.isEmpty()) {

            if (content.getBannerUrl() != null && !content.getBannerUrl().isEmpty()) {
                cloudStorageService.deleteFileFromBlobPath(content.getBannerUrl());
            }
            String blobPath = bannerFolder + "/" + bannerUrl.getOriginalFilename();
            String bannersasUrl = cloudStorageService.uploadFile(bannerUrl, blobPath);
            content.setBannerUrl(bannersasUrl);
        } else {
            content.setBannerUrl(bannerUrls);
        }

        // Clear collections only if updating the same use case
        if (existingUseCase.getUsecaseId().equals(useCase.getUsecaseId())) {
            useCase.getTags().clear();
            useCase.getSpeakers().clear();
            //useCase.getArtifacts().clear();
            useCase.getFaqs().clear();
        }

        // Upload artifacts
        //addArtifactsFromFiles(useCase, testimonialsFolder, clientTestimonials, "CLIENT_TESTIMONIAL");
        //addArtifactsFromFiles(useCase, demoVideosFolder, demoVideos, "DEMO_VIDEO");
        //addArtifactsFromFiles(useCase, elevatorFolder, elevatorPitch, "ELEVATOR_PITCH");
        //addArtifactsFromFiles(useCase, userStoryFolder, userStory, "USER_STORY");

        handleArtifacts(useCase, "usecase/artifacts/Client Testimonials/" + usecaseId, "CLIENT_TESTIMONIAL", clientTestimonialsUrls, clientTestimonials);
        handleArtifacts(useCase, "usecase/artifacts/Demo Videos/" + usecaseId, "DEMO_VIDEO", demoVideosUrls, demoVideos);
        handleArtifacts(useCase, "usecase/artifacts/Client Credentials/user_story/" + usecaseId, "USER_STORY", userStoryUrls, userStory);
        handleArtifacts(useCase, "usecase/artifacts/Client Credentials/elevator_pitch/" + usecaseId, "ELEVATOR_PITCH", elevatorPitchUrls, elevatorPitch);

        handleDemoVideoLinks(useCase, demoVideoExistingLinks, demoVideoLinks);
        // Update content details
        content.setDescription(requestDTO.getDescription());
        content.setBusinessProblem(requestDTO.getBusinessProblem());
        content.setSolution(requestDTO.getSolutions());
        content.setToolsAndTechnologies(requestDTO.getToolsAndTechnologies());
        content.setKeyResults(requestDTO.getKeyResults());
        content.setValueDelivered(requestDTO.getValueDelivered());
        content.setDuration(requestDTO.getDuration());
        content.setNarrationGuide(requestDTO.getNarrationGuide());

        useCaseContentRepository.save(content);

        // Add related entities
        addTags(useCase, requestDTO);
        addSpeakers(useCase, requestDTO);
        addUseCaseFaqs(useCase, requestDTO);

        useCaseRepository.save(useCase);

        return buildResponseFromEntities(useCase, content);
    }

    public Map<String, Object> getApprovedActiveUseCaseCount() {

        long count = useCaseRepository.countByStatusAndIsActive("APPROVED", true);

        Map<String, Object> response = new HashMap<>();
        response.put("Total Approved usecases", formatCount(count));

        return response;
    }
    private String formatCount(long count) {
        if (count < 10) {
            return String.valueOf(count);
        }

        if (count % 10 == 0) {
            return String.valueOf(count);
        }

        long rounded = (count / 10) * 10;
        return rounded + "+";
    }

    @Override
    public List<Map<String, Object>> getUseCaseCountByIndustry() {

        List<Object[]> results = useCaseRepository.getApprovedActiveUseCaseCountByIndustry();

        List<Map<String, Object>> response = new ArrayList<>();

        for (Object[] row : results) {
            Map<String, Object> data = new HashMap<>();
            data.put("industryId", row[0]);
            data.put("usecaseCount", row[1]);
            response.add(data);
        }

        return response;
    }

    @Override
    @Transactional
    public UseCaseResponseDTO updateUseCaseAndSaveBySuperAdminWithBlob(Integer usecaseId, UseCaseRequestDTO requestDTO, List<MultipartFile> clientTestimonials,
                                                                       List<MultipartFile> demoVideos, MultipartFile thumbnailUrl, MultipartFile bannerUrl,
                                                                       List<MultipartFile> elevatorPitch, List<MultipartFile> userStory, List<String> clientTestimonialsUrls,
                                                                       List<String> demoVideosUrls,List<String> elevatorPitchUrls,List<String> userStoryUrls, String thumbnailUrls,
                                                                       String bannerUrls, List<String> demoVideoExistingLinks, List<String> demoVideoLinks) {

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
            useCase.setParentUsecaseId(null);
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

        String thumbnailFolder = thumbnailurlPath.replace("{usecase-id}", String.valueOf(useCase.getUsecaseId()));
        String bannerFolder = bannerurlPath.replace("{usecase-id}", String.valueOf(useCase.getUsecaseId()));

        if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {

            if (content.getThumbnailUrl() != null && !content.getThumbnailUrl().isEmpty()) {
                cloudStorageService.deleteFileFromBlobPath(content.getThumbnailUrl());
            }
            String blobPath = thumbnailFolder + "/" + thumbnailUrl.getOriginalFilename();
            String thumbnailAsUrl = cloudStorageService.uploadFile(thumbnailUrl, blobPath);
            content.setThumbnailUrl(thumbnailAsUrl);
        } else {
            content.setThumbnailUrl(thumbnailUrls);
        }

        if (bannerUrl != null && !bannerUrl.isEmpty()) {

            if (content.getBannerUrl() != null && !content.getBannerUrl().isEmpty()) {
                cloudStorageService.deleteFileFromBlobPath(content.getBannerUrl());
            }
            String blobPath = bannerFolder + "/" + bannerUrl.getOriginalFilename();
            String bannersasUrl = cloudStorageService.uploadFile(bannerUrl, blobPath);
            content.setBannerUrl(bannersasUrl);
        } else {
            content.setBannerUrl(bannerUrls);
        }

        if (existingUseCase.getUsecaseId().equals(useCase.getUsecaseId())) {
            useCase.getTags().clear();
            useCase.getSpeakers().clear();
            //useCase.getArtifacts().clear();
            useCase.getFaqs().clear();
        }

        handleArtifacts(useCase, "usecase/artifacts/Client Testimonials/" + usecaseId, "CLIENT_TESTIMONIAL", clientTestimonialsUrls, clientTestimonials);
        handleArtifacts(useCase, "usecase/artifacts/Demo Videos/" + usecaseId, "DEMO_VIDEO", demoVideosUrls, demoVideos);
        handleArtifacts(useCase, "usecase/artifacts/Client Credentials/user_story/" + usecaseId, "USER_STORY", userStoryUrls, userStory);
        handleArtifacts(useCase, "usecase/artifacts/Client Credentials/elevator_pitch/" + usecaseId, "ELEVATOR_PITCH", elevatorPitchUrls, elevatorPitch);

        handleDemoVideoLinks(useCase, demoVideoExistingLinks, demoVideoLinks);

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

    @Override
    @Transactional
    public UseCaseResponseDTO createUseCaseAndSaveBySuperAdminWithBlob(UseCaseRequestDTO requestDTO, List<MultipartFile> clientTestimonials, List<MultipartFile> demoVideos, MultipartFile thumbnailUrl, MultipartFile bannerUrl, List<MultipartFile> elevatorPitch, List<MultipartFile> userStory) {
        logger.debug(ManufacturingLabConstants.LOG_SUBMITTING_FOR_APPROVAL, requestDTO.getTitle());

        UseCase useCase = UseCase.builder()
                .valueChainId(requestDTO.getValueChainId())
                .title(requestDTO.getTitle())
                .ownerEid(String.valueOf(requestDTO.getOwnerEId()))
                .status(requestDTO.getStatus() != null ? requestDTO.getStatus() : UseCaseStatus.APPROVED.name())
                .approverId(requestDTO.getCreatorId())
                .isUpdatedUsecase(false)
                .createdDate(LocalDateTime.now())
                .isActive(true)
                .creatorId(requestDTO.getCreatorId())
                .approvedDate(LocalDateTime.now())
                .build();

        addTags(useCase, requestDTO);

        addSpeakers(useCase, requestDTO);

        //addArtifacts(useCase, requestDTO);

        addUseCaseFaqs(useCase, requestDTO);

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

        uploadArtifacts(useCase, clientTestimonials, "CLIENT_TESTIMONIAL", clientTestimonialsPath);
        uploadArtifacts(useCase, demoVideos, "DEMO_VIDEO", demoVideosPath);
        uploadArtifacts(useCase, elevatorPitch, "ELEVATOR_PITCH", elevatorPitchPath);
        uploadArtifacts(useCase, userStory, "USER_STORY", userStoryPath);

        useCase = useCaseRepository.save(useCase);

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

    @Transactional
    @Override
    public UseCaseResponseDTO approveUseCaseWithoutEditingBySuperAdmin(Integer usecaseId, Integer approverId) {

        UseCase useCase = useCaseRepository.findById(usecaseId)
                .orElseThrow(() ->
                        new CommonException(
                                CommonExceptionConstants.BAD_REQUEST,
                                "Usecase not found with ID: " + usecaseId));

        useCase.setStatus("APPROVED");
        useCase.setIsActive(true);
        useCase.setApproverId(approverId);
        useCase.setApprovedDate(LocalDateTime.now());
        useCase.setUpdatedDate(LocalDateTime.now());
        useCase.setIsUpdatedUsecase(true);

        UseCase savedUseCase = useCaseRepository.save(useCase);

        UseCaseContent content = useCaseContentRepository
                .findByUsecaseId(savedUseCase.getUsecaseId())
                .orElse(null);

        ValueChain valueChain = valueChainRepository
                .findById(Long.valueOf(savedUseCase.getValueChainId()))
                .orElseThrow(() ->
                        new CommonException(
                                CommonExceptionConstants.BAD_REQUEST, "Value chain not found"));

        return buildResponseFromEntities(savedUseCase, content, valueChain.getIndustryId(), valueChain.getSubIndustryId());
    }

    @Transactional
    @Override
    public UseCaseResponseDTO sendBackToDraftWithoutEditingBySuperAdmin(Integer usecaseId) {

        UseCase useCase = useCaseRepository.findById(usecaseId)
                .orElseThrow(() ->
                        new CommonException(
                                CommonExceptionConstants.BAD_REQUEST,
                                "Usecase not found with ID: " + usecaseId
                        ));

        useCase.setStatus("DRAFT");
        useCase.setIsActive(false);
        useCase.setUpdatedDate(LocalDateTime.now());
        useCase.setIsUpdatedUsecase(true);

        UseCase savedUseCase = useCaseRepository.save(useCase);

        UseCaseContent content = useCaseContentRepository
                .findByUsecaseId(savedUseCase.getUsecaseId())
                .orElse(null);

        ValueChain valueChain = valueChainRepository
                .findById(Long.valueOf(savedUseCase.getValueChainId()))
                .orElseThrow(() ->
                        new CommonException(
                                CommonExceptionConstants.BAD_REQUEST,
                                "Value chain not found"
                        ));

        return buildResponseFromEntities(savedUseCase, content, valueChain.getIndustryId(), valueChain.getSubIndustryId());
    }

    @Override
    public Favourite addUseCaseAsFavourite(Integer userId, Integer usecaseId) {

        Optional<Favourite> existing = favouriteUseCaseRepository.findByUserIdAndUsecaseId(userId, usecaseId);

        if (existing.isPresent()) {
            throw new CommonException(CommonExceptionConstants.BAD_REQUEST, "Use case already marked as favourite");
        }

        Favourite favourite = Favourite.builder()
                .userId(userId)
                .usecaseId(usecaseId)
                .lastUpdated(LocalDateTime.now())
                .build();

        return favouriteUseCaseRepository.save(favourite);
    }
    @Override
    @Transactional(readOnly = true)
    public List<UseCaseResponseDTO> getFavouriteUseCases(Integer userId, Integer page, Integer size) {


        Pageable pageable = PageRequest.of(page - 1, size);

        Page<Favourite> favouritePage = favouriteUseCaseRepository.findByUserId(userId, pageable);

        if (!favouritePage.hasContent()) {
            return Collections.emptyList();
        }

        List<Integer> usecaseIds = favouritePage.getContent().stream()
                .map(Favourite::getUsecaseId)
                .distinct()
                .toList();

        List<UseCase> useCases = useCaseRepository.findByUsecaseIdIn(usecaseIds);

        Map<Integer, UseCase> useCaseMap = useCases.stream()
                .collect(Collectors.toMap(
                        UseCase::getUsecaseId,
                        Function.identity()
                ));

        Map<Integer, UseCaseContent> contentMap =
                useCaseContentRepository.findByUsecaseIdIn(usecaseIds)
                        .stream()
                        .collect(Collectors.toMap(
                                UseCaseContent::getUsecaseId,
                                Function.identity(),
                                (a, b) -> a
                        ));

        return usecaseIds.stream()
                .map(id -> buildResponseFromEntities(
                        useCaseMap.get(id),
                        contentMap.get(id)
                ))
                .toList();
    }

    @Transactional
    public void saveFaqs(Integer usecaseId, List<FaqDTO> faqDTOs) {

        UseCase useCase = useCaseRepository.findById(usecaseId)
                .orElseThrow(() ->
                        new RuntimeException("Use case not found"));

        List<UseCaseFaq> existingFaqs =
                useCaseFaqRepository.findByUseCase(useCase);

        Map<Integer, UseCaseFaq> existingMap = existingFaqs.stream()
                .collect(Collectors.toMap(
                        UseCaseFaq::getUsecaseFaqId,
                        Function.identity()
                ));
        Set<Integer> incomingIds = new HashSet<>();

        for (FaqDTO dto : faqDTOs) {

            if (dto.getUsecaseFaqId() != null &&
                    existingMap.containsKey(dto.getUsecaseFaqId())) {

                UseCaseFaq faq =
                        existingMap.get(dto.getUsecaseFaqId());

                faq.setQuestion(dto.getQuestion());
                faq.setAnswer(dto.getAnswer());
                faq.setUpdatedBy(dto.getUpdatedBy());
                faq.setLastUpdated(LocalDateTime.now());

                incomingIds.add(dto.getUsecaseFaqId());
            } else {

                UseCaseFaq newFaq = UseCaseFaq.builder()
                        .useCase(useCase)
                        .question(dto.getQuestion())
                        .answer(dto.getAnswer())
                        .updatedBy(dto.getUpdatedBy())
                        .lastUpdated(LocalDateTime.now())
                        .build();

                useCaseFaqRepository.save(newFaq);
            }
        }
        List<UseCaseFaq> toDelete = existingFaqs.stream()
                .filter(faq ->
                        !incomingIds.contains(faq.getUsecaseFaqId()))
                .toList();

        useCaseFaqRepository.deleteAll(toDelete);
    }

    @Override
    public List<FaqDTO> getFaqsByUseCaseId(Integer usecaseId) {

        List<UseCaseFaq> faqs = useCaseFaqRepository.findByUseCaseUsecaseId(usecaseId);

        return faqs.stream()
                .map(faq -> FaqDTO.builder()
                        .usecaseFaqId(faq.getUsecaseFaqId())
                        .usecaseId(faq.getUseCase().getUsecaseId())
                        .question(faq.getQuestion())
                        .answer(faq.getAnswer())
                        .updatedBy(faq.getUpdatedBy())
                        .lastUpdated(faq.getLastUpdated())
                        .build())
                .toList();
    }

    @Override
    public void removeFavourite(Integer userId, Integer usecaseId) {

        Favourite favourite = favouriteUseCaseRepository.findByUserIdAndUsecaseId(userId, usecaseId)
                .orElseThrow(() -> new CommonException(CommonExceptionConstants.BAD_REQUEST, "Favourite use case not found"));

        favouriteUseCaseRepository.delete(favourite);
    }

    @Transactional
    private void handleDemoVideoLinks(UseCase useCase, List<String> demoVideoExistingLinks, List<String> demoVideoLinks) {

        final String LINK_TYPE = "DEMO_VIDEO_LINK";

        Set<String> finalSet = demoVideoExistingLinks == null
                ? new HashSet<>()
                : demoVideoExistingLinks.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toSet());

        if (demoVideoLinks != null) {
            finalSet.addAll(
                    demoVideoLinks.stream()
                            .filter(Objects::nonNull)
                            .map(String::trim)
                            .filter(s -> !s.isBlank())
                            .collect(Collectors.toSet())
            );
        }

        Set<String> dbSet = useCase.getArtifacts().stream()
                .filter(a -> LINK_TYPE.equalsIgnoreCase(a.getArtifactType()))
                .map(a -> a.getUrl().trim())
                .collect(Collectors.toSet());

        Set<String> toDelete = new HashSet<>(dbSet);
        toDelete.removeAll(finalSet);

        Set<String> toInsert = new HashSet<>(finalSet);
        toInsert.removeAll(dbSet);

        Iterator<UseCaseArtifact> iterator = useCase.getArtifacts().iterator();

        while (iterator.hasNext()) {
            UseCaseArtifact artifact = iterator.next();

            if (!LINK_TYPE.equalsIgnoreCase(artifact.getArtifactType())) {
                continue;
            }

            String dbUrl = artifact.getUrl().trim();

            if (toDelete.contains(dbUrl)) {
                iterator.remove();
                useCaseArtifactRepository.deleteById(artifact.getArtifactId());
            }
        }

        for (String link : toInsert) {

            UseCaseArtifact newArtifact = UseCaseArtifact.builder()
                    .useCase(useCase)
                    .artifactType(LINK_TYPE)
                    .artifactName("DEMO_VIDEO_LINK")
                    .url(link)
                    .build();

            useCase.getArtifacts().add(newArtifact);
        }
    }

    @Transactional
    private void saveDemoVideoLinks(UseCase useCase, List<String> demoVideoLinks) {

        final String LINK_TYPE = "DEMO_VIDEO_LINK";

        if (demoVideoLinks == null || demoVideoLinks.isEmpty()) {
            return;
        }

        Set<String> existingSet = useCase.getArtifacts().stream()
                .filter(a -> LINK_TYPE.equalsIgnoreCase(a.getArtifactType()))
                .map(a -> a.getUrl().trim())
                .collect(Collectors.toSet());

        Set<String> incomingSet = demoVideoLinks.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toSet());

        incomingSet.removeAll(existingSet);

        for (String link : incomingSet) {

            UseCaseArtifact artifact = UseCaseArtifact.builder()
                    .useCase(useCase)
                    .artifactType(LINK_TYPE)
                    .artifactName("DEMO_VIDEO_LINK")
                    .url(link)
                    .build();

            useCase.getArtifacts().add(artifact);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UseCaseResponseDTO> getUseCasesByStatus(String status, Integer page, Integer size) {

        validateStatus(status);


        Pageable pageable = PageRequest.of(page - 1, size);

        Page<UseCase> useCasePage = fetchUseCases(status, pageable);

        if (!useCasePage.hasContent()) {
            return Collections.emptyList();
        }

        List<Integer> ids = useCasePage.getContent().stream()
                .map(UseCase::getUsecaseId)
                .toList();

        Map<Integer, UseCaseContent> contentMap =
                useCaseContentRepository.findByUsecaseIdIn(ids)
                        .stream()
                        .collect(Collectors.toMap(
                                UseCaseContent::getUsecaseId,
                                Function.identity(),
                                (a, b) -> a
                        ));

        return useCasePage.getContent().stream()
                .map(useCase -> buildResponseFromEntities(
                        useCase,
                        contentMap.get(useCase.getUsecaseId())
                ))
                .toList();
    }

    private void validateStatus(String status) {
        if (status == null) {
            throw new IllegalArgumentException("Status is required");
        }

        List<String> allowed = List.of("DRAFT", "IN_REVIEW", "APPROVED", "ARCHIVED");

        if (!allowed.contains(status.toUpperCase())) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
    }

    private Page<UseCase> fetchUseCases(String status, Pageable pageable) {

        if ("APPROVED".equalsIgnoreCase(status)) {
            return useCaseRepository.findByStatusAndIsActiveTrue("APPROVED", pageable);
        }

        return useCaseRepository.findByStatus(status.toUpperCase(), pageable);
    }

}
