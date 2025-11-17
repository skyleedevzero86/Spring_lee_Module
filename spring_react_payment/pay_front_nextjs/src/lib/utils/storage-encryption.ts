const ENCRYPTION_KEY = 'auth_storage_key_v1';
const ALGORITHM = 'AES-GCM';
const KEY_LENGTH = 256;
const IV_LENGTH = 12;

async function deriveKey(password: string, salt: Uint8Array): Promise<CryptoKey> {
  const encoder = new TextEncoder();
  const keyMaterial = await crypto.subtle.importKey(
    'raw',
    encoder.encode(password),
    'PBKDF2',
    false,
    ['deriveBits', 'deriveKey']
  );

  return crypto.subtle.deriveKey(
    {
      name: 'PBKDF2',
      salt: salt,
      iterations: 100000,
      hash: 'SHA-256',
    },
    keyMaterial,
    { name: ALGORITHM, length: KEY_LENGTH },
    false,
    ['encrypt', 'decrypt']
  );
}

function generateRandomBytes(length: number): Uint8Array {
  return crypto.getRandomValues(new Uint8Array(length));
}

function stringToBytes(str: string): Uint8Array {
  return new TextEncoder().encode(str);
}

function bytesToString(bytes: Uint8Array): string {
  return new TextDecoder().decode(bytes);
}

function base64Encode(bytes: Uint8Array): string {
  return btoa(String.fromCharCode(...bytes));
}

function base64Decode(str: string): Uint8Array | null {
  try {
    return Uint8Array.from(atob(str), (c) => c.charCodeAt(0));
  } catch {
    return null;
  }
}

function isEncryptedData(data: string): boolean {
  try {
    const decoded = base64Decode(data);
    if (!decoded || decoded.length < 16 + IV_LENGTH) {
      return false;
    }
    return true;
  } catch {
    return false;
  }
}

export async function encryptData(data: string): Promise<string> {
  if (typeof window === 'undefined' || !crypto.subtle) {
    return data;
  }

  try {
    const salt = generateRandomBytes(16);
    const iv = generateRandomBytes(IV_LENGTH);
    const key = await deriveKey(ENCRYPTION_KEY, salt);

    const encrypted = await crypto.subtle.encrypt(
      {
        name: ALGORITHM,
        iv: iv,
      },
      key,
      stringToBytes(data)
    );

    const encryptedArray = new Uint8Array(encrypted);
    const combined = new Uint8Array(salt.length + iv.length + encryptedArray.length);
    combined.set(salt, 0);
    combined.set(iv, salt.length);
    combined.set(encryptedArray, salt.length + iv.length);

    return base64Encode(combined);
  } catch (error) {
    console.error('암호화 실패:', error);
    return data;
  }
}

export async function decryptData(encryptedData: string): Promise<string> {
  if (typeof window === 'undefined' || !crypto.subtle) {
    return encryptedData;
  }

  if (!isEncryptedData(encryptedData)) {
    return encryptedData;
  }

  try {
    const combined = base64Decode(encryptedData);
    if (!combined) {
      return encryptedData;
    }

    if (combined.length < 16 + IV_LENGTH) {
      return encryptedData;
    }

    const salt = combined.slice(0, 16);
    const iv = combined.slice(16, 16 + IV_LENGTH);
    const encrypted = combined.slice(16 + IV_LENGTH);

    const key = await deriveKey(ENCRYPTION_KEY, salt);

    const decrypted = await crypto.subtle.decrypt(
      {
        name: ALGORITHM,
        iv: iv,
      },
      key,
      encrypted
    );

    return bytesToString(new Uint8Array(decrypted));
  } catch (error) {
    return encryptedData;
  }
}

export async function setEncryptedItem(key: string, value: string): Promise<void> {
  if (typeof window === 'undefined') return;
  
  try {
    const encrypted = await encryptData(value);
    localStorage.setItem(key, encrypted);
  } catch (error) {
    console.error('암호화 저장 실패:', error);
    localStorage.setItem(key, value);
  }
}

export async function getEncryptedItem(key: string): Promise<string | null> {
  if (typeof window === 'undefined') return null;

  const stored = localStorage.getItem(key);
  if (!stored) return null;

  try {
    const decrypted = await decryptData(stored);
    return decrypted;
  } catch (error) {
    return stored;
  }
}

export function removeEncryptedItem(key: string): void {
  if (typeof window === 'undefined') return;
  localStorage.removeItem(key);
}
