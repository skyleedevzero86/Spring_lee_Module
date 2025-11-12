import { usePayment } from '@/hooks/usePayment';
import { Button } from '@/components/ui/button';

export default function PayPage() {
  const paymentHook = usePayment();

  const handlePay = async () => {
    try {
      await paymentHook.requestPayment(
        2,
        50000,
        {
          name: '홍길동',
          email: 'hong@example.com',
          mobilePhone: '01012345678',
        }
      );
    } catch (err) {
    }
  };

  return (
    <div className="max-w-2xl mx-auto mt-12 p-6">
      <h1 className="text-3xl font-bold mb-6 text-center">공연 예매</h1>

      <div className="mb-6 p-4 border rounded-lg bg-white">
        <div className="text-center py-8">
          <h2 className="text-xl font-semibold mb-2">공연 예매 결제</h2>
          <p className="text-sm text-gray-600 mb-4">
            결제하기 버튼을 클릭하면 토스 페이먼츠 결제창이 열립니다.
          </p>
        </div>
      </div>

      <div className="mb-6 p-4 border rounded-lg">
        <div className="text-sm text-gray-600">
          결제 금액: <span className="font-bold text-lg">50,000원</span>
        </div>
      </div>

      {paymentHook.error && (
        <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded text-red-600 text-sm">
          {paymentHook.error}
        </div>
      )}

      <Button
        onClick={handlePay}
        disabled={paymentHook.loading}
        className="w-full"
        size="lg"
      >
        {paymentHook.loading ? '결제 준비 중...' : '결제하기'}
      </Button>
    </div>
  );
}
