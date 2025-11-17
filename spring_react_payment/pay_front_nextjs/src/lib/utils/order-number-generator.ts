const ORDER_NO_PREFIX = 'orderNo';
const SEQUENCE_KEY = 'orderNo_sequence';
const DATE_KEY = 'orderNo_date';

function getTodayString(): string {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  return `${year}${month}${day}`;
}

function getCurrentDateTimeString(): string {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  const hours = String(now.getHours()).padStart(2, '0');
  const minutes = String(now.getMinutes()).padStart(2, '0');
  const seconds = String(now.getSeconds()).padStart(2, '0');
  return `${year}${month}${day}${hours}${minutes}${seconds}`;
}

function getSequence(): number {
  if (typeof window === 'undefined') {
    return Math.floor(Math.random() * 1000) + 1;
  }

  const today = getTodayString();
  const storedDate = localStorage.getItem(DATE_KEY);
  let sequence = 1;

  if (storedDate === today) {
    const storedSequence = localStorage.getItem(SEQUENCE_KEY);
    if (storedSequence) {
      sequence = parseInt(storedSequence, 10) + 1;
    }
  } else {
    localStorage.setItem(DATE_KEY, today);
  }

  localStorage.setItem(SEQUENCE_KEY, String(sequence));
  return sequence;
}

export function generateOrderNo(): string {
  const dateTime = getCurrentDateTimeString();
  const sequence = getSequence();
  
  const sequenceStr = sequence <= 999 
    ? String(sequence).padStart(3, '0')
    : String(sequence);
  
  return `${ORDER_NO_PREFIX}-${dateTime}-${sequenceStr}`;
}

export function resetOrderNoGenerator(): void {
  if (typeof window !== 'undefined') {
    localStorage.removeItem(SEQUENCE_KEY);
    localStorage.removeItem(DATE_KEY);
  }
}

