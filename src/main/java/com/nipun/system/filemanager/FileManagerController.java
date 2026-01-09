package com.nipun.system.filemanager;

import com.nipun.system.filemanager.dtos.FileUploadResponse;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/files")
@Tag(name = "File Manager", description = "Manages files in the system")
public class FileManagerController {

    private final FileManagerService fileManagerService;

    @RateLimiter(name = "default")
    @PostMapping("/images")
    @Operation(summary = "Receive Image", description = "Receives images from the client")
    public ResponseEntity<FileUploadResponse> receiveImage(
            @RequestParam("file")
            @Parameter(description = "The request file")
            MultipartFile file,
            UriComponentsBuilder uriBuilder
    ) {
        var fileUrl = fileManagerService.uploadFileToCloud(file);

        var uri = uriBuilder.path(fileUrl).build().toUri();

        return ResponseEntity.created(uri).body(new FileUploadResponse(fileUrl));
    }
}
