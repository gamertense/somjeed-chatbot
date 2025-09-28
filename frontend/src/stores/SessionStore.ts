import { create } from "zustand";

interface SessionState {
  isLoggedIn: boolean;
  userId: string | null;
  login: (id: string) => void;
  logout: () => void;
}

export const useSessionStore = create<SessionState>((set) => ({
  isLoggedIn: false,
  userId: null,
  login: (id) => set({ isLoggedIn: true, userId: id }),
  logout: () => set({ isLoggedIn: false, userId: null }),
}));
