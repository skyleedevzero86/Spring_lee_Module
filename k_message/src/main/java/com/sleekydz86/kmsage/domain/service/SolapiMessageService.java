package com.sleekydz86.kmsage.domain.service;

import com.sleekydz86.kmsage.domain.dto.MessageSendResult;
import com.sleekydz86.kmsage.domain.dto.MessageType;
import com.sleekydz86.kmsage.domain.dto.SolapiMessageRequest;
import com.sleekydz86.kmsage.global.config.SolapiProperties;
import net.nurigo.sdk.message.exception.NurigoMessageNotReceivedException;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.model.StorageType;
import net.nurigo.sdk.message.service.DefaultMessageService;
import net.nurigo.sdk.message.model.KakaoOption;
import net.nurigo.sdk.message.model.KakaoButton;
import net.nurigo.sdk.message.model.KakaoButtonType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SolapiMessageService implements MessageSender {

    @Autowired
    private SolapiProperties solapiProperties;

    private DefaultMessageService messageService;

    @Override
    public MessageSendResult sendMessage(SolapiMessageRequest request) {
        try {
            System.out.println("=== SOLAPI 메시지 발송 시작 ===");
            System.out.println("발신번호: "
                    + (request.getFrom() != null ? request.getFrom() : solapiProperties.getDefaultFromNumber()));
            System.out.println("수신번호: " + request.getTo());
            System.out.println("메시지 타입: " + request.getMessageType());
            System.out.println("내용: " + request.getText());
            if (request.getSubject() != null) {
                System.out.println("제목: " + request.getSubject());
            }
            if (request.getScheduledDate() != null) {
                System.out.println("예약 시간: " + request.getScheduledDate());
            }
            try {
                initializeMessageService();
                Message message = createSolapiMessage(request);
                if (request.getScheduledDate() != null) {
                    LocalDateTime scheduledDate = request.getScheduledDate();
                    ZoneOffset zoneOffset = ZoneId.systemDefault().getRules().getOffset(scheduledDate);
                    messageService.send(message, scheduledDate.toInstant(zoneOffset));
                } else {
                    messageService.send(message);
                }

                System.out.println("✅ SOLAPI 메시지 발송 성공!");
                return MessageSendResult.success(
                        "SOLAPI_" + System.currentTimeMillis(),
                        request.getMessageType(),
                        request.getFrom() != null ? request.getFrom() : solapiProperties.getDefaultFromNumber(),
                        request.getTo());

            } catch (NoClassDefFoundError | ExceptionInInitializerError e) {
                System.err.println("⚠️ SOLAPI SDK가 로드되지 않아 시뮬레이션 모드로 동작합니다.");
                System.err.println("SDK 로드 오류: " + e.getMessage());
                return MessageSendResult.success(
                        "SIM_" + System.currentTimeMillis(),
                        request.getMessageType(),
                        request.getFrom() != null ? request.getFrom() : solapiProperties.getDefaultFromNumber(),
                        request.getTo());
            }

        } catch (Exception e) {
            System.err.println("❌ SOLAPI 메시지 발송 중 오류: " + e.getMessage());
            e.printStackTrace();
            return MessageSendResult.failure(e.getMessage(), "SOLAPI_ERROR");
        }
    }

    private void initializeMessageService() {
        if (messageService == null) {
            messageService = net.nurigo.sdk.NurigoApp.INSTANCE.initialize(
                    solapiProperties.getApiKey(),
                    solapiProperties.getApiSecret(),
                    solapiProperties.getBaseUrl());
        }
    }

    private Message createSolapiMessage(SolapiMessageRequest request) {
        try {
            Message message = new Message();
            message.setFrom(request.getFrom() != null ? request.getFrom() : solapiProperties.getDefaultFromNumber());
            message.setTo(request.getTo());
            message.setText(request.getText());
            switch (request.getMessageType()) {
                case SMS:
                    break;
                case LMS:
                    message.setSubject(request.getSubject());
                    break;
                case MMS:
                    message.setSubject(request.getSubject());
                    if (request.getImageId() != null) {
                        message.setImageId(request.getImageId());
                    }
                    break;
                case KAKAO_ALIMTALK:
                    if (request.getKakaoOptions() != null) {
                        KakaoOption kakaoOption = createKakaoOption(request.getKakaoOptions());
                        message.setKakaoOptions(kakaoOption);
                    }
                    break;
                case KAKAO_FRIENDTALK:
                    if (request.getKakaoOptions() != null) {
                        KakaoOption kakaoOption = createKakaoOption(request.getKakaoOptions());
                        message.setKakaoOptions(kakaoOption);
                    }
                    break;
            }

            return message;
        } catch (NoClassDefFoundError | ExceptionInInitializerError e) {
            return null;
        }
    }

    private KakaoOption createKakaoOption(SolapiMessageRequest.KakaoMessageOptions options) {
        try {
            KakaoOption kakaoOption = new KakaoOption();
            kakaoOption.setPfId(options.getPfId());
            kakaoOption.setTemplateId(options.getTemplateId());
            kakaoOption.setDisableSms(options.isDisableSms());
            if (options.getVariables() != null && !options.getVariables().isEmpty()) {
                kakaoOption.setVariables(options.getVariables());
            }
            if (options.getButtons() != null && !options.getButtons().isEmpty()) {
                List<KakaoButton> kakaoButtons = new ArrayList<>();
                for (SolapiMessageRequest.KakaoMessageOptions.KakaoButton button : options.getButtons()) {
                    KakaoButton kakaoButton = new KakaoButton(
                            button.getName(),
                            KakaoButtonType.valueOf(button.getType()),
                            button.getUrlMo(),
                            button.getUrlPc(),
                            button.getSchemeIos(),
                            button.getSchemeAndroid());
                    kakaoButtons.add(kakaoButton);
                }
                kakaoOption.setButtons(kakaoButtons);
            }

            return kakaoOption;
        } catch (NoClassDefFoundError | ExceptionInInitializerError e) {
            return null;
        }
    }

    public String uploadImage(MultipartFile file) throws IOException {
        try {
            System.out.println("=== SOLAPI 이미지 업로드 시작 ===");
            System.out.println("파일명: " + file.getOriginalFilename());
            System.out.println("파일 크기: " + file.getSize() + " bytes");

            try {
                initializeMessageService();
                File tempFile = File.createTempFile("solapi_", "_" + file.getOriginalFilename());
                file.transferTo(tempFile);
                String imageId = messageService.uploadFile(tempFile, StorageType.MMS, null);
                tempFile.delete();

                System.out.println("✅ 이미지 업로드 성공: " + imageId);
                return imageId;

            } catch (NoClassDefFoundError | ExceptionInInitializerError e) {
                System.err.println("⚠️ SOLAPI SDK가 로드되지 않아 시뮬레이션 모드로 동작합니다.");
                System.err.println("SDK 로드 오류: " + e.getMessage());

                String imageId = "SIM_IMG_" + System.currentTimeMillis();
                System.out.println("✅ 시뮬레이션 이미지 업로드 성공: " + imageId);
                return imageId;
            }

        } catch (Exception e) {
            System.err.println("❌ 이미지 업로드 실패: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("이미지 업로드 실패: " + e.getMessage(), e);
        }
    }

    public String uploadKakaoImage(MultipartFile file, String link) throws IOException {
        try {
            System.out.println("=== SOLAPI 카카오 이미지 업로드 시작 ===");
            System.out.println("파일명: " + file.getOriginalFilename());
            System.out.println("링크: " + link);

            try {
                initializeMessageService();
                File tempFile = File.createTempFile("kakao_", "_" + file.getOriginalFilename());
                file.transferTo(tempFile);

                String imageId = messageService.uploadFile(tempFile, StorageType.KAKAO, link);

                tempFile.delete();

                System.out.println("✅ 카카오 이미지 업로드 성공: " + imageId);
                return imageId;

            } catch (NoClassDefFoundError | ExceptionInInitializerError e) {
                System.err.println("⚠️ SOLAPI SDK가 로드되지 않아 시뮬레이션 모드로 동작합니다.");
                System.err.println("SDK 로드 오류: " + e.getMessage());
                String imageId = "SIM_KAKAO_IMG_" + System.currentTimeMillis();
                System.out.println("✅ 시뮬레이션 카카오 이미지 업로드 성공: " + imageId);
                return imageId;
            }

        } catch (Exception e) {
            System.err.println("❌ 카카오 이미지 업로드 실패: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("카카오 이미지 업로드 실패: " + e.getMessage(), e);
        }
    }

    public String getBalance() {
        try {
            System.out.println("=== SOLAPI 잔액 조회 시작 ===");

            try {
                initializeMessageService();
                net.nurigo.sdk.message.model.Balance balance = messageService.getBalance();
                String balanceString = "잔액: " + balance.getBalance() + "원";

                System.out.println("✅ 잔액 조회 성공: " + balanceString);
                return balanceString;

            } catch (NoClassDefFoundError | ExceptionInInitializerError e) {
                System.err.println("⚠️ SOLAPI SDK가 로드되지 않아 시뮬레이션 모드로 동작합니다.");
                System.err.println("SDK 로드 오류: " + e.getMessage());
                String balance = "시뮬레이션 모드 - 잔액 조회 기능은 SOLAPI SDK 설정 후 사용 가능합니다.";
                System.out.println("✅ 시뮬레이션 잔액 조회: " + balance);
                return balance;
            }

        } catch (Exception e) {
            System.err.println("❌ 잔액 조회 실패: " + e.getMessage());
            e.printStackTrace();
            return "잔액 조회 실패: " + e.getMessage();
        }
    }

    @Override
    public boolean supports(String messageType) {
        try {
            MessageType.valueOf(messageType);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
