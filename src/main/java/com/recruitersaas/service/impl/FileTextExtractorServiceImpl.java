package com.recruitersaas.service.impl;

import com.recruitersaas.model.ApplicationFile;
import com.recruitersaas.service.FileTextExtractorService;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j
public class FileTextExtractorServiceImpl implements FileTextExtractorService {

    @Override
    public String extractText(ApplicationFile file) {
        Path path = Paths.get(file.getFilePath()).resolve(file.getStoredFileName()).normalize();
        if (!Files.exists(path)) {
            log.warn("Fichier introuvable sur le disque: {}", path);
            return null;
        }

        String mime = file.getMimeType() != null ? file.getMimeType().toLowerCase() : "";
        String name = file.getStoredFileName().toLowerCase();

        try {
            if (mime.contains("pdf") || name.endsWith(".pdf")) {
                return extractPdf(path);
            } else if (mime.contains("wordprocessingml") || name.endsWith(".docx")) {
                return extractDocx(path);
            } else if (mime.contains("msword") || name.endsWith(".doc")) {
                return extractDoc(path);
            } else if (mime.contains("text/plain") || name.endsWith(".txt")) {
                return Files.readString(path, StandardCharsets.UTF_8);
            } else {
                log.debug("Type de fichier non supporté pour extraction: {} ({})", name, mime);
                return null;
            }
        } catch (Exception e) {
            log.warn("Échec extraction texte du fichier {}: {}", file.getOriginalFileName(), e.getMessage());
            return null;
        }
    }

    private String extractPdf(Path path) throws Exception {
        try (PDDocument doc = Loader.loadPDF(path.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc);
        }
    }

    private String extractDocx(Path path) throws Exception {
        try (FileInputStream fis = new FileInputStream(path.toFile());
             XWPFDocument doc = new XWPFDocument(fis);
             XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
            return extractor.getText();
        }
    }

    private String extractDoc(Path path) throws Exception {
        try (FileInputStream fis = new FileInputStream(path.toFile());
             HWPFDocument doc = new HWPFDocument(fis);
             WordExtractor extractor = new WordExtractor(doc)) {
            return extractor.getText();
        }
    }
}
