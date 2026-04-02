package com.ix.manufacturinglab.storage;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.ix.manufacturinglab.constants.CommonExceptionConstants;
import com.ix.manufacturinglab.exception.CommonException;
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
            logger.info("File uploaded successfully to blob path: {}", blobPath);
            return blobClient.getBlobUrl();
        } catch (IOException e) {
            logger.error("Failed to upload file to blob path {}: {}", blobPath, e.getMessage(), e);
            throw new CommonException(CommonExceptionConstants.BAD_REQUEST,
                    "Failed to upload file: " + file.getOriginalFilename());
        }
    }
}
