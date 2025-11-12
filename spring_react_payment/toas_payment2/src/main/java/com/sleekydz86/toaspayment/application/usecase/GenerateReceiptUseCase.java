package com.sleekydz86.toaspayment.application.usecase;

import com.itextpdf.io.font.FontProgram;
import com.itextpdf.io.font.FontProgramFactory;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.sleekydz86.toaspayment.domain.order.Order;
import com.sleekydz86.toaspayment.domain.order.OrderRepository;
import com.sleekydz86.toaspayment.domain.order.valueobject.OrderId;
import com.sleekydz86.toaspayment.domain.user.User;
import com.sleekydz86.toaspayment.domain.user.UserRepository;
import com.sleekydz86.toaspayment.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenerateReceiptUseCase {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private static final int RECEIPT_AVAILABLE_DAYS = 14;

    public byte[] execute(String orderIdString) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long memberId = Long.parseLong(authentication.getName());

        OrderId orderId = OrderId.of(orderIdString);
        Order order = orderRepository.findByOrderIdAndMemberId(orderId, memberId)
                .orElseThrow(() -> new BadRequestException("주문을 찾을 수 없습니다."));

        if (!order.isDone()) {
            throw new BadRequestException("결제가 완료된 주문만 영수증을 발급할 수 있습니다.");
        }

        LocalDateTime paymentDate = order.getCreatedAt();
        LocalDateTime now = LocalDateTime.now();
        long daysSincePayment = ChronoUnit.DAYS.between(paymentDate, now);

        if (daysSincePayment > RECEIPT_AVAILABLE_DAYS) {
            throw new BadRequestException("영수증 발급 기한이 지났습니다. 결제일로부터 " + RECEIPT_AVAILABLE_DAYS + "일 이내에만 발급 가능합니다.");
        }

        User user = userRepository.findById(memberId)
                .orElseThrow(() -> new BadRequestException("사용자를 찾을 수 없습니다."));

        return generatePdf(order, user);
    }

    private byte[] generatePdf(Order order, User user) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            PdfFont koreanFont = createKoreanFont();
            log.info("PDF 생성 시작 - 폰트 로드 완료");

            Paragraph title = new Paragraph("결제 영수증")
                    .setFont(koreanFont)
                    .setFontSize(24)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(title);

            Paragraph companyInfo = new Paragraph("토스 페이먼츠 결제 시스템")
                    .setFont(koreanFont)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(30);
            document.add(companyInfo);

            float[] columnWidths = { 2, 5 };
            Table table = new Table(UnitValue.createPercentArray(columnWidths)).useAllAvailableWidth();

            table.addCell(createCell("주문번호", true, koreanFont));
            table.addCell(createCell(order.getOrderId().toString(), false, koreanFont));

            if (order.getOriginalOrderId() != null && !order.getOriginalOrderId().isBlank()) {
                table.addCell(createCell("원본 주문번호", true, koreanFont));
                table.addCell(createCell(order.getOriginalOrderId(), false, koreanFont));
            }

            table.addCell(createCell("주문명", true, koreanFont));
            table.addCell(createCell(order.getOrderName(), false, koreanFont));

            table.addCell(createCell("결제금액", true, koreanFont));
            table.addCell(createCell(String.format("%,d원", order.getFinalAmount().toInteger()), false, koreanFont));

            table.addCell(createCell("결제수단", true, koreanFont));
            table.addCell(createCell(order.getPaymentMethod() != null ? order.getPaymentMethod().name() : "카드", false,
                    koreanFont));

            table.addCell(createCell("결제상태", true, koreanFont));
            table.addCell(createCell(order.getStatus().name(), false, koreanFont));

            table.addCell(createCell("결제일시", true, koreanFont));
            table.addCell(createCell(order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    false, koreanFont));

            table.addCell(createCell("구매자명", true, koreanFont));
            table.addCell(createCell(user.getName(), false, koreanFont));

            table.addCell(createCell("구매자 이메일", true, koreanFont));
            table.addCell(createCell(user.getEmail(), false, koreanFont));

            document.add(table);

            Paragraph notice = new Paragraph("\n※ 본 영수증은 결제일로부터 14일간 발급 가능합니다.")
                    .setFont(koreanFont)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(30);
            document.add(notice);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("PDF 생성 중 오류 발생: {}", e.getMessage(), e);
            throw new BadRequestException("영수증 생성 중 오류가 발생했습니다.");
        }
    }

    private PdfFont createKoreanFont() {
        try {
            String osName = System.getProperty("os.name").toLowerCase();
            log.info("OS 감지: {}", osName);

            if (osName.contains("win")) {
                PdfFont font = findWindowsKoreanFont();
                if (font != null) {
                    return font;
                }
            } else if (osName.contains("mac")) {
                PdfFont font = findMacKoreanFont();
                if (font != null) {
                    return font;
                }
            } else {
                PdfFont font = findLinuxKoreanFont();
                if (font != null) {
                    return font;
                }
            }

            PdfFont font = trySystemFontNames();
            if (font != null) {
                return font;
            }

            log.warn("한글 폰트를 찾을 수 없어 기본 폰트를 사용합니다. 한글이 깨질 수 있습니다.");
            try {
                return PdfFontFactory.createFont(StandardFonts.HELVETICA);
            } catch (IOException e) {
                log.error("기본 폰트 생성 실패: {}", e.getMessage(), e);
                throw new BadRequestException("PDF 폰트 생성에 실패했습니다.");
            }
        } catch (Exception e) {
            log.error("폰트 생성 실패: {}", e.getMessage(), e);
            try {
                return PdfFontFactory.createFont(StandardFonts.HELVETICA);
            } catch (IOException ioException) {
                log.error("기본 폰트 생성도 실패: {}", ioException.getMessage(), ioException);
                throw new BadRequestException("PDF 폰트 생성에 실패했습니다.");
            }
        }
    }

    private PdfFont findWindowsKoreanFont() {
        try {
            String windowsFontDir = System.getenv("WINDIR");
            if (windowsFontDir == null) {
                windowsFontDir = "C:\\Windows";
            }
            Path fontsDir = Paths.get(windowsFontDir, "Fonts");
            log.info("Windows 폰트 디렉토리: {}", fontsDir);

            if (!Files.exists(fontsDir)) {
                log.warn("폰트 디렉토리가 존재하지 않습니다: {}", fontsDir);
                return null;
            }

            String[] preferredFonts = {
                    "malgun.ttf", "malgunbd.ttf", "malgunsl.ttf",
                    "gulim.ttc", "gulimche.ttc",
                    "batang.ttc", "batangche.ttc",
                    "gungsuh.ttc", "gungsuhche.ttc",
                    "NanumGothic.ttf", "NanumBarunGothic.ttf",
                    "NanumGothicBold.ttf", "NanumBarunGothicBold.ttf"
            };

            for (String fontFile : preferredFonts) {
                Path fontPath = fontsDir.resolve(fontFile);
                if (Files.exists(fontPath)) {
                    try {
                        log.info("폰트 파일 발견: {}", fontPath);
                        FontProgram fontProgram = FontProgramFactory.createFont(fontPath.toString());
                        PdfFont font = PdfFontFactory.createFont(fontProgram, "Identity-H");
                        log.info("폰트 로드 성공: {}", fontPath);
                        return font;
                    } catch (Exception e) {
                        log.warn("폰트 로드 실패: {}, 오류: {}", fontPath, e.getMessage());
                    }
                }
            }

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(fontsDir, "*.{ttf,ttc,otf}")) {
                for (Path fontPath : stream) {
                    String fileName = fontPath.getFileName().toString().toLowerCase();
                    if (fileName.contains("malgun") || fileName.contains("gulim") ||
                            fileName.contains("batang") || fileName.contains("gungsuh") ||
                            fileName.contains("nanum")) {
                        try {
                            log.info("한글 폰트 파일 발견: {}", fontPath);
                            FontProgram fontProgram = FontProgramFactory.createFont(fontPath.toString());
                            PdfFont font = PdfFontFactory.createFont(fontProgram, "Identity-H");
                            log.info("폰트 로드 성공: {}", fontPath);
                            return font;
                        } catch (Exception e) {
                            log.debug("폰트 로드 실패: {}, 오류: {}", fontPath, e.getMessage());
                        }
                    }
                }
            } catch (IOException e) {
                log.warn("폰트 디렉토리 스캔 실패: {}", e.getMessage());
            }
        } catch (Exception e) {
            log.error("Windows 폰트 검색 실패: {}", e.getMessage());
        }
        return null;
    }

    private PdfFont findMacKoreanFont() {
        String[] fontPaths = {
                "/System/Library/Fonts/AppleGothic.ttf",
                "/Library/Fonts/AppleGothic.ttf",
                "/System/Library/Fonts/Supplemental/AppleGothic.ttf"
        };

        for (String fontPath : fontPaths) {
            try {
                Path path = Paths.get(fontPath);
                if (Files.exists(path)) {
                    log.info("폰트 파일 발견: {}", fontPath);
                    FontProgram fontProgram = FontProgramFactory.createFont(fontPath);
                    PdfFont font = PdfFontFactory.createFont(fontProgram, "Identity-H");
                    log.info("폰트 로드 성공: {}", fontPath);
                    return font;
                }
            } catch (Exception e) {
                log.debug("폰트 로드 실패: {}, 오류: {}", fontPath, e.getMessage());
            }
        }
        return null;
    }

    private PdfFont findLinuxKoreanFont() {
        String[] fontPaths = {
                "/usr/share/fonts/truetype/noto/NotoSansCJK-Regular.ttc",
                "/usr/share/fonts/truetype/nanum/NanumGothic.ttf",
                "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.otf"
        };

        for (String fontPath : fontPaths) {
            try {
                Path path = Paths.get(fontPath);
                if (Files.exists(path)) {
                    log.info("폰트 파일 발견: {}", fontPath);
                    FontProgram fontProgram = FontProgramFactory.createFont(fontPath);
                    PdfFont font = PdfFontFactory.createFont(fontProgram, "Identity-H");
                    log.info("폰트 로드 성공: {}", fontPath);
                    return font;
                }
            } catch (Exception e) {
                log.debug("폰트 로드 실패: {}, 오류: {}", fontPath, e.getMessage());
            }
        }
        return null;
    }

    private PdfFont trySystemFontNames() {
        String[] fontNames = {
                "Malgun Gothic",
                "맑은 고딕",
                "AppleGothic",
                "Noto Sans CJK KR",
                "NanumGothic",
                "Gulim"
        };

        for (String fontName : fontNames) {
            try {
                log.debug("폰트 이름으로 로드 시도: {}", fontName);
                FontProgram fontProgram = FontProgramFactory.createFont(fontName);
                PdfFont font = PdfFontFactory.createFont(fontProgram, "Identity-H");
                log.info("폰트 로드 성공 (이름): {}", fontName);
                return font;
            } catch (Exception e) {
                log.debug("폰트 로드 실패 (이름): {}, 오류: {}", fontName, e.getMessage());
            }
        }
        return null;
    }

    private com.itextpdf.layout.element.Cell createCell(String text, boolean isHeader, PdfFont font) {
        com.itextpdf.layout.element.Cell cell = new com.itextpdf.layout.element.Cell()
                .add(new Paragraph(text).setFont(font))
                .setPadding(10);
        if (isHeader) {
            cell.setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setBold();
        }
        return cell;
    }
}
