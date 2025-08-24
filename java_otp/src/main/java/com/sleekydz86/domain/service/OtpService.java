package com.sleekydz86.domain.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class OtpService {

    @Value("${otp.issuer}")
    private String issuer;

    @Value("${otp.algorithm}")
    private String algorithm;

    @Value("${otp.digits}")
    private int digits;

    @Value("${otp.period}")
    private int period;

    @Value("${otp.window}")
    private int window;

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int SECRET_SIZE = 32;

    public String generateSecretKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[SECRET_SIZE];
        random.nextBytes(bytes);
        Base32 base32 = new Base32();
        return base32.encodeToString(bytes);
    }

    public String generateTOTP(String secret) {
        Base32 base32 = new Base32();
        byte[] bytes = base32.decode(secret);
        String hexKey = Hex.encodeHexString(bytes);

        long time = Instant.now().getEpochSecond() / period;
        String hexTime = Long.toHexString(time);

        while (hexTime.length() < 16) {
            hexTime = "0" + hexTime;
        }

        return generateHOTP(hexKey, hexTime);
    }

    private String generateHOTP(String key, String time) {
        try {
            byte[] msg = hexStringToBytes(time);
            byte[] k = hexStringToBytes(key);

            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec spec = new SecretKeySpec(k, HMAC_ALGORITHM);
            mac.init(spec);
            byte[] hash = mac.doFinal(msg);

            return truncateHash(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("OTP 생성 중 오류 발생", e);
        }
    }

    private String truncateHash(byte[] hash) {
        int offset = hash[hash.length - 1] & 0xf;

        long truncatedHash = 0;
        for (int i = 0; i < 4; ++i) {
            truncatedHash <<= 8;
            truncatedHash |= (hash[offset + i] & 0xff);
        }

        truncatedHash &= 0x7FFFFFFF;
        truncatedHash %= Math.pow(10, digits);

        String result = String.valueOf(truncatedHash);
        while (result.length() < digits) {
            result = "0" + result;
        }

        return result;
    }

    public boolean verifyCode(String secret, String code) {

        long currentTime = Instant.now().getEpochSecond() / period;

        for (int i = -window; i <= window; i++) {
            String validCode = generateTOTPForTime(secret, currentTime + i);
            if (code.equals(validCode)) {
                return true;
            }
        }

        return false;
    }

    private String generateTOTPForTime(String secret, long time) {
        Base32 base32 = new Base32();
        byte[] bytes = base32.decode(secret);
        String hexKey = Hex.encodeHexString(bytes);

        String hexTime = Long.toHexString(time);
        while (hexTime.length() < 16) {
            hexTime = "0" + hexTime;
        }

        return generateHOTP(hexKey, hexTime);
    }

    public String generateQRUrl(String username, String secret) {
        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA256&digits=%d&period=%d",
                issuer, username, secret, issuer, digits, period);
    }

    public String generateQRCodeImage(String username, String secret) {
        String qrUrl = generateQRUrl(username, secret);

        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrUrl, BarcodeFormat.QR_CODE, 200, 200);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            byte[] qrCodeImage = outputStream.toByteArray();
            return Base64.getEncoder().encodeToString(qrCodeImage);
        } catch (WriterException | IOException e) {
            throw new RuntimeException("QR 코드 생성 실패", e);
        }
    }

    private byte[] hexStringToBytes(String hex) {
        byte[] result = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i += 2) {
            result[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
        }
        return result;
    }
}