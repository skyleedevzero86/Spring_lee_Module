package com.sleekydz86.payment2v2.domain.payment.application.port.out;

public interface InventoryPort {
    void deductInventory(String orderNo);
}
