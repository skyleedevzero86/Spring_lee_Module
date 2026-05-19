package com.sleekydz86.monitoring.logstack_s3.infrastructure.thumbnail;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.sleekydz86.monitoring.logstack_s3.application.port.ThumbnailPort;
import com.sleekydz86.monitoring.logstack_s3.global.common.message.KoreanMessages;
import com.sleekydz86.monitoring.logstack_s3.domain.exception.FileStorageException;

@Component
public class PdfBoxThumbnailAdapter implements ThumbnailPort {

    private static final int THUMB_WIDTH = 200;
    private static final int THUMB_HEIGHT = 200;
    private static final String THUMB_CONTENT_TYPE = "image/jpeg";

    @Override
    public Optional<byte[]> generate(MultipartFile file, String contentType) {
        try {
            if (contentType != null && contentType.startsWith("image/")) {
                return Optional.ofNullable(scaleImage(file.getInputStream()));
            }
            if ("application/pdf".equals(contentType)) {
                return Optional.of(scalePdf(file.getBytes()));
            }
            return Optional.empty();
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    @Override
    public byte[] placeholder() {
        try {
            BufferedImage image = new BufferedImage(THUMB_WIDTH, THUMB_HEIGHT, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            graphics.setColor(java.awt.Color.LIGHT_GRAY);
            graphics.fillRect(0, 0, THUMB_WIDTH, THUMB_HEIGHT);
            graphics.setColor(java.awt.Color.DARK_GRAY);
            graphics.drawString("FILE", 80, 105);
            graphics.dispose();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", output);
            return output.toByteArray();
        } catch (IOException e) {
            throw new FileStorageException(KoreanMessages.THUMBNAIL_CREATE_FAILED, e);
        }
    }

    @Override
    public String contentType() {
        return THUMB_CONTENT_TYPE;
    }

    @Override
    public boolean supports(String contentType) {
        return contentType != null
                && (contentType.startsWith("image/") || "application/pdf".equals(contentType));
    }

    private byte[] scaleImage(InputStream inputStream) throws IOException {
        BufferedImage source = ImageIO.read(inputStream);
        if (source == null) {
            return null;
        }
        return scaleToJpeg(source);
    }

    private byte[] scalePdf(byte[] pdfBytes) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            if (document.getNumberOfPages() < 1) {
                return null;
            }
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage page = renderer.renderImageWithDPI(0, 96);
            return scaleToJpeg(page);
        }
    }

    private byte[] scaleToJpeg(BufferedImage source) throws IOException {
        int width = source.getWidth();
        int height = source.getHeight();
        double ratio = Math.min((double) THUMB_WIDTH / width, (double) THUMB_HEIGHT / height);
        int targetW = Math.max(1, (int) (width * ratio));
        int targetH = Math.max(1, (int) (height * ratio));
        Image scaled = source.getScaledInstance(targetW, targetH, Image.SCALE_SMOOTH);
        BufferedImage thumbnail = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = thumbnail.createGraphics();
        graphics.drawImage(scaled, 0, 0, null);
        graphics.dispose();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(thumbnail, "jpg", output);
        return output.toByteArray();
    }
}
