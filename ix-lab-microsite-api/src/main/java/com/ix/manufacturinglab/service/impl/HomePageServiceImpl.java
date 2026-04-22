package com.ix.manufacturinglab.service.impl;


import com.ix.manufacturinglab.constants.CommonExceptionConstants;
import com.ix.manufacturinglab.dto.FeatureStoriesDTO;
import com.ix.manufacturinglab.dto.HomePageRequestDTO;
import com.ix.manufacturinglab.dto.HomePageResponseDTO;
import com.ix.manufacturinglab.dto.IndustryThumbnailDTO;
import com.ix.manufacturinglab.entity.FeaturedStories;
import com.ix.manufacturinglab.entity.HomePageConfiguration;
import com.ix.manufacturinglab.entity.IndustryThumbnails;
import com.ix.manufacturinglab.exception.CommonException;
import com.ix.manufacturinglab.repository.FeaturedStoriesRepository;
import com.ix.manufacturinglab.repository.HomePageConfigurationRepository;
import com.ix.manufacturinglab.repository.IndustryThumbnailsRepository;
import com.ix.manufacturinglab.service.HomePageService;
import com.ix.manufacturinglab.storage.CloudStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.Set;
import java.util.Optional;
import java.util.Objects;

@Service
public class HomePageServiceImpl implements HomePageService {

    private static final Logger logger = LoggerFactory.getLogger(HomePageServiceImpl.class);
    private final HomePageConfigurationRepository homePageRepository;
    private final FeaturedStoriesRepository featuredStoriesRepository;
    private final IndustryThumbnailsRepository industryThumbnailsRepository;
    private final CloudStorageService cloudStorageService;

    public HomePageServiceImpl(HomePageConfigurationRepository homePageRepository,
                               FeaturedStoriesRepository featuredStoriesRepository,
                               IndustryThumbnailsRepository industryThumbnailsRepository,
                               CloudStorageService cloudStorageService) {
        this.homePageRepository = homePageRepository;
        this.featuredStoriesRepository = featuredStoriesRepository;
        this.industryThumbnailsRepository = industryThumbnailsRepository;
        this.cloudStorageService = cloudStorageService;
    }

