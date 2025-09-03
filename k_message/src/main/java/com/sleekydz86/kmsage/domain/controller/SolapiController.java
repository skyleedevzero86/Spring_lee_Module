package com.sleekydz86.kmsage.domain.controller;

import com.sleekydz86.kmsage.domain.dto.*;
import com.sleekydz86.kmsage.domain.service.KakaoService;
import com.sleekydz86.kmsage.domain.service.UnifiedMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/solapi")
public class SolapiController {

    @Autowired
    private UnifiedMessageService unifiedMessageService;
    @Autowired
    private KakaoService kakaoService;

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        String accessToken = (String) session.getAttribute("accessToken");
        boolean isLoggedIn = accessToken != null;

        model.addAttribute("isLoggedIn", isLoggedIn);
        model.addAttribute("loginUrl", kakaoService.getKakaoLoginUrl());

        if (isLoggedIn) {
            Map<String, Object> userInfo = kakaoService.getUserInfo(accessToken);
            if (userInfo != null) {
                model.addAttribute("userInfo", userInfo);
            }
            try {
                String balance = unifiedMessageService.getBalance();
                model.addAttribute("balance", balance);
            } catch (Exception e) {
                model.addAttribute("balanceError", "잔액 조회 실패: " + e.getMessage());
            }
        }

