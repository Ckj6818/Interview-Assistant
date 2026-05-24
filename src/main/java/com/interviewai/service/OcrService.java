package com.interviewai.service;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class OcrService {

    private final Tesseract tesseract;

    public OcrService() {
        tesseract = new Tesseract();
        // 设置 tessdata 目录
        tesseract.setDatapath("tessdata");
        // 设置语言为英语和简体中文
        tesseract.setLanguage("eng+chi_sim");
    }

    /**
     * 提取图片中的文字
     *
     * @param imageFile 图片文件
     * @return 提取出的文字
     */
    public String extractTextFromImage(File imageFile) {
        try {
            return tesseract.doOCR(imageFile);
        } catch (TesseractException e) {
            e.printStackTrace();
            return "OCR 识别失败：" + e.getMessage();
        }
    }
}
