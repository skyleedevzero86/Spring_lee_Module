import { create } from 'zustand';
import type { Member, MemberRole } from '@/domain/types/member.types';
import { STORAGE_KEYS } from '@/constants/api.constants';

interface MemberState {
  member: Member | null;
  isAuthenticated: boolean;
  setMember: (member: Member | null) => void;
  clearMember: () => void;
  isAdmin: () => boolean;
  isUser: () => boolean;
}

const getStoredMember = (): Member | null => {
  if (typeof window === 'undefined') return null;
  const stored = localStorage.getItem(STORAGE_KEYS.MEMBER_STORAGE);
  if (!stored) return null;
  try {
    return JSON.parse(stored);
  } catch {
    return null;
  }
};

const setStoredMember = (member: Member | null): void => {
  if (typeof window === 'undefined') return;
  if (member) {
    localStorage.setItem(STORAGE_KEYS.MEMBER_STORAGE, JSON.stringify(member));
  } else {
    localStorage.removeItem(STORAGE_KEYS.MEMBER_STORAGE);
  }
};

export const useMemberStore = create<MemberState>((set, get) => ({
  member: getStoredMember(),
  isAuthenticated: getStoredMember() !== null,
  setMember: (member) => {
    setStoredMember(member);
    set({
      member,
      isAuthenticated: member !== null,
    });
  },
  clearMember: () => {
    setStoredMember(null);
    set({
      member: null,
      isAuthenticated: false,
    });
  },
  isAdmin: () => {
    const member = get().member;
    return member?.role === MemberRole.ADMIN;
  },
  isUser: () => {
    const member = get().member;
    return member?.role === MemberRole.USER;
  },
}));

