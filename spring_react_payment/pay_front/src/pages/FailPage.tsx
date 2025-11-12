export default function FailPage() {
  return (
    <div className="max-w-lg mx-auto mt-10 p-4 border rounded text-center">
      <p className="text-red-600 font-bold text-xl">❌ 결제가 실패했습니다.</p>
      <p className="mt-2">다시 시도해주세요.</p>
    </div>
  );
}

