import { Link } from 'react-router-dom';
import { Button } from '@/components/ui/button';

export default function HomePage() {
  return (
    <div className="min-h-screen bg-background">
      <div className="container mx-auto px-4 py-8">
        <main className="flex flex-col gap-8 items-center sm:items-start max-w-4xl mx-auto">
          <div className="text-center sm:text-left w-full">
            <h1 className="text-4xl font-bold text-foreground mb-4">
              Toss Payment
            </h1>
            <p className="text-muted-foreground mb-6">
              Toss Payments Sample Integration
            </p>
          </div>

          <div className="flex gap-4 items-center flex-col sm:flex-row w-full sm:w-auto">
            <Link to="/login">
              <Button variant="default" size="lg">
                로그인
              </Button>
            </Link>
            <Link to="/pay">
              <Button variant="outline" size="lg">
                결제하기
              </Button>
            </Link>
          </div>

          <div className="mt-8 p-6 border rounded w-full">
            <h2 className="text-xl font-semibold mb-4">주요 기능</h2>
            <ul className="list-disc list-inside space-y-2 text-muted-foreground">
              <li>사용자 로그인</li>
              <li>공연 예매 결제</li>
              <li>결제 성공/실패 처리</li>
              <li>Toss Payments 연동</li>
            </ul>
          </div>
        </main>
      </div>
    </div>
  );
}

