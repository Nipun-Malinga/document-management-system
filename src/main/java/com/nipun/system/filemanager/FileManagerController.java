package com.nipun.system.filemanager;

import com.nipun.system.filemanager.dtos.FileUploadResponse;
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
@RequestMapping("/files")
public class FileManagerController {

    private final FileManagerService fileManagerService;

    @PostMapping("/images")
    public ResponseEntity<FileUploadResponse> receiveImage(
            @RequestParam("file") MultipartFile file,
            UriComponentsBuilder uriBuilder
    ) {
        var fileUrl = fileManagerService.uploadFileToCloud(file);

        var uri = uriBuilder.path(fileUrl).build().toUri();

        return ResponseEntity.created(uri).body(new FileUploadResponse(fileUrl));
    }
}
