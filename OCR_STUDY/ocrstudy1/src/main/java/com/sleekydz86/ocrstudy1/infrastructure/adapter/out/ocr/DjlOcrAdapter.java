package com.sleekydz86.ocrstudy1.infrastructure.adapter.out.ocr;

import com.sleekydz86.ocrstudy1.application.port.out.OcrServicePort;
import com.sleekydz86.ocrstudy1.doamin.model.OcrResult;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.Word;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class DjlOcrAdapter implements OcrServicePort {

    private final ITesseract tesseract;

    public DjlOcrAdapter() {
        this.tesseract = new Tesseract();
        try {
            String tessDataPath = System.getenv("TESSDATA_PREFIX");
            if (tessDataPath == null || tessDataPath.isEmpty()) {
                String os = System.getProperty("os.name").toLowerCase();
                if (os.contains("win")) {
                    tessDataPath = "C:\\Program Files\\Tesseract-OCR\\tessdata";
                } else if (os.contains("mac")) {
                    tessDataPath = "/usr/local/share/tessdata";
                } else {
                    tessDataPath = "/usr/share/tesseract-ocr/4.00/tessdata";
                }
            }
            tesseract.setDatapath(tessDataPath);
            tesseract.setLanguage("kor+eng");
            tesseract.setPageSegMode(1);
            tesseract.setOcrEngineMode(1);

            log.info("Tesseract OCR initialized with data path: {}", tessDataPath);
        } catch (Exception e) {
            log.warn("Failed to set Tesseract data path, using defaults: {}", e.getMessage());
        }
    }

    @Override
    public OcrResult extractText(InputStream imageStream, String filename) {
        try {
            BufferedImage bufferedImage = ImageIO.read(imageStream);
            if (bufferedImage == null) {
                log.error("Failed to read image from stream: {}", filename);
                return createEmptyResult();
            }

            String extractedText = tesseract.doOCR(bufferedImage);
            if (extractedText == null) {
                extractedText = "";
            } else {
                extractedText = extractedText.trim();
            }

            log.info("OCR extracted text length: {} for file: {}", extractedText.length(), filename);

            List<OcrResult.BoundingBox> boundingBoxes = extractBoundingBoxes(bufferedImage, extractedText);

            double avgConfidence = calculateAverageConfidence(bufferedImage);

            return OcrResult.builder()
                    .text(extractedText)
                    .boundingBoxes(boundingBoxes)
                    .confidence(avgConfidence)
                    .language("kor+eng")
                    .build();

        } catch (TesseractException e) {
            log.error("Tesseract OCR failed for file: {}", filename, e);
            return createEmptyResult();
        } catch (IOException e) {
            log.error("Failed to read image stream: {}", filename, e);
            return createEmptyResult();
        } catch (Exception e) {
            log.error("OCR extraction failed for file: {}", filename, e);
            return createEmptyResult();
        }
    }

    private List<OcrResult.BoundingBox> extractBoundingBoxes(BufferedImage image, String text) {
        List<OcrResult.BoundingBox> boxes = new ArrayList<>();

        try {
            List<Word> words = tesseract.getWords(image, ITesseract.RenderedFormat.TEXT);

            if (words != null && !words.isEmpty()) {
                for (Word word : words) {
                    if (word.getText() != null && !word.getText().trim().isEmpty()) {
                        OcrResult.BoundingBox box = OcrResult.BoundingBox.builder()
                                .x((double) word.getBoundingBox().x)
                                .y((double) word.getBoundingBox().y)
                                .width((double) word.getBoundingBox().width)
                                .height((double) word.getBoundingBox().height)
                                .text(word.getText().trim())
                                .confidence(word.getConfidence() / 100.0)
                                .build();
                        boxes.add(box);
                    }
                }
            } else {
                if (text != null && !text.trim().isEmpty()) {
                    OcrResult.BoundingBox box = OcrResult.BoundingBox.builder()
                            .x(0.0)
                            .y(0.0)
                            .width((double) image.getWidth())
                            .height((double) image.getHeight())
                            .text(text)
                            .confidence(0.75)
                            .build();
                    boxes.add(box);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract bounding boxes, using full image area", e);
            if (text != null && !text.trim().isEmpty()) {
                OcrResult.BoundingBox box = OcrResult.BoundingBox.builder()
                        .x(0.0)
                        .y(0.0)
                        .width((double) image.getWidth())
                        .height((double) image.getHeight())
                        .text(text)
                        .confidence(0.70)
                        .build();
                boxes.add(box);
            }
        }

        return boxes;
    }

    private double calculateAverageConfidence(BufferedImage image) {
        try {
            List<Word> words = tesseract.getWords(image, ITesseract.RenderedFormat.TEXT);
            if (words != null && !words.isEmpty()) {
                double totalConfidence = 0.0;
                int count = 0;
                for (Word word : words) {
                    if (word.getConfidence() > 0) {
                        totalConfidence += word.getConfidence();
                        count++;
                    }
                }
                if (count > 0) {
                    return (totalConfidence / count) / 100.0;
                }
            }
        } catch (Exception e) {
            log.debug("Failed to calculate average confidence", e);
        }
        return 0.75;
    }

    private OcrResult createEmptyResult() {
        return OcrResult.builder()
                .text("")
                .boundingBoxes(new ArrayList<>())
                .confidence(0.0)
                .language("unknown")
                .build();
    }
}
