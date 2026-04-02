package com.ix.manufacturinglab.storage;
import org.springframework.web.multipart.MultipartFile;
/**
 * Interface for cloud storage operations.
 */
public interface CloudStorageService {
    /**
     * Uploads a file to cloud storage at the specified path.
     *
     * @param file the file to upload
     * @param blobPath the destination path in cloud storage
     * @return the URL of the uploaded file
     */
    String uploadFile(MultipartFile file, String blobPath);
}