package com.sleekydz86.ocrstudy1.doamin.service;

public interface EncryptionService {
    String encrypt(String plainText);
    String decrypt(String encryptedText);
    byte[] encryptBytes(byte[] data);
    byte[] decryptBytes(byte[] encryptedData);
}