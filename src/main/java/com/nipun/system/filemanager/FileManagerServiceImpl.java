package com.nipun.system.filemanager;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.nipun.system.filemanager.exceptions.FileUploadFailedException;
import com.nipun.system.filemanager.exceptions.InvalidFileTypeException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class FileManagerServiceImpl implements FileManagerService {

    @Value("${cloud.blob-storage.azure.endpoint}")
    private String BLOB_SERVICE_ENDPOINT;
    @Value("${cloud.blob-storage.azure.token}")
    private String BLOB_SERVICE_TOKEN;
    @Value("${cloud.blob-storage.azure.container}")
    private String BLOB_CONTAINER_NAME;

    private final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE
    );

    @Override
    public String uploadFileToCloud(MultipartFile file) {
        if (file.getOriginalFilename() == null) return null;

        if (!ALLOWED_MIME_TYPES.contains(file.getContentType())) throw new InvalidFileTypeException();

        try {
            BlobServiceClient serviceClient = new BlobServiceClientBuilder()
                    .endpoint(BLOB_SERVICE_ENDPOINT)
                    .sasToken(BLOB_SERVICE_TOKEN)
                    .buildClient();

            BlobContainerClient containerClient = serviceClient.getBlobContainerClient(BLOB_CONTAINER_NAME);

            BlobClient blobClient = containerClient.getBlobClient(file.getOriginalFilename());

            blobClient.upload(file.getInputStream(), file.getSize(), true);

            return blobClient.getBlobUrl();

        } catch (IOException e) {
            throw new FileUploadFailedException();
        }
    }
}
