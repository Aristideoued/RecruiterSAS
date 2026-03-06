package com.recruitersaas.controller;

import com.recruitersaas.exception.ResourceNotFoundException;
import com.recruitersaas.model.ApplicationFile;
import com.recruitersaas.repository.ApplicationFileRepository;
import com.recruitersaas.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;
    private final ApplicationFileRepository applicationFileRepository;

    /**
     * Télécharger / visualiser un fichier (CV, LM, etc.)
     * Accessible aux recruteurs et super admin authentifiés.
     */
    @GetMapping("/{fileId}")
    @PreAuthorize("hasAnyRole('RECRUITER', 'SUPER_ADMIN')")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileId) {
        ApplicationFile appFile = applicationFileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("Fichier introuvable: " + fileId));

        Resource resource = fileStorageService.loadFileAsResource(fileId);

        String contentType = appFile.getMimeType() != null
                ? appFile.getMimeType()
                : "application/octet-stream";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + appFile.getOriginalFileName() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{fileId}")
    @PreAuthorize("hasAnyRole('RECRUITER', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteFile(@PathVariable String fileId) {
        fileStorageService.deleteFile(fileId);
        return ResponseEntity.noContent().build();
    }
}
