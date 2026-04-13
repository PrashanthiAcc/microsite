package com.ix.manufacturinglab.storage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import com.azure.core.http.rest.PagedIterable;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobItem;
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
    private static final int CHUNK_SIZE = 4 * 1024 * 1024; // 4MB per block
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
        String artifactTypePart = parts[parts.length-2];

        Long usecaseId = null;
        String artifactType = null;

        try {
            usecaseId = Long.valueOf(lastPart);
            artifactType = artifactTypePart;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Invalid folderPath: usecaseId not found in path -> " + folderPath);
        }


        String prefix = folderPath + "/";

        PagedIterable<BlobItem> blobItems = blobContainerClient.listBlobsByHierarchy(prefix);

        for (BlobItem blobItem : blobItems) {
            String blobName = blobItem.getName();
            try {
                blobContainerClient.getBlobClient(blobName).deleteIfExists();
                String fileName = blobName.substring(blobName.lastIndexOf("/") + 1);
                useCaseArtifactRepository.deleteByUseCase_UsecaseIdAndArtifactTypeAndArtifactName(usecaseId, artifactType, fileName);

            } catch (Exception e) {
                logger.error("Failed to delete blob {}", blobName, e);
                throw new RuntimeException("Delete failed for: " + blobName, e);
            }
        }
    }

    @Override
    public String uploadFileChunked(MultipartFile file, String blobPath) {
        try {
            // Append the original file name to the blobPath
            blobPath = blobPath.endsWith("/") ? blobPath + file.getOriginalFilename() : blobPath + "/" + file.getOriginalFilename();

            BlobClient blobClient = blobContainerClient.getBlobClient(blobPath);
            BlockBlobClient blockBlobClient = blobClient.getBlockBlobClient();

            InputStream inputStream = file.getInputStream();
            long fileSize = file.getSize();
            List<String> blockIds = new ArrayList<>();
            byte[] buffer = new byte[CHUNK_SIZE];
            int blockNumber = 0;
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                String blockId = Base64.getEncoder().encodeToString(
                        String.format("%06d", blockNumber).getBytes());
                blockIds.add(blockId);

                try (ByteArrayInputStream blockStream = new ByteArrayInputStream(buffer, 0, bytesRead)) {
                    blockBlobClient.stageBlock(blockId, blockStream, bytesRead);
                }

                blockNumber++;
                logger.debug("Staged block {} ({} bytes) for: {}", blockNumber, bytesRead, blobPath);
            }

            String contentType = ContentTypeUtil.getContentType(file.getOriginalFilename());
            BlobHttpHeaders headers = new BlobHttpHeaders().setContentType(contentType);
            blockBlobClient.commitBlockListWithResponse(blockIds, headers, null, null, null, null, null);

            BlobSasPermission permission = new BlobSasPermission().setReadPermission(true);
            OffsetDateTime expiry = OffsetDateTime.now().plusYears(1);
            BlobServiceSasSignatureValues values = new BlobServiceSasSignatureValues(expiry, permission)
                    .setContentDisposition("inline");

            String sasToken = blobClient.generateSas(values);
            String sasUrl = blobClient.getBlobUrl() + "?" + sasToken;

            logger.info("Chunked upload complete: {} ({} blocks, {} bytes)", blobPath, blockNumber, fileSize);
            return sasUrl;

        } catch (IOException e) {
            logger.error("Chunked upload failed for blob path {}: {}", blobPath, e.getMessage(), e);
            throw new CommonException(CommonExceptionConstants.BAD_REQUEST,
                    "Failed to upload file (chunked): " + file.getOriginalFilename());
        }
    }

}