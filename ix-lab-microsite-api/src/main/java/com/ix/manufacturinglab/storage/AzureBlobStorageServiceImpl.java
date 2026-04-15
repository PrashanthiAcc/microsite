package com.ix.manufacturinglab.storage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Collections;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.azure.core.http.rest.PagedIterable;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.ix.manufacturinglab.repository.UseCaseArtifactRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.ix.manufacturinglab.constants.CommonExceptionConstants;
import com.ix.manufacturinglab.exception.CommonException;
import com.ix.manufacturinglab.util.ContentTypeUtil;

/**
 * Azure Blob Storage implementation of CloudStorageService.
 */
@Service
public class AzureBlobStorageServiceImpl implements CloudStorageService {
    private static final Logger logger = LoggerFactory.getLogger(AzureBlobStorageServiceImpl.class);
    private final BlobContainerClient blobContainerClient;

    private final UseCaseArtifactRepository useCaseArtifactRepository;
    private static final int CHUNK_SIZE = 8 * 1024 * 1024; // 8MB per block
    public AzureBlobStorageServiceImpl(BlobContainerClient blobContainerClient, UseCaseArtifactRepository useCaseArtifactRepository) {
        this.blobContainerClient = blobContainerClient;
        this.useCaseArtifactRepository = useCaseArtifactRepository;
    }
    @Override
    public String uploadFile(MultipartFile file, String blobPath) {
        try {
            BlobClient blobClient = blobContainerClient.getBlobClient(blobPath);
            blobClient.upload(file.getInputStream(), file.getSize(), true);

            String contentType = ContentTypeUtil.getContentType(file.getOriginalFilename());


            blobClient.setHttpHeaders(new BlobHttpHeaders().setContentType(contentType));

            BlobSasPermission permission = new BlobSasPermission().setReadPermission(true);
            OffsetDateTime expiry = OffsetDateTime.now().plusYears(1);

            BlobServiceSasSignatureValues values = new BlobServiceSasSignatureValues(expiry, permission).setContentDisposition("inline");

            String sasToken = blobClient.generateSas(values);
            String sasUrl = blobClient.getBlobUrl() + "?" + sasToken;

            logger.debug("File uploaded & SAS generated: {}", blobPath);
            return sasUrl;

        } catch (IOException e) {
            logger.error("Failed to upload file to blob path {}: {}", blobPath, e.getMessage(), e);
            throw new CommonException(CommonExceptionConstants.BAD_REQUEST,
                    "Failed to upload file: " + file.getOriginalFilename());
        }
    }

    @Override
    public String updateFile(MultipartFile file, String blobPath) {
        try {
            String folderPath = blobPath.substring(0, blobPath.lastIndexOf("/") + 1);
            deleteAllFilesInFolder(folderPath);

            BlobClient blobClient = blobContainerClient.getBlobClient(blobPath);
            blobClient.upload(file.getInputStream(), file.getSize(), true);

            String contentType = ContentTypeUtil.getContentType(file.getOriginalFilename());
            blobClient.setHttpHeaders(new BlobHttpHeaders().setContentType(contentType));

            BlobSasPermission permission = new BlobSasPermission().setReadPermission(true);
            OffsetDateTime expiry = OffsetDateTime.now().plusYears(1);
            BlobServiceSasSignatureValues values = new BlobServiceSasSignatureValues(expiry, permission)
                    .setContentDisposition("inline");

            String sasToken = blobClient.generateSas(values);
            String sasUrl = blobClient.getBlobUrl() + "?" + sasToken;

            return sasUrl;

        } catch (IOException e) {
            logger.error("Failed to upload file '{}' to blob path {}: {}", file.getOriginalFilename(), blobPath, e.getMessage(), e);
            throw new CommonException(CommonExceptionConstants.BAD_REQUEST,
                    "Failed to upload file: " + file.getOriginalFilename());
        }
    }

    /*
    @Override
    public void deleteAllFilesInFolder(String folderPath) {
        PagedIterable<BlobItem> blobItems = blobContainerClient.listBlobsByHierarchy(folderPath + "/");
        for (BlobItem blobItem : blobItems) {
            blobContainerClient.getBlobClient(blobItem.getName()).delete();
        }
    }

     */

    @Override
    public void deleteAllFilesInFolder(String folderPath) {

        if (folderPath.endsWith("/")) {
            folderPath = folderPath.substring(0, folderPath.length() - 1);
        }

        String[] parts = folderPath.split("/");

        String lastPart = parts[parts.length - 1];
        String artifactTypePart = parts[parts.length - 2];

        Long usecaseId = Long.valueOf(lastPart);
        String artifactType = mapArtifactType(artifactTypePart);

        String prefix = folderPath + "/";

        PagedIterable<BlobItem> blobItems = blobContainerClient.listBlobs(new ListBlobsOptions().setPrefix(prefix), null);

        for (BlobItem blobItem : blobItems) {
            String blobName = blobItem.getName();
            blobContainerClient.getBlobClient(blobName).deleteIfExists();
        }

        int deleted = useCaseArtifactRepository
                .deleteByUseCase_UsecaseIdAndArtifactType(usecaseId, artifactType);

        logger.info("Deleted {} DB records for usecaseId={}, artifactType={}",
                deleted, usecaseId, artifactType);
    }

