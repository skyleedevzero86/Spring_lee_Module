import * as XLSX from 'xlsx';
import type { PaymentHistoryResponse } from '@/domain/types/payment.types';

function getStatusLabel(status: string): string {
  switch (status) {
    case 'COMPLETED':
      return '완료';
    case 'PENDING':
      return '대기';
    case 'CANCELLED':
      return '취소';
    case 'FAILED':
      return '실패';
    default:
      return status;
  }
}

export function exportPaymentsToExcel(payments: PaymentHistoryResponse[], filename = '결제내역') {
  const now = new Date();
  const year = now.getFullYear();
  const month = now.getMonth() + 1;
  const title = `${year}년 ${month}월 결제 내역`;

  const data: any[][] = [
    [],
    [],
    [],
    [],
    ['주문번호', '결재한사람', '상품명', '금액', '상태', '결제수단', '생성일시', '결제일시', '거래ID'],
  ];

  payments.forEach((payment) => {
    const payerInfo = payment.userName && payment.userEmail
      ? `${payment.userName} (${payment.userEmail})`
      : payment.userName || payment.userEmail || '-';
    
    data.push([
      payment.orderNo,
      payerInfo,
      payment.productDesc,
      payment.amount,
      getStatusLabel(payment.status),
      payment.payMethod || '-',
      payment.createdAt ? new Date(payment.createdAt).toLocaleString('ko-KR') : '-',
      payment.paidTs ? new Date(payment.paidTs).toLocaleString('ko-KR') : '-',
      payment.transactionId || '-',
    ]);
  });

  const worksheet = XLSX.utils.aoa_to_sheet(data);
  
  const titleCell = XLSX.utils.encode_cell({ r: 3, c: 3 });
  worksheet[titleCell] = { t: 's', v: title };
  
  worksheet['!cols'] = [
    { wch: 25 },
    { wch: 35 },
    { wch: 20 },
    { wch: 12 },
    { wch: 10 },
    { wch: 12 },
    { wch: 20 },
    { wch: 20 },
    { wch: 20 },
  ];

  const workbook = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(workbook, worksheet, '결제내역');

  const excelBuffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
  const blob = new Blob([excelBuffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = `${filename}_${new Date().toISOString().split('T')[0]}.xlsx`;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(url);
}

