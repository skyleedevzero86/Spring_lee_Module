package com.sleekydz86.toaspayment.infrastructure.persistence;

import com.sleekydz86.toaspayment.domain.paymentlog.PaymentLog;
import com.sleekydz86.toaspayment.domain.paymentlog.PaymentLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaPaymentLogRepository extends JpaRepository<PaymentLog, Long> {
    List<PaymentLog> findByOrderId(String orderId);
    List<PaymentLog> findByMemberId(Long memberId);
}

@Component
@RequiredArgsConstructor
class PaymentLogRepositoryImpl implements PaymentLogRepository {
    private final JpaPaymentLogRepository jpaPaymentLogRepository;

    @Override
    public PaymentLog save(PaymentLog paymentLog) {
        return jpaPaymentLogRepository.save(paymentLog);
    }

    @Override
    public List<PaymentLog> findByOrderId(String orderId) {
        return jpaPaymentLogRepository.findByOrderId(orderId);
    }

    @Override
    public List<PaymentLog> findByMemberId(Long memberId) {
        return jpaPaymentLogRepository.findByMemberId(memberId);
    }

    @Override
    public List<PaymentLog> findAll() {
        return jpaPaymentLogRepository.findAll();
    }

    @Override
    public Optional<PaymentLog> findById(Long id) {
        return jpaPaymentLogRepository.findById(id);
    }
}