    @Override
    @Transactional
    public void createHomePage(HomePageRequestDTO requestDTO, MultipartFile heroImageUrl, MultipartFile keyCapConfigUrl, List<MultipartFile> industryThumbnailUrl) {

        try {

            Optional<HomePageConfiguration> existingOpt =
                    homePageRepository.findByApplicationName(requestDTO.getApplicationName());

            if (existingOpt.isPresent()) {

                HomePageConfiguration existing = existingOpt.get();

                if (existing.getHeroImageUrl() != null) {
                    cloudStorageService.deleteFileFromBlobforhomepage(existing.getHeroImageUrl());
                }

                if (existing.getKeyCapConfUrl() != null) {
                    cloudStorageService.deleteFileFromBlobforhomepage(existing.getKeyCapConfUrl());
                }

                List<IndustryThumbnails> oldThumbs =
                        industryThumbnailsRepository.findAll();

                oldThumbs.forEach(t -> {
                    if (t.getIndustryThumbnailUrl() != null) {
                        cloudStorageService.deleteFileFromBlobforhomepage(t.getIndustryThumbnailUrl());
                    }
                });

                industryThumbnailsRepository.deleteAll();
                featuredStoriesRepository.deleteAll();
                homePageRepository.delete(existing);
            }

            String heroSasUrl = null;
            String keyCapabilityConfigurationSasUrl = null;

            if (heroImageUrl != null && !heroImageUrl.isEmpty()) {
                String blobPath = "hero-image/" + heroImageUrl.getOriginalFilename();
                logger.debug("Uploading heroImageUrl to blob storage at path: {}", blobPath);
                heroSasUrl = cloudStorageService.uploadFile(heroImageUrl, blobPath);
                logger.debug("heroImageUrl uploaded successfully. Blob URL: {}", heroSasUrl);
            }

            if (keyCapConfigUrl != null && !keyCapConfigUrl.isEmpty()) {
                String blobPath = "key-capacity/" + keyCapConfigUrl.getOriginalFilename();
                logger.debug("Uploading keyCapConfigUrl to blob storage at path: {}", blobPath);
                keyCapabilityConfigurationSasUrl = cloudStorageService.uploadFile(keyCapConfigUrl, blobPath);
                logger.debug("keyCapConfigUrl uploaded successfully. Blob URL: {}", heroSasUrl);
            }

            HomePageConfiguration config = HomePageConfiguration.builder()
                    .applicationName(requestDTO.getApplicationName())
                    .title(requestDTO.getTitle())
                    .subtitle(requestDTO.getSubTitle())
                    .mesMomSolDelivered(requestDTO.getMesMomSolDelivered())
                    .prodSiteCriticalSupport(requestDTO.getProdSiteCriticalSupport())
                    .sapEwmPrgDelivered(requestDTO.getSapEwmPrgDelivered())
                    .updatedById(requestDTO.getUpdatedById())
                    .heroImageUrl(heroSasUrl)
                    .keyCapConfUrl(keyCapabilityConfigurationSasUrl)
                    .lastUpdated(LocalDateTime.now())
                    .build();

            homePageRepository.save(config);


            if (requestDTO.getFeaturedStories() != null && !requestDTO.getFeaturedStories().isEmpty()) {

                List<FeaturedStories> stories = requestDTO.getFeaturedStories()
                        .stream()
                        .map(f -> {
                            if (f.getUsecaseId() == null) {
                                throw new CommonException(CommonExceptionConstants.BAD_REQUEST, "usecaseId is required");
                            }

                            FeaturedStories fs = new FeaturedStories();
                            fs.setUsecaseId(f.getUsecaseId());
                            return fs;
                        })
                        .toList();

                featuredStoriesRepository.saveAll(stories);
            }

            if (requestDTO.getIndustryThumbnails() != null && !requestDTO.getIndustryThumbnails().isEmpty()) {

                if (industryThumbnailUrl == null || industryThumbnailUrl.isEmpty()) {
                    throw new CommonException(CommonExceptionConstants.BAD_REQUEST, "Industry thumbnail file is required");
                }

                if (industryThumbnailUrl.size() != requestDTO.getIndustryThumbnails().size()) {
                    throw new CommonException(CommonExceptionConstants.BAD_REQUEST,
                            "Mismatch between industryIds and files count");
                }

                List<IndustryThumbnails> thumbnails = new ArrayList<>();

                for (int i = 0; i < requestDTO.getIndustryThumbnails().size(); i++) {

                    IndustryThumbnailDTO t = requestDTO.getIndustryThumbnails().get(i);
                    MultipartFile file = industryThumbnailUrl.get(i);

                    if (t.getIndustryId() == null) {
                        throw new CommonException(CommonExceptionConstants.BAD_REQUEST, "industryId is required");
                    }

                    String blobPath = "industry-thumbnail/" + t.getIndustryId() + "/" +
                            file.getOriginalFilename();

                    logger.debug("Uploading industry thumbnail for industryId {} at path: {}", t.getIndustryId(), blobPath);

                    String industryThumbnailSasUrl = cloudStorageService.uploadFile(file, blobPath);

                    IndustryThumbnails entity = new IndustryThumbnails();
                    entity.setIndustryId(t.getIndustryId());
                    entity.setIndustryThumbnailUrl(industryThumbnailSasUrl);

                    thumbnails.add(entity);
                }

                industryThumbnailsRepository.saveAll(thumbnails);
            }

        } catch (CommonException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Error while creating Home Page Configuration", ex);

            throw new CommonException(CommonExceptionConstants.INTERNAL_SERVER_ERROR, "Failed to initialize Home Page Configuration"
            );
        }
    }


