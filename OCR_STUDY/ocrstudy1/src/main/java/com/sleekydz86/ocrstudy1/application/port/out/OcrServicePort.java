package com.sleekydz86.ocrstudy1.application.port.out;

import com.sleekydz86.ocrstudy1.doamin.model.OcrResult;
import java.io.InputStream;

public interface OcrServicePort {
    OcrResult extractText(InputStream imageStream, String filename);
}