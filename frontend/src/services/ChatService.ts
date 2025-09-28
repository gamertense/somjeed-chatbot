export type FeedbackRating = "HAPPY" | "NEUTRAL" | "SAD";

export interface SendMessageResponse {
  sessionId: string;
  botMessage: string;
  timestamp: string;
  quickReplies?: string[];
  feedbackOptions?: string[];
}

export interface FeedbackSubmissionRequest {
  sessionId: string;
  userId: string;
  rating: FeedbackRating;
}

export const chatService = {
  async sendMessage(
    message: string,
    sessionId: string | null,
    userId: string
  ): Promise<SendMessageResponse> {
    const res = await fetch("http://localhost:8080/api/v1/chat", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ message, sessionId, userId }),
    });
    if (!res.ok) {
      throw new Error("Failed to send message");
    }
    return res.json();
  },

  async sendFeedback(
    sessionId: string,
    userId: string,
    rating: FeedbackRating
  ): Promise<void> {
    const res = await fetch("http://localhost:8080/api/v1/chat/feedback", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        sessionId,
        userId,
        rating,
      } as FeedbackSubmissionRequest),
    });
    if (!res.ok) {
      throw new Error("Failed to send feedback");
    }
  },
};