    private String mapArtifactType(String folderName) {
        switch (folderName.trim().toLowerCase()) {
            case "demo videos":
                return "DEMO_VIDEO";
            case "elevator_pitch":
                return "ELEVATOR_PITCH";
            case "user_story":
                return "USER_STORY";
            case "client testimonials":
                return "CLIENT_TESTIMONIAL";
            default:
                throw new IllegalArgumentException("Unknown artifact type: " + folderName);
        }
    }

    @Override
    public String uploadFileChunked(MultipartFile file, String blobPath) {
        int THREADS = 2;
        int MAX_RETRIES = 3;

        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        List<String> blockIds = Collections.synchronizedList(new ArrayList<>());
        List<Future<?>> futures = new ArrayList<>();

        try {
            blobPath = blobPath.endsWith("/")
                    ? blobPath + file.getOriginalFilename()
                    : blobPath + "/" + file.getOriginalFilename();

            BlobClient blobClient = blobContainerClient.getBlobClient(blobPath);
            BlockBlobClient blockBlobClient = blobClient.getBlockBlobClient();

            InputStream inputStream = file.getInputStream();
            byte[] buffer = new byte[CHUNK_SIZE];

            int blockNumber = 0;
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {

                byte[] chunkData = Arrays.copyOf(buffer, bytesRead);

                String blockId = Base64.getEncoder().encodeToString(
                        String.format("%06d", blockNumber).getBytes());

                blockIds.add(blockId);

                int currentBlock = blockNumber;

                futures.add(executor.submit(() -> {
                    int attempt = 0;
                    boolean success = false;

                    while (attempt < MAX_RETRIES && !success) {
                        long start = System.currentTimeMillis();
                        try (ByteArrayInputStream blockStream = new ByteArrayInputStream(chunkData)) {

                            blockBlobClient.stageBlock(blockId, blockStream, chunkData.length);
                            success = true;

                        } catch (Exception e) {
                            attempt++;
                            logger.warn("Chunk {} failed (Attempt {}): {}",
                                    currentBlock, attempt, e.getMessage());

                            if (attempt >= MAX_RETRIES) {
                                throw new RuntimeException("Chunk " + currentBlock + " failed after retries", e);
                            }

                            try {
                                Thread.sleep(1000L * attempt);
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                            }
                        }
                    }
                }));

                blockNumber++;
            }

            for (Future<?> future : futures) {
                future.get();
            }

            executor.shutdown();

            String contentType = ContentTypeUtil.getContentType(file.getOriginalFilename());
            BlobHttpHeaders headers = new BlobHttpHeaders().setContentType(contentType);

            blockBlobClient.commitBlockListWithResponse(blockIds, headers, null, null, null, null, null);

            BlobSasPermission permission = new BlobSasPermission().setReadPermission(true);
            OffsetDateTime expiry = OffsetDateTime.now().plusYears(1);

            BlobServiceSasSignatureValues values = new BlobServiceSasSignatureValues(expiry, permission).setContentDisposition("inline");

            String sasUrl = blobClient.getBlobUrl() + "?" + blobClient.generateSas(values);

            logger.info("Parallel upload complete: {} ({} chunks)", blobPath, blockNumber);

            return sasUrl;

        } catch (Exception e) {
            logger.error("Parallel chunk upload failed: {}", e.getMessage(), e);
            throw new CommonException(CommonExceptionConstants.BAD_REQUEST,
                    "Failed to upload file: " + file.getOriginalFilename());
        } finally {
            executor.shutdownNow();
        }
    }


    public void deleteFileFromBlobPath(String fileUrl) {
        try {
            if (fileUrl == null || fileUrl.isEmpty()) return;

            String cleanUrl = fileUrl.split("\\?")[0];

            String containerName = blobContainerClient.getBlobContainerName();
            String blobPath = cleanUrl.substring(cleanUrl.indexOf(containerName) + containerName.length() + 1);

            blobPath = URLDecoder.decode(blobPath, StandardCharsets.UTF_8);

            boolean deleted = blobContainerClient.getBlobClient(blobPath).deleteIfExists();

            if (!deleted) {
                logger.error("Blob not found: {}", blobPath);
            }

        } catch (Exception e) {
            logger.error("Delete failed for URL: {}", fileUrl, e);
        }
    }


}