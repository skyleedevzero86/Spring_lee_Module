package com.sleekydz86.sever.model.presentation.controller;

import com.sleekydz86.sever.model.application.service.UserService;
import com.sleekydz86.sever.model.domain.User;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
            @RequestParam String password,
            RedirectAttributes redirectAttributes) {
        try {
            String encodedPassword = passwordEncoder.encode(password);
            userService.register(username, encodedPassword, "ROLE_USER");
            redirectAttributes.addFlashAttribute("message", "회원가입이 완료되었습니다.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "회원가입에 실패했습니다: " + e.getMessage());
            return "redirect:/users/register";
        }
    }

    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public String profile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userService.getUserByUsername(username);
        model.addAttribute("user", user);
        return "profile";
    }

    @GetMapping("/edit")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public String editForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userService.getUserByUsername(username);
        model.addAttribute("user", user);
        return "edit";
    }

    @PostMapping("/edit")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public String update(@RequestParam(required = false) String password,
            RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User user = userService.getUserByUsername(username);

            String encodedPassword = password != null && !password.isEmpty()
                    ? passwordEncoder.encode(password)
                    : null;

            userService.updateUser(user.getId(), null, encodedPassword, null, null);
            redirectAttributes.addFlashAttribute("message", "정보가 수정되었습니다.");
            return "redirect:/users/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "수정에 실패했습니다: " + e.getMessage());
            return "redirect:/users/edit";
        }
    }

    @PostMapping("/delete")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public String delete(RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User user = userService.getUserByUsername(username);
            userService.deleteUser(user.getId());
            redirectAttributes.addFlashAttribute("message", "탈퇴가 완료되었습니다.");
            return "redirect:/logout";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "탈퇴에 실패했습니다: " + e.getMessage());
            return "redirect:/users/profile";
        }
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public String list(@RequestParam(required = false) String keyword, Model model) {
        List<Map<String, Object>> users;
        if (keyword != null && !keyword.isEmpty()) {
            users = userService.searchUsers(keyword);
            model.addAttribute("keyword", keyword);
        } else {
            users = userService.getUserList();
        }
        model.addAttribute("users", users);
        return "user-list";
    }

    @GetMapping("/detail/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String detail(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id);
        if (user == null) {
            return "redirect:/users/list";
        }
        model.addAttribute("user", user);
        return "user-detail";
    }
}