    @Override
    public HomePageResponseDTO getHomePage() {

        try {
            HomePageConfiguration config = homePageRepository.findAll()
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new CommonException(
                            CommonExceptionConstants.NOT_FOUND,
                            "Home Page Configuration not found"
                    ));

            List<FeatureStoriesDTO> stories = featuredStoriesRepository.findAll()
                    .stream()
                    .map(fs -> new FeatureStoriesDTO(
                            fs.getFeatureStoryId(),
                            fs.getUsecaseId()
                    ))
                    .toList();

            List<IndustryThumbnailDTO> thumbnails = industryThumbnailsRepository.findAll()
                    .stream()
                    .map(t -> new IndustryThumbnailDTO(
                            t.getIndustryThumbnailId(),
                            t.getIndustryId(),
                            t.getIndustryThumbnailUrl()
                    ))
                    .toList();

            return HomePageResponseDTO.builder()
                    .applicationName(config.getApplicationName())
                    .title(config.getTitle())
                    .subTitle(config.getSubtitle())
                    .heroImageUrl(config.getHeroImageUrl())
                    .mesMomSolDelivered(config.getMesMomSolDelivered())
                    .prodSiteCriticalSupport(config.getProdSiteCriticalSupport())
                    .sapEwmPrgDelivered(config.getSapEwmPrgDelivered())
                    .keyCapabilityConfigurationImage(config.getKeyCapConfUrl())
                    .updatedById(config.getUpdatedById())
                    .featuredStories(stories)
                    .industryThumbnails(thumbnails)
                    .build();

        } catch (CommonException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Error while fetching Home Page Configuration", ex);

            throw new CommonException(
                    CommonExceptionConstants.INTERNAL_SERVER_ERROR,
                    "Failed to fetch Home Page Configuration"
            );
        }
    }

    @Transactional
    public void updateHomePage(HomePageRequestDTO dto, MultipartFile heroFile, MultipartFile keyCapFile, List<MultipartFile> industryFiles,
                               String heroUrl, String keyCapUrl, List<String> industryThumbnailFileUrls) {

        HomePageConfiguration config = homePageRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Home page config not found"));

        config.setApplicationName(dto.getApplicationName());
        config.setTitle(dto.getTitle());
        config.setSubtitle(dto.getSubTitle());
        config.setMesMomSolDelivered(dto.getMesMomSolDelivered());
        config.setProdSiteCriticalSupport(dto.getProdSiteCriticalSupport());
        config.setSapEwmPrgDelivered(dto.getSapEwmPrgDelivered());
        config.setUpdatedById(dto.getUpdatedById());
        config.setLastUpdated(LocalDateTime.now());

        config.setHeroImageUrl(
                handleSingleUpdate(config.getHeroImageUrl(), heroUrl, heroFile, "hero-image/"));

        config.setKeyCapConfUrl(
                handleSingleUpdate(config.getKeyCapConfUrl(), keyCapUrl, keyCapFile, "key-capacity/")
        );

        updateIndustryThumbnails(dto, industryFiles, industryThumbnailFileUrls);

        handleFeaturedStories(dto);

        homePageRepository.save(config);
    }

    private String handleSingleUpdate(String existingUrl, String incomingUrl, MultipartFile file, String folder) {

        if (file != null && !file.isEmpty()) {

            if (folder.endsWith("/")) {
                folder = folder.substring(0, folder.length() - 1);
            }

            String fileName = file.getOriginalFilename().trim();

            String blobPath = folder + "/" + fileName;

            if (existingUrl != null && !existingUrl.isBlank()) {
                String oldPath = extractBlobPath(existingUrl);
                if (oldPath != null) {
                    cloudStorageService.deleteFile(oldPath);
                }
            }

            return cloudStorageService.uploadFile(file, blobPath);
        }

        if (incomingUrl != null && !incomingUrl.isBlank()) {
            return incomingUrl;
        }

        return existingUrl;
    }

    @Transactional
    public void updateIndustryThumbnails(HomePageRequestDTO dto, List<MultipartFile> industryFiles,
                                         List<String> industryThumbnailFileUrls) {

        if (industryThumbnailFileUrls != null) {
            if (industryThumbnailFileUrls.size() == 1 && industryThumbnailFileUrls.get(0).isBlank()) {
                industryThumbnailFileUrls = Collections.emptyList();
            }
        } else {
            industryThumbnailFileUrls = Collections.emptyList();
        }

        List<String> incomingPaths = industryThumbnailFileUrls.stream()
                .map(this::extractBlobPath)
                .filter(Objects::nonNull)
                .map(String::trim)
                .toList();

        List<IndustryThumbnails> existing = industryThumbnailsRepository.findAll();

        for (IndustryThumbnails old : existing) {

            String dbPath = extractBlobPath(old.getIndustryThumbnailUrl());

            if (dbPath != null && !incomingPaths.contains(dbPath)) {

                cloudStorageService.deleteFile(dbPath);
                industryThumbnailsRepository.deleteById(old.getIndustryThumbnailId());
            }
        }

        if (industryFiles != null && !industryFiles.isEmpty()) {

            Set<Long> existingIds = incomingPaths.stream()
                    .map(path -> Long.parseLong(path.split("/")[1]))
                    .collect(Collectors.toSet());

            List<Long> newIds = dto.getIndustryThumbnails().stream()
                    .map(i -> i.getIndustryId())
                    .filter(id -> !existingIds.contains(id))
                    .toList();

            int index = 0;

            for (MultipartFile file : industryFiles) {

                if (file.isEmpty()) continue;

                Long industryId = newIds.get(index);

                String fileName = file.getOriginalFilename().trim();
                String blobPath = "industry-thumbnail/" + industryId + "/" + fileName;

                System.out.println("UPLOAD PATH: " + blobPath);

                String url = cloudStorageService.uploadFile(file, blobPath);

                IndustryThumbnails entity = new IndustryThumbnails();
                entity.setIndustryId(industryId);
                entity.setIndustryThumbnailUrl(url);

                industryThumbnailsRepository.save(entity);

                index++;
            }
        }
    }

    @Transactional
    private void handleFeaturedStories(HomePageRequestDTO dto) {

        featuredStoriesRepository.deleteAll();

        if (dto.getFeaturedStories() == null || dto.getFeaturedStories().isEmpty()) {
            return;
        }

        for (var item : dto.getFeaturedStories()) {

            if (item.getUsecaseId() == null) {
                throw new IllegalArgumentException("UsecaseId is required");
            }

            FeaturedStories entity = new FeaturedStories();
            entity.setUsecaseId(item.getUsecaseId());

            featuredStoriesRepository.save(entity);
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
}