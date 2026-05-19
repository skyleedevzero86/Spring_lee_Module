package com.sleekydz86.monitoring.logstack_s3.interfaces.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sleekydz86.monitoring.logstack_s3.global.common.message.KoreanMessages;
import com.sleekydz86.monitoring.logstack_s3.application.query.SearchFilesQuery;
import com.sleekydz86.monitoring.logstack_s3.application.query.UploadFileCommand;
import com.sleekydz86.monitoring.logstack_s3.application.usecase.GetFileDetailUseCase;
import com.sleekydz86.monitoring.logstack_s3.application.usecase.SearchFilesUseCase;
import com.sleekydz86.monitoring.logstack_s3.application.usecase.UploadFileUseCase;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class FileViewController {

    private final SearchFilesUseCase searchFilesUseCase;
    private final UploadFileUseCase uploadFileUseCase;
    private final GetFileDetailUseCase getFileDetailUseCase;

    @GetMapping("/")
    public String list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String keyword,
            Model model
    ) {
        var result = searchFilesUseCase.apply(new SearchFilesQuery(keyword, page, size));
        model.addAttribute("page", result);
        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        return "list";
    }

    @GetMapping("/upload")
    public String uploadForm() {
        return "upload";
    }

    @PostMapping("/upload")
    public String upload(
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes
    ) {
        var view = uploadFileUseCase.apply(new UploadFileCommand(file));
        redirectAttributes.addFlashAttribute("message", KoreanMessages.UPLOAD_COMPLETE);
        return "redirect:/files/" + view.id();
    }

    @GetMapping("/files/{id}")
    public String detail(@PathVariable String id, Model model) {
        model.addAttribute("file", getFileDetailUseCase.apply(id));
        return "detail";
    }
}
