import { PaymentStatus } from './types/payment.types';

export class PaymentStateMachine {
  private readonly validTransitions: Record<
    PaymentStatus,
    PaymentStatus[]
  > = {
    [PaymentStatus.PENDING]: [
      PaymentStatus.APPROVED,
      PaymentStatus.CANCELLED,
      PaymentStatus.FAILED,
    ],
    [PaymentStatus.APPROVED]: [
      PaymentStatus.COMPLETED,
      PaymentStatus.CANCELLED,
      PaymentStatus.FAILED,
    ],
    [PaymentStatus.COMPLETED]: [],
    [PaymentStatus.CANCELLED]: [],
    [PaymentStatus.FAILED]: [PaymentStatus.PENDING],
  };

  canTransition(
    from: PaymentStatus,
    to: PaymentStatus
  ): boolean {
    if (from === to) {
      return false;
    }

    const allowedTransitions = this.validTransitions[from];
    if (!allowedTransitions) {
      return false;
    }

    return allowedTransitions.includes(to);
  }

  transition(
    currentStatus: PaymentStatus,
    targetStatus: PaymentStatus
  ): PaymentStatus {
    if (!this.canTransition(currentStatus, targetStatus)) {
      throw new Error(
        `${currentStatus}에서 ${targetStatus}로의 전이는 유효하지 않습니다`
      );
    }

    return targetStatus;
  }

  getAllowedTransitions(
    currentStatus: PaymentStatus
  ): PaymentStatus[] {
    return [...(this.validTransitions[currentStatus] || [])];
  }

  isTerminalState(status: PaymentStatus): boolean {
    return (
      status === PaymentStatus.COMPLETED ||
      status === PaymentStatus.CANCELLED
    );
  }

  canRefund(status: PaymentStatus): boolean {
    return status === PaymentStatus.APPROVED || status === PaymentStatus.COMPLETED;
  }

  canCancel(status: PaymentStatus): boolean {
    return (
      status === PaymentStatus.PENDING || status === PaymentStatus.APPROVED
    );
  }
}

export const paymentStateMachine = new PaymentStateMachine();

