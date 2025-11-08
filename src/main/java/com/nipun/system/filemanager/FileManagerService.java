package com.nipun.system.filemanager;

import org.springframework.web.multipart.MultipartFile;

public interface FileManagerService {
    String uploadFileToCloud(MultipartFile file);
}
