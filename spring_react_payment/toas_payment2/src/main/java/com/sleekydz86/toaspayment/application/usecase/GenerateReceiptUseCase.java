package com.sleekydz86.toaspayment.application.usecase;

import com.itextpdf.kernel.colors.ColorConstants;
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

            Paragraph title = new Paragraph("결제 영수증")
                    .setFontSize(24)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(title);

            Paragraph companyInfo = new Paragraph("토스 페이먼츠 결제 시스템")
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(30);
            document.add(companyInfo);

            float[] columnWidths = {2, 5};
            Table table = new Table(UnitValue.createPercentArray(columnWidths)).useAllAvailableWidth();

            table.addCell(createCell("주문번호", true));
            table.addCell(createCell(order.getOrderId().toString(), false));

            table.addCell(createCell("주문명", true));
            table.addCell(createCell(order.getOrderName(), false));

            table.addCell(createCell("결제금액", true));
            table.addCell(createCell(String.format("%,d원", order.getFinalAmount().toInteger()), false));

            table.addCell(createCell("결제수단", true));
            table.addCell(createCell(order.getPaymentMethod() != null ? order.getPaymentMethod().name() : "카드", false));

            table.addCell(createCell("결제상태", true));
            table.addCell(createCell(order.getStatus().name(), false));

            table.addCell(createCell("결제일시", true));
            table.addCell(createCell(order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), false));

            table.addCell(createCell("구매자명", true));
            table.addCell(createCell(user.getName(), false));

            table.addCell(createCell("구매자 이메일", true));
            table.addCell(createCell(user.getEmail(), false));

            document.add(table);

            Paragraph notice = new Paragraph("\n※ 본 영수증은 결제일로부터 14일간 발급 가능합니다.")
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

    private com.itextpdf.layout.element.Cell createCell(String text, boolean isHeader) {
        com.itextpdf.layout.element.Cell cell = new com.itextpdf.layout.element.Cell()
                .add(new Paragraph(text))
                .setPadding(10);
        if (isHeader) {
            cell.setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setBold();
        }
        return cell;
    }
}

