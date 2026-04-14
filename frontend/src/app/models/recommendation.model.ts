export interface Recommendation {
  id: number;
  sessionId: number;
  content: string;
  category: string;
  createdAt: string;
}

export interface FeedbackRequest {
  recommendationId: number;
  useful: boolean;
  comment?: string;
}
