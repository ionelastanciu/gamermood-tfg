export interface Recommendation {
  id: number;
  sesionId: number;
  texto: string;
  fuente: string;
  createdAt: string;
}

export interface FeedbackRequest {
  util: boolean;
  comentario?: string;
}
