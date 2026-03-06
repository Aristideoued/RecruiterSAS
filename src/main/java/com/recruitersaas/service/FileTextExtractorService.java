package com.recruitersaas.service;

import com.recruitersaas.model.ApplicationFile;

public interface FileTextExtractorService {

    /**
     * Extracts readable text from a stored ApplicationFile (PDF, DOCX, DOC, TXT).
     * Returns null if the file type is not supported or extraction fails.
     */
    String extractText(ApplicationFile file);
}
