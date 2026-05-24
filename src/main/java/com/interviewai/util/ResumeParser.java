package com.interviewai.util;

import org.springframework.web.multipart.MultipartFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import net.sourceforge.tess4j.Tesseract;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ResumeParser {

    public static String extractText(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件为空，请重新选择文件上传。");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IllegalArgumentException("无法获取合法的文件后缀名。");
        }

        String suffix = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();

        switch (suffix) {
            case ".txt":
            case ".md":
                return new String(file.getBytes(), StandardCharsets.UTF_8);
            case ".pdf":
                return extractTextFromPdf(file.getInputStream());
            case ".docx":
                return extractTextFromDocx(file.getInputStream());
            case ".doc":
                throw new IllegalArgumentException("暂不支持旧版 Word (.doc) 格式，请另存为 .docx 后重新上传。");
            default:
                throw new IllegalArgumentException("不支持的文件格式 (" + suffix + ")，目前仅支持 .pdf, .docx, .txt, .md 文件。");
        }
    }

    private static String extractTextFromPdf(InputStream inputStream) throws IOException {
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            if (text == null || text.trim().isEmpty()) {
                // PDF is empty or scanned image, fallback to OCR
                text = extractTextFromPdfWithOcr(document);
                if (text == null || text.trim().isEmpty()) {
                    throw new IllegalArgumentException("PDF 文件内容为空，且扫描版OCR识别失败，请直接复制您的简历内容粘贴到下方文本框中！");
                }
            }
            return text;
        }
    }

    private static String extractTextFromPdfWithOcr(PDDocument document) {
        try {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            StringBuilder out = new StringBuilder();
            Tesseract tesseract = new Tesseract();
            // Assuming tessdata is placed in the project root directory
            tesseract.setDatapath("tessdata");
            tesseract.setLanguage("chi_sim+eng");

            for (int page = 0; page < document.getNumberOfPages(); page++) {
                BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);
                String result = tesseract.doOCR(bim);
                out.append(result).append("\n");
            }
            return out.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String extractTextFromDocx(InputStream inputStream) throws IOException {
        try (XWPFDocument document = new XWPFDocument(inputStream);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            String text = extractor.getText();
            if (text == null || text.trim().isEmpty()) {
                throw new IOException("Word 文件内容为空。");
            }
            return text;
        }
    }
}
