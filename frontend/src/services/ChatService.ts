export const chatService = {
  async sendMessage(message: string, sessionId: string | null, userId: string) {
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

  async sendFeedback(sessionId: string, userId: string, feedback: string) {
    const res = await fetch("http://localhost:8080/api/v1/chat/feedback", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ sessionId, userId, feedback }),
    });
    if (!res.ok) {
      throw new Error("Failed to send feedback");
    }
  },
};
