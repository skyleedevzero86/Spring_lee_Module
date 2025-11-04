package com.sleekydz86.ocrstudy1.application.port.out;

import com.sleekydz86.ocrstudy1.doamin.model.FaceRecognition;
import java.io.InputStream;

public interface FaceRecognitionPort {
    FaceRecognition detectFaces(InputStream imageStream, String filename);
    Double compareFaces(String encoding1, String encoding2);
}