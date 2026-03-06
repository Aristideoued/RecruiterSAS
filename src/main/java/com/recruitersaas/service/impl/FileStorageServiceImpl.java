package com.recruitersaas.service.impl;

import com.recruitersaas.exception.BusinessException;
import com.recruitersaas.exception.ResourceNotFoundException;
import com.recruitersaas.model.ApplicationFile;
import com.recruitersaas.model.JobApplication;
import com.recruitersaas.model.enums.FileType;
import com.recruitersaas.repository.ApplicationFileRepository;
import com.recruitersaas.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${app.file-storage.upload-dir}")
    private String uploadDir;

    private final ApplicationFileRepository fileRepository;

    @Override
    public ApplicationFile storeFile(MultipartFile file, JobApplication jobApplication, FileType fileType) {
        String originalFileName = Optional.ofNullable(file.getOriginalFilename())
                .map(n -> n.replaceAll("[^a-zA-Z0-9._-]", "_"))
                .orElse("file");

        String extension = getExtension(originalFileName);
        String storedFileName = UUID.randomUUID() + "." + extension;

        try {
            Path uploadPath = Paths.get(uploadDir)
                    .resolve(jobApplication.getId())
                    .toAbsolutePath()
                    .normalize();
            Files.createDirectories(uploadPath);

            Path targetPath = uploadPath.resolve(storedFileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            ApplicationFile appFile = ApplicationFile.builder()
                    .jobApplication(jobApplication)
                    .originalFileName(originalFileName)
                    .storedFileName(storedFileName)
                    .filePath(uploadPath.toString())
                    .mimeType(file.getContentType())
                    .fileSize(file.getSize())
                    .fileType(fileType)
                    .build();

            return fileRepository.save(appFile);

        } catch (IOException ex) {
            throw new BusinessException("Impossible de stocker le fichier: " + ex.getMessage());
        }
    }

    @Override
    public Resource loadFileAsResource(String fileId) {
        ApplicationFile appFile = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("Fichier introuvable: " + fileId));

        try {
            Path filePath = Paths.get(appFile.getFilePath())
                    .resolve(appFile.getStoredFileName())
                    .normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new ResourceNotFoundException("Fichier inaccessible sur le disque: " + fileId);

        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("Chemin de fichier invalide: " + ex.getMessage());
        }
    }

    @Override
    public void deleteFile(String fileId) {
        ApplicationFile appFile = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("Fichier introuvable: " + fileId));
        try {
            Path filePath = Paths.get(appFile.getFilePath())
                    .resolve(appFile.getStoredFileName())
                    .normalize();
            Files.deleteIfExists(filePath);
            fileRepository.delete(appFile);
        } catch (IOException ex) {
            throw new BusinessException("Impossible de supprimer le fichier: " + ex.getMessage());
        }
    }

    private String getExtension(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        }
        return "bin";
    }
}