        return "solapi/index";
    }

    @GetMapping("/sms")
    public String smsPage(HttpSession session, Model model) {
        String accessToken = (String) session.getAttribute("accessToken");
        if (accessToken == null) {
            return "redirect:/";
        }
        return "solapi/sms";
    }

    @GetMapping("/lms")
    public String lmsPage(HttpSession session, Model model) {
        String accessToken = (String) session.getAttribute("accessToken");
        if (accessToken == null) {
            return "redirect:/";
        }
        return "solapi/lms";
    }

    @GetMapping("/mms")
    public String mmsPage(HttpSession session, Model model) {
        String accessToken = (String) session.getAttribute("accessToken");
        if (accessToken == null) {
            return "redirect:/";
        }
        return "solapi/mms";
    }

    @GetMapping("/kakao")
    public String kakaoPage(HttpSession session, Model model) {
        String accessToken = (String) session.getAttribute("accessToken");
        if (accessToken == null) {
            return "redirect:/";
        }
        return "solapi/kakao";
    }

    @PostMapping("/send/sms")
    public String sendSms(@RequestParam("to") String to,
            @RequestParam("text") String text,
            @RequestParam(value = "scheduledDate", required = false) String scheduledDate,
            HttpSession session,
            Model model) {

        String accessToken = (String) session.getAttribute("accessToken");
        if (accessToken == null) {
            return "redirect:/";
        }

        SolapiMessageRequest request = new SolapiMessageRequest();
        request.setTo(to);
        request.setText(text);
        request.setMessageType(MessageType.SMS);

        if (scheduledDate != null && !scheduledDate.trim().isEmpty()) {
            try {
                LocalDateTime dateTime = LocalDateTime.parse(scheduledDate,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
                request.setScheduledDate(dateTime);
            } catch (Exception e) {
                model.addAttribute("success", false);
                model.addAttribute("message", "예약 시간 형식이 올바르지 않습니다.");
                return "result";
            }
        }

        MessageSendResult result = unifiedMessageService.sendSms(request);
        model.addAttribute("success", result.isSuccess());
        model.addAttribute("message", result.getMessage());
        model.addAttribute("messageId", result.getMessageId());

        return "result";
    }

    @PostMapping("/send/lms")
    public String sendLms(@RequestParam("to") String to,
            @RequestParam("subject") String subject,
            @RequestParam("text") String text,
            @RequestParam(value = "scheduledDate", required = false) String scheduledDate,
            HttpSession session,
            Model model) {

        String accessToken = (String) session.getAttribute("accessToken");
        if (accessToken == null) {
            return "redirect:/";
        }

        SolapiMessageRequest request = new SolapiMessageRequest();
        request.setTo(to);
        request.setSubject(subject);
        request.setText(text);
        request.setMessageType(MessageType.LMS);

        if (scheduledDate != null && !scheduledDate.trim().isEmpty()) {
            try {
                LocalDateTime dateTime = LocalDateTime.parse(scheduledDate,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
                request.setScheduledDate(dateTime);
            } catch (Exception e) {
                model.addAttribute("success", false);
                model.addAttribute("message", "예약 시간 형식이 올바르지 않습니다.");
                return "result";
            }
        }

        MessageSendResult result = unifiedMessageService.sendLms(request);
        model.addAttribute("success", result.isSuccess());
        model.addAttribute("message", result.getMessage());
        model.addAttribute("messageId", result.getMessageId());

        return "result";
    }

    @PostMapping("/send/mms")
    public String sendMms(@RequestParam("to") String to,
            @RequestParam("subject") String subject,
            @RequestParam("text") String text,
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "scheduledDate", required = false) String scheduledDate,
            HttpSession session,
            Model model) {

        String accessToken = (String) session.getAttribute("accessToken");
        if (accessToken == null) {
            return "redirect:/";
        }

        try {
            String imageId = unifiedMessageService.uploadImage(image);

            SolapiMessageRequest request = new SolapiMessageRequest();
            request.setTo(to);
            request.setSubject(subject);
            request.setText(text);
            request.setImageId(imageId);
            request.setMessageType(MessageType.MMS);

            if (scheduledDate != null && !scheduledDate.trim().isEmpty()) {
                try {
                    LocalDateTime dateTime = LocalDateTime.parse(scheduledDate,
                            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
                    request.setScheduledDate(dateTime);
                } catch (Exception e) {
                    model.addAttribute("success", false);
                    model.addAttribute("message", "예약 시간 형식이 올바르지 않습니다.");
                    return "result";
                }
            }

            MessageSendResult result = unifiedMessageService.sendMms(request);
            model.addAttribute("success", result.isSuccess());
            model.addAttribute("message", result.getMessage());
            model.addAttribute("messageId", result.getMessageId());

        } catch (IOException e) {
            model.addAttribute("success", false);
            model.addAttribute("message", "이미지 업로드 실패: " + e.getMessage());
        }

        return "result";
    }

    @PostMapping("/send/kakao-alimtalk")
    public String sendKakaoAlimtalk(@RequestParam("to") String to,
            @RequestParam("pfId") String pfId,
            @RequestParam("templateId") String templateId,
            @RequestParam(value = "disableSms", required = false) String disableSms,
            @RequestParam(value = "scheduledDate", required = false) String scheduledDate,
            HttpSession session,
            Model model) {

        String accessToken = (String) session.getAttribute("accessToken");
        if (accessToken == null) {
            return "redirect:/";
        }

        SolapiMessageRequest.KakaoMessageOptions kakaoOptions = new SolapiMessageRequest.KakaoMessageOptions();
        kakaoOptions.setPfId(pfId);
        kakaoOptions.setTemplateId(templateId);
        kakaoOptions.setDisableSms("on".equals(disableSms));

        SolapiMessageRequest request = new SolapiMessageRequest();
        request.setTo(to);
        request.setKakaoOptions(kakaoOptions);
        request.setMessageType(MessageType.KAKAO_ALIMTALK);

        if (scheduledDate != null && !scheduledDate.trim().isEmpty()) {
            try {
                LocalDateTime dateTime = LocalDateTime.parse(scheduledDate,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
                request.setScheduledDate(dateTime);
            } catch (Exception e) {
                model.addAttribute("success", false);
                model.addAttribute("message", "예약 시간 형식이 올바르지 않습니다.");
                return "result";
            }
        }

        MessageSendResult result = unifiedMessageService.sendKakaoAlimtalk(request);
        model.addAttribute("success", result.isSuccess());
        model.addAttribute("message", result.getMessage());
        model.addAttribute("messageId", result.getMessageId());

        return "result";
    }

    @PostMapping("/send/kakao-friendtalk")
    public String sendKakaoFriendtalk(@RequestParam("to") String to,
            @RequestParam("pfId") String pfId,
            @RequestParam("text") String text,
            @RequestParam(value = "disableSms", required = false) String disableSms,
            @RequestParam(value = "scheduledDate", required = false) String scheduledDate,
            HttpSession session,
            Model model) {

        String accessToken = (String) session.getAttribute("accessToken");
        if (accessToken == null) {
            return "redirect:/";
        }

        SolapiMessageRequest.KakaoMessageOptions kakaoOptions = new SolapiMessageRequest.KakaoMessageOptions();
        kakaoOptions.setPfId(pfId);
        kakaoOptions.setDisableSms("on".equals(disableSms));

        SolapiMessageRequest request = new SolapiMessageRequest();
        request.setTo(to);
        request.setText(text);
        request.setKakaoOptions(kakaoOptions);
        request.setMessageType(MessageType.KAKAO_FRIENDTALK);

        if (scheduledDate != null && !scheduledDate.trim().isEmpty()) {
            try {
                LocalDateTime dateTime = LocalDateTime.parse(scheduledDate,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
                request.setScheduledDate(dateTime);
            } catch (Exception e) {
                model.addAttribute("success", false);
                model.addAttribute("message", "예약 시간 형식이 올바르지 않습니다.");
                return "result";
            }
        }

        MessageSendResult result = unifiedMessageService.sendKakaoFriendtalk(request);
        model.addAttribute("success", result.isSuccess());
        model.addAttribute("message", result.getMessage());
        model.addAttribute("messageId", result.getMessageId());

        return "result";
    }

    @PostMapping("/upload/image")
    @ResponseBody
    public Map<String, String> uploadImage(@RequestParam("image") MultipartFile image) {
        Map<String, String> result = new HashMap<>();
        try {
            String imageId = unifiedMessageService.uploadImage(image);
            result.put("success", "true");
            result.put("imageId", imageId);
        } catch (IOException e) {
            result.put("success", "false");
            result.put("error", e.getMessage());
        }
        return result;
    }
}