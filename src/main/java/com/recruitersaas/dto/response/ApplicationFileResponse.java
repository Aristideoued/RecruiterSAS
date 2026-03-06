package com.recruitersaas.dto.response;

import com.recruitersaas.model.enums.FileType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ApplicationFileResponse {

    private String id;
    private String originalFileName;
    private String mimeType;
    private Long fileSize;
    private FileType fileType;
    private String downloadUrl;
    private LocalDateTime uploadedAt;
}
