import { PaymentStateMachine } from '../payment-state-machine';
import { PaymentStatus } from '../types/payment.types';

describe('PaymentStateMachine', () => {
  let stateMachine: PaymentStateMachine;

  beforeEach(() => {
    stateMachine = new PaymentStateMachine();
  });

  describe('canTransition', () => {
    it('PENDING에서 APPROVED로 전이 허용', () => {
      expect(
        stateMachine.canTransition(PaymentStatus.PENDING, PaymentStatus.APPROVED)
      ).toBe(true);
    });

    it('PENDING에서 CANCELLED로 전이 허용', () => {
      expect(
        stateMachine.canTransition(
          PaymentStatus.PENDING,
          PaymentStatus.CANCELLED
        )
      ).toBe(true);
    });

    it('PENDING에서 FAILED로 전이 허용', () => {
      expect(
        stateMachine.canTransition(PaymentStatus.PENDING, PaymentStatus.FAILED)
      ).toBe(true);
    });

    it('APPROVED에서 COMPLETED로 전이 허용', () => {
      expect(
        stateMachine.canTransition(
          PaymentStatus.APPROVED,
          PaymentStatus.COMPLETED
        )
      ).toBe(true);
    });

    it('APPROVED에서 CANCELLED로 전이 허용', () => {
      expect(
        stateMachine.canTransition(
          PaymentStatus.APPROVED,
          PaymentStatus.CANCELLED
        )
      ).toBe(true);
    });

    it('FAILED에서 PENDING으로 전이 허용', () => {
      expect(
        stateMachine.canTransition(PaymentStatus.FAILED, PaymentStatus.PENDING)
      ).toBe(true);
    });

    it('PENDING에서 COMPLETED로 전이 불가', () => {
      expect(
        stateMachine.canTransition(
          PaymentStatus.PENDING,
          PaymentStatus.COMPLETED
        )
      ).toBe(false);
    });

    it('COMPLETED에서 다른 상태로 전이 불가', () => {
      expect(
        stateMachine.canTransition(
          PaymentStatus.COMPLETED,
          PaymentStatus.APPROVED
        )
      ).toBe(false);
    });

    it('CANCELLED에서 다른 상태로 전이 불가', () => {
      expect(
        stateMachine.canTransition(
          PaymentStatus.CANCELLED,
          PaymentStatus.APPROVED
        )
      ).toBe(false);
    });

    it('같은 상태로 전이 불가', () => {
      expect(
        stateMachine.canTransition(PaymentStatus.PENDING, PaymentStatus.PENDING)
      ).toBe(false);
    });
  });

  describe('transition', () => {
    it('PENDING에서 APPROVED로 전이', () => {
      const result = stateMachine.transition(
        PaymentStatus.PENDING,
        PaymentStatus.APPROVED
      );
      expect(result).toBe(PaymentStatus.APPROVED);
    });

    it('APPROVED에서 COMPLETED로 전이', () => {
      const result = stateMachine.transition(
        PaymentStatus.APPROVED,
        PaymentStatus.COMPLETED
      );
      expect(result).toBe(PaymentStatus.COMPLETED);
    });

    it('유효하지 않은 전이에 대한 에러 발생', () => {
      expect(() =>
        stateMachine.transition(PaymentStatus.PENDING, PaymentStatus.COMPLETED)
      ).toThrow('유효하지 않습니다');
    });

    it('터미널 상태에서 전이 시 에러 발생', () => {
      expect(() =>
        stateMachine.transition(
          PaymentStatus.COMPLETED,
          PaymentStatus.APPROVED
        )
      ).toThrow('유효하지 않습니다');
    });
  });

  describe('getAllowedTransitions', () => {
    it('PENDING에 대한 허용된 전이 반환', () => {
      const transitions = stateMachine.getAllowedTransitions(PaymentStatus.PENDING);
      expect(transitions).toContain(PaymentStatus.APPROVED);
      expect(transitions).toContain(PaymentStatus.CANCELLED);
      expect(transitions).toContain(PaymentStatus.FAILED);
      expect(transitions.length).toBe(3);
    });

    it('APPROVED에 대한 허용된 전이 반환', () => {
      const transitions = stateMachine.getAllowedTransitions(PaymentStatus.APPROVED);
      expect(transitions).toContain(PaymentStatus.COMPLETED);
      expect(transitions).toContain(PaymentStatus.CANCELLED);
      expect(transitions).toContain(PaymentStatus.FAILED);
      expect(transitions.length).toBe(3);
    });

    it('COMPLETED에 대한 빈 배열 반환', () => {
      const transitions = stateMachine.getAllowedTransitions(
        PaymentStatus.COMPLETED
      );
      expect(transitions).toEqual([]);
    });

    it('CANCELLED에 대한 빈 배열 반환', () => {
      const transitions = stateMachine.getAllowedTransitions(
        PaymentStatus.CANCELLED
      );
      expect(transitions).toEqual([]);
    });

    it('FAILED에 대한 전이 반환', () => {
      const transitions = stateMachine.getAllowedTransitions(PaymentStatus.FAILED);
      expect(transitions).toContain(PaymentStatus.PENDING);
      expect(transitions.length).toBe(1);
    });
  });

  describe('isTerminalState', () => {
    it('COMPLETED에 대해 true 반환', () => {
      expect(stateMachine.isTerminalState(PaymentStatus.COMPLETED)).toBe(true);
    });

    it('CANCELLED에 대해 true 반환', () => {
      expect(stateMachine.isTerminalState(PaymentStatus.CANCELLED)).toBe(true);
    });

    it('PENDING에 대해 false 반환', () => {
      expect(stateMachine.isTerminalState(PaymentStatus.PENDING)).toBe(false);
    });

    it('APPROVED에 대해 false 반환', () => {
      expect(stateMachine.isTerminalState(PaymentStatus.APPROVED)).toBe(false);
    });

    it('FAILED에 대해 false 반환', () => {
      expect(stateMachine.isTerminalState(PaymentStatus.FAILED)).toBe(false);
    });
  });

  describe('canRefund', () => {
    it('APPROVED에 대해 true 반환', () => {
      expect(stateMachine.canRefund(PaymentStatus.APPROVED)).toBe(true);
    });

    it('COMPLETED에 대해 true 반환', () => {
      expect(stateMachine.canRefund(PaymentStatus.COMPLETED)).toBe(true);
    });

    it('PENDING에 대해 false 반환', () => {
      expect(stateMachine.canRefund(PaymentStatus.PENDING)).toBe(false);
    });

    it('CANCELLED에 대해 false 반환', () => {
      expect(stateMachine.canRefund(PaymentStatus.CANCELLED)).toBe(false);
    });

    it('FAILED에 대해 false 반환', () => {
      expect(stateMachine.canRefund(PaymentStatus.FAILED)).toBe(false);
    });
  });

  describe('canCancel', () => {
    it('PENDING에 대해 true 반환', () => {
      expect(stateMachine.canCancel(PaymentStatus.PENDING)).toBe(true);
    });

    it('APPROVED에 대해 true 반환', () => {
      expect(stateMachine.canCancel(PaymentStatus.APPROVED)).toBe(true);
    });

    it('COMPLETED에 대해 false 반환', () => {
      expect(stateMachine.canCancel(PaymentStatus.COMPLETED)).toBe(false);
    });

    it('CANCELLED에 대해 false 반환', () => {
      expect(stateMachine.canCancel(PaymentStatus.CANCELLED)).toBe(false);
    });

    it('FAILED에 대해 false 반환', () => {
      expect(stateMachine.canCancel(PaymentStatus.FAILED)).toBe(false);
    });
  });
});
