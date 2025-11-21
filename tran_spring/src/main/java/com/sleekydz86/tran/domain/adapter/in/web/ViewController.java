package com.sleekydz86.tran.domain.adapter.in.web;

import com.sleekydz86.tran.domain.application.dto.TranslationRequestDto;
import com.sleekydz86.tran.domain.application.dto.TranslationResponseDto;
import com.sleekydz86.tran.domain.application.usecase.TranslateTextUseCase;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

@Controller
public class ViewController {
    private static final Logger logger = LoggerFactory.getLogger(ViewController.class);

    private final TranslateTextUseCase translateTextUseCase;

    public ViewController(TranslateTextUseCase translateTextUseCase) {
        this.translateTextUseCase = translateTextUseCase;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("translationRequest", new TranslationRequestDto());
        model.addAttribute("languages", getSupportedLanguages());
        model.addAttribute("serviceAvailable", translateTextUseCase.isServiceAvailable());
        model.addAttribute("usageInfo", translateTextUseCase.getServiceUsageInfo());
        return "index";
    }

    @PostMapping("/translate")
    public String translate(
            @Valid @ModelAttribute("translationRequest") TranslationRequestDto requestDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {

        if (bindingResult.hasErrors()) {
            logger.warn("번역 요청 검증 실패: {}", bindingResult.getAllErrors());
            model.addAttribute("languages", getSupportedLanguages());
            model.addAttribute("serviceAvailable", translateTextUseCase.isServiceAvailable());
            model.addAttribute("usageInfo", translateTextUseCase.getServiceUsageInfo());
            return "index";
        }

        try {
            logger.info("번역 요청: {}", requestDto);

            TranslationResponseDto response = translateTextUseCase.translate(requestDto);

            model.addAttribute("translationResponse", response);
            model.addAttribute("languages", getSupportedLanguages());
            model.addAttribute("serviceAvailable", translateTextUseCase.isServiceAvailable());
            model.addAttribute("usageInfo", translateTextUseCase.getServiceUsageInfo());

            logger.info("번역 완료: {} -> {}",
                    response.getDetectedSourceLanguage(),
                    response.getTargetLanguage());

            return "index";

        } catch (Exception e) {
            logger.error("번역 처리 중 오류 발생: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("languages", getSupportedLanguages());
            model.addAttribute("serviceAvailable", translateTextUseCase.isServiceAvailable());
            model.addAttribute("usageInfo", translateTextUseCase.getServiceUsageInfo());
            return "index";
        }
    }

    private List<LanguageOption> getSupportedLanguages() {
        return Arrays.asList(
                new LanguageOption("auto", "자동 감지 (원본 언어)"),
                new LanguageOption("ko", "한국어 (Korean)"),
                new LanguageOption("en-US", "영어 (English)"),
                new LanguageOption("ja", "일본어 (Japanese)"),
                new LanguageOption("zh", "중국어 간체 (Chinese Simplified)"),
                new LanguageOption("es", "스페인어 (Spanish)"),
                new LanguageOption("fr", "프랑스어 (French)"),
                new LanguageOption("de", "독일어 (German)"),
                new LanguageOption("it", "이탈리아어 (Italian)"),
                new LanguageOption("pt-PT", "포르투갈어 (Portuguese)"),
                new LanguageOption("ru", "러시아어 (Russian)"),
                new LanguageOption("ar", "아랍어 (Arabic)"),
                new LanguageOption("hi", "힌디어 (Hindi)"),
                new LanguageOption("vi", "베트남어 (Vietnamese)")
        );
    }
    public static class LanguageOption {
        private final String code;
        private final String name;

        public LanguageOption(String code, String name) {
            this.code = code;
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }
    }
}