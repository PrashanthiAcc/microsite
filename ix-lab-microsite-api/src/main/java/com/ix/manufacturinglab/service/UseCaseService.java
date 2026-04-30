package com.ix.manufacturinglab.service;

import com.ix.manufacturinglab.dto.UseCaseRequestDTO;
import com.ix.manufacturinglab.dto.UseCaseResponseDTO;
import com.ix.manufacturinglab.entity.Favourite;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Service interface for Use Case CRUD operations.
 */
public interface UseCaseService {


    /**
     * Discards a draft use case by deleting it from all related tables based on the given use case ID.
     * This operation is allowed only if the use case is in DRAFT status.
     *
     * @param usecaseId the ID of the draft use case to be discarded
     */

    void discardDraftUseCase (Integer usecaseId);

    /**
     * Get a use case by its ID with all related data.
     *
     * @param usecaseId the use case ID
     * @return the use case response
     */
    UseCaseResponseDTO getUseCaseById(Integer usecaseId);

    /**
     * Get all active use cases.
     *
     * @return list of use case responses
     */
    Page<UseCaseResponseDTO> getAllUseCases(int page, int size);


    /**
     * Soft-delete a use case (set is_active = false).
     *
     * @param usecaseId the use case ID
     */
    void deleteUseCase(Integer usecaseId);

    /**
     * Archives an existing use case.
     *
     * @param usecaseId  the use case ID
     */
    void archiveApprovedUseCase(Integer usecaseId);


    /**
     * Get all ARCHIVE use cases.
     *
     * @return the use cases response
     */
    Page<UseCaseResponseDTO> getAllArchiveUseCases(int page, int size);


    //UseCaseResponseDTO updateUseCaseAndSubmitForApprovalwithBlob(Integer usecaseId, UseCaseRequestDTO requestDTO, List<MultipartFile> clientTestimonials, List<MultipartFile> demoVideos, MultipartFile thumbnailUrl, MultipartFile bannerUrl, List<MultipartFile> elevatorPitch, List<MultipartFile> userStory);

    UseCaseResponseDTO createUseCaseAndSubmitForApprovalwithBlob(UseCaseRequestDTO requestDTO, List<MultipartFile> clientTestimonials, List<MultipartFile> demoVideos, MultipartFile thumbnailUrl, MultipartFile bannerUrl, List<MultipartFile> elevatorPitch, List<MultipartFile> userStory);

    //UseCaseResponseDTO updateUseCaseandSaveasDraftWithBlob(Integer usecaseId, UseCaseRequestDTO requestDTO, List<MultipartFile> clientTestimonials, List<MultipartFile> demoVideos, MultipartFile thumbnailUrl, MultipartFile bannerUrl, List<MultipartFile> elevatorPitch, List<MultipartFile> userStory);

    UseCaseResponseDTO createUseCaseAndSaveAsDraftWithBlob(UseCaseRequestDTO requestDTO, List<MultipartFile> clientTestimonials, List<MultipartFile> demoVideos, MultipartFile thumbnailUrl, MultipartFile bannerUrl, List<MultipartFile> elevatorPitch, List<MultipartFile> userStory);

    String uploadDemoVideoInCheckMode(MultipartFile demoVideo);

    Map<String, Object> getApprovedActiveUseCaseCount();

    UseCaseResponseDTO updateUseCaseandSaveasDraftWithBlob(Integer usecaseId, UseCaseRequestDTO requestDTO, List<MultipartFile> clientTestimonials,
                                                           List<MultipartFile> demoVideos, MultipartFile thumbnailUrl, MultipartFile bannerUrl,
                                                           List<MultipartFile> elevatorPitch, List<MultipartFile> userStory, List<String> clientTestimonialsUrls,
                                                           List<String> demoVideosUrls,List<String> elevatorPitchUrls,List<String> userStoryUrls, String thumbnailUrls, String bannerUrls);

    UseCaseResponseDTO updateUseCaseAndSubmitForApprovalwithBlob(Integer usecaseId, UseCaseRequestDTO requestDTO, List<MultipartFile> clientTestimonials,
                                                                 List<MultipartFile> demoVideos, MultipartFile thumbnailUrl, MultipartFile bannerUrl,
                                                                 List<MultipartFile> elevatorPitch, List<MultipartFile> userStory, List<String> clientTestimonialsUrls,
                                                                 List<String> demoVideosUrls,List<String> elevatorPitchUrls,List<String> userStoryUrls, String thumbnailUrls, String bannerUrls);

    List<Map<String, Object>> getUseCaseCountByIndustry();

    UseCaseResponseDTO updateUseCaseAndSaveBySuperAdminWithBlob(Integer usecaseId, UseCaseRequestDTO requestDTO, List<MultipartFile> clientTestimonials,
                                                                List<MultipartFile> demoVideos, MultipartFile thumbnailUrl, MultipartFile bannerUrl,
                                                                List<MultipartFile> elevatorPitch, List<MultipartFile> userStory, List<String> clientTestimonialsUrls,
                                                                List<String> demoVideosUrls,List<String> elevatorPitchUrls,List<String> userStoryUrls, String thumbnailUrls, String bannerUrls);

    UseCaseResponseDTO createUseCaseAndSaveBySuperAdminWithBlob(UseCaseRequestDTO requestDTO, List<MultipartFile> clientTestimonials,
                                                                List<MultipartFile> demoVideos, MultipartFile thumbnailUrl,
                                                                MultipartFile bannerUrl, List<MultipartFile> elevatorPitch,
                                                                List<MultipartFile> userStory);

    UseCaseResponseDTO approveUseCaseWithoutEditingBySuperAdmin(Integer usecaseId, Integer approverId);

    UseCaseResponseDTO sendBackToDraftWithoutEditingBySuperAdmin(Integer usecaseId);

    List<Favourite> getFavouriteUseCases(Integer userId);

    Favourite addUseCaseAsFavourite(Integer userId, Integer usecaseId);


}
