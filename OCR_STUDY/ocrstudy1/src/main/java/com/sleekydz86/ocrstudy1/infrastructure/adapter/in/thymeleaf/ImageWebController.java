package com.sleekydz86.ocrstudy1.infrastructure.adapter.in.thymeleaf;

import com.sleekydz86.ocrstudy1.application.port.in.ImageCompareUseCase;
import com.sleekydz86.ocrstudy1.application.port.in.ImageUploadUseCase;
import com.sleekydz86.ocrstudy1.doamin.model.DocumentType;
import com.sleekydz86.ocrstudy1.doamin.model.Image;
import com.sleekydz86.ocrstudy1.doamin.model.ImageComparison;
import com.sleekydz86.ocrstudy1.doamin.repository.ImageRepository;
import com.sleekydz86.ocrstudy1.doamin.service.EncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class ImageWebController {

    private final ImageUploadUseCase imageUploadUseCase;
    private final ImageCompareUseCase imageCompareUseCase;
    private final ImageRepository imageRepository;
    private final EncryptionService encryptionService;

    @GetMapping
    public String index(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "documentType", required = false) DocumentType documentType,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Image> imagePage;

        if (documentType != null) {
            imagePage = imageRepository.findByDocumentType(documentType, pageable);
        } else {
            imagePage = imageRepository.findAll(pageable);
        }

        model.addAttribute("images", imagePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", imagePage.getTotalPages());
        model.addAttribute("totalItems", imagePage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("documentTypes", DocumentType.values());
        model.addAttribute("selectedDocumentType", documentType);

        return "index";
    }

    @PostMapping("/upload")
    public String uploadImage(
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) {

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "파일이 비어있습니다.");
            return "redirect:/";
        }

        try {
            ImageUploadUseCase.UploadResult result = imageUploadUseCase.uploadAndProcess(file);
            redirectAttributes.addFlashAttribute("success", "파일 업로드 및 처리 완료");
            redirectAttributes.addFlashAttribute("result", result);
            return "redirect:/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "처리 실패: " + e.getMessage());
            return "redirect:/";
        }
    }

    @GetMapping("/images/{id}")
    public String viewImage(@PathVariable Long id, Model model) {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Image not found: " + id));

        if (image.getEncryptedOcrText() != null && !image.getEncryptedOcrText().isEmpty()) {
            try {
                String decryptedOcrText = encryptionService.decrypt(image.getEncryptedOcrText());
                image.setOcrText(decryptedOcrText);
            } catch (Exception e) {
            }
        }

        if (image.getEncryptedExtractedIdInfo() != null && !image.getEncryptedExtractedIdInfo().isEmpty()) {
            try {
                String decryptedExtractedInfo = encryptionService.decrypt(image.getEncryptedExtractedIdInfo());
                image.setExtractedIdInfo(decryptedExtractedInfo);
            } catch (Exception e) {
            }
        }

        model.addAttribute("image", image);

        ImageComparison comparison = imageCompareUseCase.compareWithLatest(id);
        model.addAttribute("comparison", comparison);

        return "image-detail";
    }
}

