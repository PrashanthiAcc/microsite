package com.ix.manufacturinglab.storage;
import java.io.IOException;
import java.time.OffsetDateTime;

import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
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
    public AzureBlobStorageServiceImpl(BlobContainerClient blobContainerClient) {
        this.blobContainerClient = blobContainerClient;
    }
    @Override
    public String uploadFile(MultipartFile file, String blobPath) {
        try {
            BlobClient blobClient = blobContainerClient.getBlobClient(blobPath);
            blobClient.upload(file.getInputStream(), file.getSize(), true);

            String contentType = ContentTypeUtil.getContentType(file.getOriginalFilename());


            blobClient.setHttpHeaders(new BlobHttpHeaders().setContentType(contentType));

            BlobSasPermission permission = new BlobSasPermission().setReadPermission(true);
            OffsetDateTime expiry = OffsetDateTime.now().plusHours(24);

            BlobServiceSasSignatureValues values = new BlobServiceSasSignatureValues(expiry, permission).setContentDisposition("inline");

            String sasToken = blobClient.generateSas(values);
            String sasUrl = blobClient.getBlobUrl() + "?" + sasToken;

            logger.info("File uploaded & SAS generated: {}", blobPath);
            return sasUrl;

        } catch (IOException e) {
            logger.error("Failed to upload file to blob path {}: {}", blobPath, e.getMessage(), e);
            throw new CommonException(CommonExceptionConstants.BAD_REQUEST,
                    "Failed to upload file: " + file.getOriginalFilename());
        }
    }
}
