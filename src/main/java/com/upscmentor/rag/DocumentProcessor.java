package com.upscmentor.rag;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DocumentProcessor {

    private static final Logger logger = LoggerFactory.getLogger(DocumentProcessor.class);
    private static final Pattern PAGE_PATTERN = Pattern.compile("\\[PAGE (\\d+)\\]");

    private final ChunkingConfig chunkingConfig;

    @Value("${rag.ocr.enabled:true}")
    private boolean ocrEnabled;

    @Value("${rag.ocr.language:eng}")
    private String ocrLanguage;

    public DocumentProcessor(ChunkingConfig chunkingConfig) {
        this.chunkingConfig = chunkingConfig;
    }

    public String extractText(File file, String fileType) throws Exception {
        return switch (fileType.toUpperCase()) {
            case "PDF" -> extractPdfText(file);
            case "TEXT" -> extractTextFile(file);
            case "IMAGE" -> extractImageText(file);
            default -> throw new IllegalArgumentException("Unsupported file type: " + fileType);
        };
    }

    private String extractPdfText(File file) throws Exception {
        try (var pdf = Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            StringBuilder result = new StringBuilder();
            int pageCount = pdf.getNumberOfPages();

            for (int i = 1; i <= pageCount; i++) {
                stripper.setStartPage(i);
                stripper.setEndPage(i);
                String pageText = stripper.getText(pdf);
                if (!pageText.isBlank()) {
                    result.append("[PAGE ").append(i).append("]\n").append(pageText).append("\n\n");
                }
            }
            logger.info("Extracted text from PDF: {} pages, {} chars", pageCount, result.length());
            return result.toString();
        }
    }

    private String extractTextFile(File file) throws IOException {
        String content = new String(Files.readAllBytes(file.toPath()));
        logger.info("Read text file: {} chars", content.length());
        return content;
    }

    private String extractImageText(File file) throws Exception {
        if (!ocrEnabled) {
            logger.warn("OCR is disabled. Cannot extract text from image: {}", file.getName());
            return "";
        }

        BufferedImage image = ImageIO.read(file);
        if (image == null) {
            throw new IOException("Could not read image: " + file.getName());
        }

        Tesseract tesseract = new Tesseract();
        tesseract.setLanguage(ocrLanguage);

        String text = tesseract.doOCR(image);
        logger.info("OCR extracted from image: {} chars", text.length());
        return text;
    }

    public List<Chunk> chunkText(String fullText, String sourceFileName) {
        List<Chunk> chunks = new ArrayList<>();
        int chunkSize = chunkingConfig.getSize();
        int overlapSize = chunkingConfig.getOverlap();

        String[] paragraphs = fullText.split("\n\n+");

        StringBuilder currentChunk = new StringBuilder();
        int currentChunkWords = 0;
        int chunkIndex = 0;
        String currentPage = "1";

        for (String paragraph : paragraphs) {
            String trimmed = paragraph.trim();
            if (trimmed.isEmpty()) continue;

            Matcher pageMatch = PAGE_PATTERN.matcher(trimmed);
            if (pageMatch.find()) {
                currentPage = pageMatch.group(1);
            }

            int wordCount = trimmed.split("\\s+").length;

            if (currentChunkWords + wordCount > chunkSize && currentChunk.length() > 0) {
                String content = currentChunk.toString().trim();
                if (content.length() > 20) {
                    chunks.add(new Chunk(chunkIndex, content, sourceFileName, currentPage));
                    chunkIndex++;
                }

                String[] words = currentChunk.toString().split("\\s+");
                int keepFrom = Math.max(0, words.length - overlapSize);
                currentChunk = new StringBuilder();
                for (int i = keepFrom; i < words.length; i++) {
                    currentChunk.append(words[i]).append(" ");
                }
                currentChunkWords = words.length - keepFrom;
            }

            currentChunk.append(trimmed).append("\n\n");
            currentChunkWords += wordCount;
        }

        String content = currentChunk.toString().trim();
        if (content.length() > 20) {
            chunks.add(new Chunk(chunkIndex, content, sourceFileName, currentPage));
        }

        logger.info("Created {} chunks from {} ({} chars)", chunks.size(), sourceFileName, fullText.length());
        return chunks;
    }

    public static class Chunk {
        public final int index;
        public final String content;
        public final String source;
        public final String page;

        public Chunk(int index, String content, String source, String page) {
            this.index = index;
            this.content = content;
            this.source = source;
            this.page = page;
        }
    }
}
