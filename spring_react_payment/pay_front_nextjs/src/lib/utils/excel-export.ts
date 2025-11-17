import ExcelJS from 'exceljs';
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

export async function exportPaymentsToExcel(payments: PaymentHistoryResponse[], filename = '결제내역') {
  const now = new Date();
  const year = now.getFullYear();
  const month = now.getMonth() + 1;
  const title = `${year}-${month}월 결제이력`;

  const workbook = new ExcelJS.Workbook();
  const worksheet = workbook.addWorksheet('결제내역');

  worksheet.getColumn(1).width = 25;
  worksheet.getColumn(2).width = 30;
  worksheet.getColumn(3).width = 20;
  worksheet.getColumn(4).width = 12;
  worksheet.getColumn(5).width = 10;
  worksheet.getColumn(6).width = 12;
  worksheet.getColumn(7).width = 20;
  worksheet.getColumn(8).width = 20;
  worksheet.getColumn(9).width = 20;

  const titleRow = worksheet.getRow(1);
  titleRow.height = 25;
  const titleCell = worksheet.getCell('A1');
  titleCell.value = title;
  titleCell.font = {
    size: 16,
    bold: true,
  };
  titleCell.alignment = {
    vertical: 'middle',
    horizontal: 'left',
  };

  const headerRow = worksheet.getRow(2);
  headerRow.height = 20;
  
  const headers = ['주문번호', '결재한사람', '상품명', '금액', '상태', '결제수단', '생성일시', '결제일시', '거래ID'];
  headers.forEach((header, index) => {
    const cell = worksheet.getCell(2, index + 1);
    cell.value = header;
    cell.font = {
      size: 11,
      bold: true,
      color: { argb: 'FFFFFFFF' },
    };
    cell.fill = {
      type: 'pattern',
      pattern: 'solid',
      fgColor: { argb: 'FF808080' },
    };
    cell.alignment = {
      vertical: 'middle',
      horizontal: 'center',
    };
    cell.border = {
      top: { style: 'thin', color: { argb: 'FF000000' } },
      left: { style: 'thin', color: { argb: 'FF000000' } },
      bottom: { style: 'thin', color: { argb: 'FF000000' } },
      right: { style: 'thin', color: { argb: 'FF000000' } },
    };
  });

  payments.forEach((payment, index) => {
    const row = worksheet.getRow(index + 3);
    row.height = 18;

    const payerInfo = payment.userName && payment.userEmail
      ? `${payment.userName} (${payment.userEmail})`
      : payment.userName || payment.userEmail || '-';

    const rowData = [
      payment.orderNo,
      payerInfo,
      payment.productDesc,
      payment.amount,
      getStatusLabel(payment.status),
      payment.payMethod || '-',
      payment.createdAt ? new Date(payment.createdAt).toLocaleString('ko-KR') : '-',
      payment.paidTs ? new Date(payment.paidTs).toLocaleString('ko-KR') : '-',
      payment.transactionId || '-',
    ];

    rowData.forEach((value, colIndex) => {
      const cell = worksheet.getCell(index + 3, colIndex + 1);
      cell.value = value;
      cell.alignment = {
        vertical: 'middle',
        horizontal: colIndex === 1 || colIndex === 2 ? 'left' : 'center',
      };
      cell.border = {
        top: { style: 'thin', color: { argb: 'FFD3D3D3' } },
        left: { style: 'thin', color: { argb: 'FFD3D3D3' } },
        bottom: { style: 'thin', color: { argb: 'FFD3D3D3' } },
        right: { style: 'thin', color: { argb: 'FFD3D3D3' } },
      };
    });
  });

  const buffer = await workbook.xlsx.writeBuffer();
  const blob = new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = `${filename}_${new Date().toISOString().split('T')[0]}.xlsx`;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(url);
}
