package com.sleekydz86.payment2v2.domain.payment.adapter.out.external.inventory;

import com.sleekydz86.payment2v2.domain.payment.application.port.out.InventoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryAdapter implements InventoryPort {

    @Override
    public void deductInventory(String orderNo) {
        log.info("재고 차감 처리 시작: orderNo={}", orderNo);
        log.info("재고 차감 처리 완료: orderNo={}", orderNo);
    }
}

