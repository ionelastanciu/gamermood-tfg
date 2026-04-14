export interface SessionRequest {
  game: string;
  mood: 'happy' | 'neutral' | 'sad';
  intensity: number;   // 1–10
  experience: string;
}

export interface SessionResponse {
  id: number;
  game: string;
  mood: string;
  intensity: number;
  experience: string;
  createdAt: string;   // ISO 8601
}
