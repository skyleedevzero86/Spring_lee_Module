export function isMobileDevice(): boolean {
  if (typeof window === 'undefined') return false;
  
  const userAgent = navigator.userAgent || navigator.vendor || (window as any).opera;
  const mobilePattern = /android|webos|iphone|ipad|ipod|blackberry|iemobile|opera mini/i;
  const hasTouchScreen = 'ontouchstart' in window || navigator.maxTouchPoints > 0;
  const isSmallScreen = window.innerWidth <= 768;
  
  return mobilePattern.test(userAgent) || (hasTouchScreen && isSmallScreen);
}

export function base64UrlToArrayBuffer(base64url: string): ArrayBuffer {
  const base64 = base64url.replace(/-/g, '+').replace(/_/g, '/');
  const paddedBase64 = base64 + '='.repeat((4 - base64.length % 4) % 4);
  const binary = atob(paddedBase64);
  const bytes = new Uint8Array(binary.length);
  for (let i = 0; i < binary.length; i++) {
    bytes[i] = binary.charCodeAt(i);
  }
  return bytes.buffer;
}

export function arrayBufferToBase64Url(buffer: ArrayBuffer): string {
  const bytes = new Uint8Array(buffer);
  let binary = '';
  for (let i = 0; i < bytes.length; i++) {
    binary += String.fromCharCode(bytes[i]);
  }
  return btoa(binary).replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '');
}

export function convertChallenge(challenge: any): ArrayBuffer {
  if (challenge instanceof ArrayBuffer) {
    return challenge;
  }
  if (challenge instanceof Uint8Array) {
    return challenge.buffer;
  }
  if (typeof challenge === 'string') {
    return base64UrlToArrayBuffer(challenge);
  }
  if (challenge?.value !== undefined) {
    if (Array.isArray(challenge.value)) {
      return new Uint8Array(challenge.value).buffer;
    }
    if (typeof challenge.value === 'string') {
      return base64UrlToArrayBuffer(challenge.value);
    }
    if (challenge.value instanceof ArrayBuffer) {
      return challenge.value;
    }
  }
  if (Array.isArray(challenge)) {
    return new Uint8Array(challenge).buffer;
  }
  if (typeof challenge === 'object') {
    const values = Object.values(challenge);
    if (values.length > 0 && values.every(v => typeof v === 'number')) {
      return new Uint8Array(values as number[]).buffer;
    }
  }
  throw new Error('Invalid challenge format');
}

export function convertUser(user: any): any {
  if (!user || !user.id) return user;
  
  if (typeof user.id === 'string') {
    return { ...user, id: base64UrlToArrayBuffer(user.id) };
  }
  if (Array.isArray(user.id)) {
    return { ...user, id: new Uint8Array(user.id).buffer };
  }
  if (user.id?.value && Array.isArray(user.id.value)) {
    return { ...user, id: new Uint8Array(user.id.value).buffer };
  }
  if (user.id instanceof ArrayBuffer) {
    return user;
  }
  
  throw new Error('Invalid user.id format');
}

export function convertCredentials(credentials: any[]): any[] {
  if (!Array.isArray(credentials)) return [];
  
  return credentials.map(cred => {
    let id = cred.id;
    if (typeof id === 'string') {
      id = base64UrlToArrayBuffer(id);
    } else if (Array.isArray(id)) {
      id = new Uint8Array(id).buffer;
    } else if (id?.value && Array.isArray(id.value)) {
      id = new Uint8Array(id.value).buffer;
    }
    return {
      ...cred,
      id: id,
    };
  });
}

function isNgrokEnvironment(): boolean {
  if (typeof window === 'undefined') return false;
  return window.location.hostname.includes('.ngrok.io') || 
         window.location.hostname.includes('.ngrok-free.app');
}

export function prepareRegistrationOptions(options: any): any {
  const prepared = { ...options };
  const isMobile = isMobileDevice();
  const isNgrok = isNgrokEnvironment();
  
  if (prepared.challenge) {
    prepared.challenge = convertChallenge(prepared.challenge);
  }
  
  if (prepared.user) {
    prepared.user = convertUser(prepared.user);
  }
  
  if (prepared.excludeCredentials) {
    prepared.excludeCredentials = convertCredentials(prepared.excludeCredentials);
  }
  
  if (prepared.hints !== undefined) {
    if (!Array.isArray(prepared.hints) || prepared.hints.length === 0) {
      delete prepared.hints;
    }
  }
  
  if (isMobile) {
    if (!prepared.authenticatorSelection) {
      prepared.authenticatorSelection = {};
    }
    prepared.authenticatorSelection.authenticatorAttachment = 'platform';
    prepared.authenticatorSelection.userVerification = 'required';
    if (prepared.authenticatorSelection.requireResidentKey === undefined) {
      prepared.authenticatorSelection.requireResidentKey = true;
    }
  }
  
  return prepared;
}

export function prepareAuthenticationOptions(options: any): any {
  const prepared = { ...options };
  const isMobile = isMobileDevice();
  const isNgrok = isNgrokEnvironment();
  
  if (prepared.challenge) {
    prepared.challenge = convertChallenge(prepared.challenge);
  }
  
  if (prepared.allowCredentials) {
    prepared.allowCredentials = convertCredentials(prepared.allowCredentials);
  }
  
  if (prepared.hints !== undefined) {
    if (!Array.isArray(prepared.hints) || prepared.hints.length === 0) {
      delete prepared.hints;
    }
  }
  
  if (isMobile && isNgrok) {
    prepared.userVerification = 'preferred';
    if (prepared.timeout === undefined || prepared.timeout < 60000) {
      prepared.timeout = 60000;
    }
  } else if (isMobile) {
    prepared.userVerification = 'required';
  }
  
  return prepared;
}

