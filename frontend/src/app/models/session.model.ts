export interface SessionRequest {
  game: string;
  mood: 'happy' | 'neutral' | 'sad';
  intensity: number;
  experience: string;
}

export interface SessionResponse {
  id: number;
  game: string;
  mood: string;
  intensity: number;
  experience: string;
  createdAt: string;
}
