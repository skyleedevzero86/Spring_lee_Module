package com.sleekydz86.monitoring.logstack_s3.infrastructure.web;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Component;

import com.sleekydz86.monitoring.logstack_s3.domain.exception.FileStorageException;
import com.sleekydz86.monitoring.logstack_s3.infrastructure.message.InfrastructureMessages;

@Component
public class SiteFaviconRenderer {

    private static final int SIZE = 32;
    private static final Color BRAND = new Color(0x25, 0x63, 0xeb);
    private static final Color STACK = Color.WHITE;

    public byte[] renderPng() {
        BufferedImage image = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(BRAND);
        graphics.fillRoundRect(0, 0, SIZE, SIZE, 8, 8);
        graphics.setColor(STACK);
        int left = 7;
        int barHeight = 4;
        int gap = 3;
        int[] widths = {18, 14, 10};
        for (int i = 0; i < widths.length; i++) {
            int y = 8 + i * (barHeight + gap);
            graphics.fillRoundRect(left, y, widths[i], barHeight, 2, 2);
        }
        graphics.dispose();
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(image, "png", output);
            return output.toByteArray();
        } catch (IOException e) {
            throw new FileStorageException(InfrastructureMessages.FAVICON_CREATE_FAILED, e);
        }
    }
}
