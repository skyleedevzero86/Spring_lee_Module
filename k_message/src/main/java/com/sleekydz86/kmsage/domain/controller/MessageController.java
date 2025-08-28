package com.sleekydz86.kmsage.domain.controller;

import com.sleekydz86.kmsage.domain.dto.EmailRequest;
import com.sleekydz86.kmsage.domain.dto.MessageTemplate;
import com.sleekydz86.kmsage.domain.service.EmailService;
import com.sleekydz86.kmsage.domain.service.KakaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.Map;

@Controller
public class MessageController {

    @Autowired
    private KakaoService kakaoService;

    @Autowired
    private EmailService emailService;

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        String accessToken = (String) session.getAttribute("accessToken");
        model.addAttribute("isLoggedIn", accessToken != null);
        model.addAttribute("loginUrl", kakaoService.getKakaoLoginUrl());

        if (accessToken != null) {
            Map<String, Object> userInfo = kakaoService.getUserInfo(accessToken);
            if (userInfo != null) {
                model.addAttribute("userInfo", userInfo);
            }
        }

        return "index";
    }

    @GetMapping("/callback")
    public String callback(@RequestParam("code") String authCode, HttpSession session) {
        try {
            String accessToken = kakaoService.getAccessToken(authCode);
            session.setAttribute("accessToken", accessToken);
            return "redirect:/message";
        } catch (Exception e) {
            System.err.println("로그인 실패: " + e.getMessage());
            return "redirect:/?error=login_failed";
        }
    }

    @GetMapping("/message")
    public String messagePage(HttpSession session, Model model) {
        String accessToken = (String) session.getAttribute("accessToken");
        if (accessToken == null) {
            return "redirect:/";
        }

        Map<String, Object> userInfo = kakaoService.getUserInfo(accessToken);
        if (userInfo != null) {
            model.addAttribute("userInfo", userInfo);
        }

        return "message";
    }

    @GetMapping("/email")
    public String emailPage(HttpSession session, Model model) {
        String accessToken = (String) session.getAttribute("accessToken");
        if (accessToken == null) {
            return "redirect:/";
        }

        Map<String, Object> userInfo = kakaoService.getUserInfo(accessToken);
        if (userInfo != null) {
            model.addAttribute("userInfo", userInfo);
        }

        return "email";
    }

    @PostMapping("/send/feed")
    public String sendFeedMessage(@RequestParam("title") String title,
                                  @RequestParam("description") String description,
                                  @RequestParam("imageUrl") String imageUrl,
                                  @RequestParam("webUrl") String webUrl,
                                  @RequestParam(value = "emailNotification", required = false) String emailNotification,
                                  @RequestParam(value = "notificationEmail", required = false) String notificationEmail,
                                  HttpSession session,
                                  Model model) {

        String accessToken = (String) session.getAttribute("accessToken");
        if (accessToken == null) {
            return "redirect:/";
        }

        MessageTemplate template = new MessageTemplate();
        template.setObjectType("feed");

        MessageTemplate.Content content = new MessageTemplate.Content(title, description, imageUrl);
        MessageTemplate.Link link = new MessageTemplate.Link(webUrl, webUrl);
        content.setLink(link);
        template.setContent(content);

        template.setSocial(new MessageTemplate.Social(100, 200));

        MessageTemplate.Button[] buttons = {
                new MessageTemplate.Button("웹으로 보기", link)
        };
        template.setButtons(buttons);

        boolean success = kakaoService.sendMessageToMe(accessToken, template);

        if ("on".equals(emailNotification) && notificationEmail != null && !notificationEmail.trim().isEmpty()) {
            emailService.sendNotificationEmail(notificationEmail, "피드 메시지", success);
        }

        model.addAttribute("success", success);
        model.addAttribute("message", success ? "메시지를 성공적으로 보냈습니다!" : "메시지 보내기에 실패했습니다.");

        return "result";
    }

    @PostMapping("/send/scrap")
    public String sendScrapMessage(@RequestParam("url") String url,
                                   @RequestParam(value = "emailNotification", required = false) String emailNotification,
                                   @RequestParam(value = "notificationEmail", required = false) String notificationEmail,
                                   HttpSession session,
                                   Model model) {

        String accessToken = (String) session.getAttribute("accessToken");
        if (accessToken == null) {
            return "redirect:/";
        }

        if (url == null || url.trim().isEmpty()) {
            model.addAttribute("success", false);
            model.addAttribute("message", "URL을 입력해주세요.");
            return "result";
        }

        String domain = extractDomain(url);
        if (!isValidDomain(domain)) {
            model.addAttribute("success", false);
            model.addAttribute("message", "허용되지 않은 도메인입니다: " + domain +
                    "\n카카오 개발자 콘솔에서 등록된 도메인만 사용 가능합니다.");
            return "result";
        }

        boolean success = kakaoService.sendScrapMessage(accessToken, url);

        if ("on".equals(emailNotification) && notificationEmail != null && !notificationEmail.trim().isEmpty()) {
            emailService.sendNotificationEmail(notificationEmail, "스크랩 메시지", success);
        }

        model.addAttribute("success", success);
        if (success) {
            model.addAttribute("message", "스크랩 메시지를 성공적으로 보냈습니다!");
        } else {
            model.addAttribute("message", "스크랩 메시지 보내기에 실패했습니다. 허용된 도메인을 사용하세요.");
        }

        return "result";
    }

    private boolean isValidDomain(String domain) {
        if (domain == null || domain.trim().isEmpty()) {
            return false;
        }

        String[] allowedDomains = {
                "developers.kakao.com",
                "www.daum.net",
                "daum.net",
                "localhost",
                "127.0.0.1"
        };

        for (String allowedDomain : allowedDomains) {
            if (domain.equals(allowedDomain) || domain.endsWith("." + allowedDomain)) {
                return true;
            }
        }

        return false;
    }

    private String extractDomain(String url) {
        try {
            if (url == null || url.trim().isEmpty()) {
                return "";
            }

            String cleanUrl = url.trim();

            if (cleanUrl.startsWith("http://")) {
                cleanUrl = cleanUrl.substring(7);
            } else if (cleanUrl.startsWith("https://")) {
                cleanUrl = cleanUrl.substring(8);
            }

            int portIndex = cleanUrl.indexOf(':');
            if (portIndex != -1) {
                cleanUrl = cleanUrl.substring(0, portIndex);
            }

            int slashIndex = cleanUrl.indexOf('/');
            if (slashIndex != -1) {
                cleanUrl = cleanUrl.substring(0, slashIndex);
            }

            int queryIndex = cleanUrl.indexOf('?');
            if (queryIndex != -1) {
                cleanUrl = cleanUrl.substring(0, queryIndex);
            }

            return cleanUrl.toLowerCase();
        } catch (Exception e) {
            return url;
        }
    }

    @PostMapping("/send/email")
    public String sendEmail(@RequestParam("to") String to,
                            @RequestParam(value = "cc", required = false) String cc,
                            @RequestParam("subject") String subject,
                            @RequestParam("content") String content,
                            @RequestParam(value = "isHtml", required = false) String isHtml,
                            HttpSession session,
                            Model model) {

        String accessToken = (String) session.getAttribute("accessToken");
        if (accessToken == null) {
            return "redirect:/";
        }

        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setTo(to);
        emailRequest.setCc(cc);
        emailRequest.setSubject(subject);
        emailRequest.setContent(content);
        emailRequest.setHtml("on".equals(isHtml));

        boolean success;
        if (emailRequest.isHtml()) {
            success = emailService.sendHtmlEmail(emailRequest);
        } else {
            success = emailService.sendSimpleEmail(emailRequest);
        }

        model.addAttribute("success", success);
        model.addAttribute("message", success ? "이메일을 성공적으로 보냈습니다!" : "이메일 보내기에 실패했습니다.");

        return "result";
    }

    @PostMapping("/send/both")
    public String sendBoth(@RequestParam("title") String title,
                           @RequestParam("description") String description,
                           @RequestParam("imageUrl") String imageUrl,
                           @RequestParam("webUrl") String webUrl,
                           @RequestParam("emailTo") String emailTo,
                           @RequestParam("emailSubject") String emailSubject,
                           @RequestParam("emailContent") String emailContent,
                           HttpSession session,
                           Model model) {

        String accessToken = (String) session.getAttribute("accessToken");
        if (accessToken == null) {
            return "redirect:/";
        }

        MessageTemplate template = new MessageTemplate();
        template.setObjectType("feed");

        MessageTemplate.Content content = new MessageTemplate.Content(title, description, imageUrl);
        MessageTemplate.Link link = new MessageTemplate.Link(webUrl, webUrl);
        content.setLink(link);
        template.setContent(content);

        boolean kakaoSuccess = kakaoService.sendMessageToMe(accessToken, template);

        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setTo(emailTo);
        emailRequest.setSubject(emailSubject);
        emailRequest.setContent(emailContent);
        emailRequest.setHtml(false);

        boolean emailSuccess = emailService.sendSimpleEmail(emailRequest);

        boolean overallSuccess = kakaoSuccess && emailSuccess;
        String message = "";

        if (overallSuccess) {
            message = "카카오 메시지와 이메일 모두 성공적으로 보냈습니다!";
        } else if (kakaoSuccess) {
            message = "카카오 메시지는 성공했지만 이메일 발송에 실패했습니다.";
        } else if (emailSuccess) {
            message = "이메일은 성공했지만 카카오 메시지 발송에 실패했습니다.";
        } else {
            message = "카카오 메시지와 이메일 발송 모두 실패했습니다.";
        }

        model.addAttribute("success", overallSuccess);
        model.addAttribute("message", message);
        model.addAttribute("kakaoResult", kakaoSuccess ? "성공" : "실패");
        model.addAttribute("emailResult", emailSuccess ? "성공" : "실패");

        return "result";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/test/email")
    public String testEmail(HttpSession session, Model model) {
        String accessToken = (String) session.getAttribute("accessToken");
        if (accessToken == null) {
            return "redirect:/";
        }

        EmailRequest testRequest = new EmailRequest();
        testRequest.setTo("sleekydz86@naver.com");
        testRequest.setSubject("[테스트] 이메일 설정 확인");
        testRequest.setContent("이메일 설정이 정상적으로 작동하는지 테스트합니다.");
        testRequest.setHtml(false);

        boolean success = emailService.sendSimpleEmail(testRequest);

        model.addAttribute("success", success);
        model.addAttribute("message", success ? "테스트 이메일 발송 성공!" : "테스트 이메일 발송 실패");

        return "result";
    }
}