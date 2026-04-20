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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
    public void createHomePage(HomePageRequestDTO requestDTO, MultipartFile heroImageUrl, MultipartFile keyCapConfigUrl, MultipartFile industryThumbnailUrl) {

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

                List<IndustryThumbnails> thumbnails = requestDTO.getIndustryThumbnails()
                        .stream()
                        .map(t -> {

                            if (t.getIndustryId() == null) {
                                throw new CommonException(CommonExceptionConstants.BAD_REQUEST, "industryId is required");
                            }

                            String blobPath = "industry-thumbnail/" + t.getIndustryId() + "/" +
                                    industryThumbnailUrl.getOriginalFilename();

                            logger.debug("Uploading industry thumbnail for industryId {} at path: {}", t.getIndustryId(), blobPath);

                            String insuatryThumbnailSasUrl = cloudStorageService.uploadFile(industryThumbnailUrl, blobPath);

                            IndustryThumbnails entity = new IndustryThumbnails();
                            entity.setIndustryId(t.getIndustryId());
                            entity.setIndustryThumbnailUrl(insuatryThumbnailSasUrl);

                            return entity;
                        })
                        .toList();

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
}