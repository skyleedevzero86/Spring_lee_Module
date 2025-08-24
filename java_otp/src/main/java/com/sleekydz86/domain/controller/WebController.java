package com.sleekydz86.domain.controller;

import com.sleekydz86.domain.dto.*;
import com.sleekydz86.domain.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final UserService userService;

    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        if (username != null) {
            model.addAttribute("username", username);
            return "dashboard";
        }
        return "index";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        try {
            LoginRequest request = new LoginRequest();
            request.setUsername(username);
            request.setPassword(password);

            LoginResponse response = userService.login(request);

            if (response.isRequireOtp()) {
                // OTP 입력 페이지로 이동
                session.setAttribute("tempUsername", username);
                session.setAttribute("tempPassword", password);
                return "otp-verify";
            } else {
                // 로그인 성공
                session.setAttribute("username", username);
                return "redirect:/dashboard";
            }
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "login";
        }
    }

    @PostMapping("/otp-verify")
    public String verifyOtp(@RequestParam String otpCode,
                            HttpSession session,
                            Model model) {
        try {
            String username = (String) session.getAttribute("tempUsername");
            String password = (String) session.getAttribute("tempPassword");

            OtpLoginRequest request = new OtpLoginRequest();
            request.setUsername(username);
            request.setPassword(password);
            request.setOtpCode(otpCode);

            LoginResponse response = userService.verifyOtpLogin(request);

            session.removeAttribute("tempUsername");
            session.removeAttribute("tempPassword");
            session.setAttribute("username", username);

            return "redirect:/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "otp-verify";
        }
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           RedirectAttributes redirectAttributes) {
        try {
            RegisterRequest request = new RegisterRequest();
            request.setUsername(username);
            request.setPassword(password);

            userService.register(request);
            redirectAttributes.addFlashAttribute("success", "회원가입이 완료되었습니다. 로그인해주세요.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/login";
        }
        model.addAttribute("username", username);
        return "dashboard";
    }

    @GetMapping("/otp-setup")
    public String otpSetupPage(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/login";
        }

        try {
            OtpSetupResponse response = userService.enableOtp(username);
            model.addAttribute("qrCode", response.getQrCode());
            model.addAttribute("secret", response.getSecret());
            model.addAttribute("username", username);
            return "otp-setup";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "dashboard";
        }
    }

    @PostMapping("/otp-setup-verify")
    public String verifyOtpSetup(@RequestParam String code,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        String username = (String) session.getAttribute("username");
        try {
            OtpVerifyResponse response = userService.verifyOtpSetup(username, code);
            if (response.isSuccess()) {
                redirectAttributes.addFlashAttribute("success", "OTP가 성공적으로 활성화되었습니다!");
            } else {
                redirectAttributes.addFlashAttribute("error", response.getMessage());
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}