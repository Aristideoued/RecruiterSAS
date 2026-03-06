package com.recruitersaas.service;

import com.recruitersaas.model.ApplicationFile;
import com.recruitersaas.model.JobApplication;
import com.recruitersaas.model.enums.FileType;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    ApplicationFile storeFile(MultipartFile file, JobApplication jobApplication, FileType fileType);

    Resource loadFileAsResource(String fileId);

    void deleteFile(String fileId);
}
