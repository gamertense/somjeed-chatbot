import { useState, useRef, useEffect } from "react";
import LoginForm from "../components/LoginForm";
import FeedbackRequest, { FeedbackRating } from "../components/FeedbackRequest";
import { useInactivityManager } from "../components/InactivityManager";
import { useSessionStore } from "../stores/SessionStore";
import { chatService, SendMessageResponse } from "../services/ChatService";

interface Message {
  id: string;
  text: string;
  sender: "user" | "bot";
  timestamp: Date;
  quickReplies?: string[];
  feedbackOptions?: string[];
}

export default function Home() {
  const [messages, setMessages] = useState<Message[]>([]);
  const [inputText, setInputText] = useState("");
  const [sessionId, setSessionId] = useState<string | null>(null);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const [loginError, setLoginError] = useState<string | null>(null);

  // Inactivity and feedback state
  const [showAssistancePrompt, setShowAssistancePrompt] = useState(false);
  const [showFeedbackRequest, setShowFeedbackRequest] = useState(false);
  const [lastBotResponseTime, setLastBotResponseTime] = useState<
    number | undefined
  >();
  const [userActivityTrigger, setUserActivityTrigger] = useState<
    number | undefined
  >();

  const { isLoggedIn, userId, login } = useSessionStore();

  // Initialize inactivity manager
  useInactivityManager({
    onShowAssistancePrompt: () => {
      setShowAssistancePrompt(true);
      // Add the assistance prompt message to chat
      const assistanceMessage: Message = {
        id: `assistance-${Date.now()}`,
        text: "Do you need any further assistance?",
        sender: "bot",
        timestamp: new Date(),
      };
      setMessages((prev) => [...prev, assistanceMessage]);
    },
    onShowFeedbackRequest: () => {
      setShowFeedbackRequest(true);
    },
    isAssistancePromptVisible: showAssistancePrompt,
    isFeedbackVisible: showFeedbackRequest,
    lastBotResponseTime,
    userActivityTrigger,
  });

  const handleLogin = async (id: string) => {
    setLoginError(null);
    try {
      await fetchGreeting(id);
      login(id);
    } catch {
      setLoginError("Invalid user ID. Please check and try again.");
    }
  };

  const triggerUserActivity = () => {
    setUserActivityTrigger(Date.now());
    setShowAssistancePrompt(false);
    setShowFeedbackRequest(false);
  };

  const fetchGreeting = async (userId: string): Promise<void> => {
    const data = await chatService.sendMessage("hello", null, userId);
    setSessionId(data.sessionId);
    const greetingMessage: Message = {
      id: Date.now().toString(),
      text: data.botMessage,
      sender: "bot",
      timestamp: new Date(),
      quickReplies: data.quickReplies,
    };
    setMessages([greetingMessage]);
    // Start inactivity timer after greeting
    setLastBotResponseTime(Date.now());
  };

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const handleSend = async (text?: string) => {
    const messageText = text || inputText.trim();
    if (messageText) {
      triggerUserActivity(); // Reset inactivity timers

      const userMessage: Message = {
        id: Date.now().toString(),
        text: messageText,
        sender: "user",
        timestamp: new Date(),
      };
      setMessages((prev) => [...prev, userMessage]);
      setInputText("");

      try {
        const data: SendMessageResponse = await chatService.sendMessage(
          messageText,
          sessionId,
          userId!
        );
        setSessionId(data.sessionId);
        const botMessage: Message = {
          id: (Date.now() + 1).toString(),
          text: data.botMessage,
          sender: "bot",
          timestamp: new Date(),
          quickReplies: data.quickReplies,
        };
        setMessages((prev) => [...prev, botMessage]);

        // Start inactivity timer after bot response (service completion)
        setLastBotResponseTime(Date.now());
      } catch {
        const errorMessage: Message = {
          id: (Date.now() + 1).toString(),
          text: "Sorry, I encountered an error. Please try again.",
          sender: "bot",
          timestamp: new Date(),
        };
        setMessages((prev) => [...prev, errorMessage]);
      }
    }
  };

  const handleQuickReply = (reply: string) => {
    handleSend(reply);
  };

  const handleFeedbackSubmit = async (rating: FeedbackRating) => {
    try {
      await chatService.sendFeedback(sessionId!, userId!, rating);

      // Hide feedback UI
      setShowFeedbackRequest(false);

      // Add confirmation message
      const confirmationMessage: Message = {
        id: Date.now().toString(),
        text: "Thank you for your feedback! 🙏",
        sender: "bot",
        timestamp: new Date(),
      };
      setMessages((prev) => [...prev, confirmationMessage]);

      // Clear all timers - session complete
      setLastBotResponseTime(undefined);
      setUserActivityTrigger(undefined);
    } catch (error) {
      console.error("Failed to submit feedback:", error);
      // Optionally show error message to user
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    triggerUserActivity(); // Trigger activity on typing
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setInputText(e.target.value);
    triggerUserActivity(); // Trigger activity on typing
  };

  return !isLoggedIn ? (
    <LoginForm onLogin={handleLogin} errorMessage={loginError} />
  ) : (
    <div className="h-screen bg-gray-50 dark:bg-gray-900 flex flex-col">
      {/* Fixed Header */}
      <header className="bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700 px-4 py-3 shadow-sm flex-shrink-0">
        <h1 className="text-lg font-semibold text-gray-800 dark:text-white">
          Somjeed Chat
        </h1>
      </header>

      {/* Scrollable Messages Area */}
      <div className="flex-1 overflow-hidden flex justify-center">
        <div className="w-full max-w-4xl flex flex-col">
          <div className="flex-1 overflow-y-auto px-4 py-4">
            <div className="space-y-4">
              {messages.length === 0 ? (
                <div className="text-center text-gray-500 mt-8">
                  Start a conversation with Somjeed
                </div>
              ) : (
                messages.map((message) => (
                  <div
                    key={message.id}
                    className={`flex ${
                      message.sender === "user"
                        ? "justify-end"
                        : "justify-start"
                    }`}
                  >
                    <div className="max-w-xs lg:max-w-md">
                      <div
                        className={`px-4 py-2 rounded-lg ${
                          message.sender === "user"
                            ? "bg-blue-500 text-white"
                            : "bg-white dark:bg-gray-800 text-gray-800 dark:text-white border border-gray-200 dark:border-gray-700"
                        }`}
                      >
                        <p className="text-sm">{message.text}</p>
                        <p className="text-xs opacity-70 mt-1">
                          {message.timestamp.toLocaleTimeString()}
                        </p>
                      </div>
                      {message.quickReplies && (
                        <div className="flex flex-wrap gap-2 mt-2">
                          {message.quickReplies.map((reply, index) => (
                            <button
                              key={index}
                              onClick={() => handleQuickReply(reply)}
                              className="px-3 py-1 bg-gray-100 dark:bg-gray-700 text-gray-800 dark:text-white rounded-full text-sm hover:bg-gray-200 dark:hover:bg-gray-600"
                            >
                              {reply}
                            </button>
                          ))}
                        </div>
                      )}
                    </div>
                  </div>
                ))
              )}

              {/* Feedback Request Component */}
              <FeedbackRequest
                onFeedbackSubmit={handleFeedbackSubmit}
                isVisible={showFeedbackRequest}
                onDismiss={() => setShowFeedbackRequest(false)}
              />

              <div ref={messagesEndRef} />
            </div>
          </div>

          {/* Fixed Input Area at Bottom */}
          <div className="flex-shrink-0 border-t border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 p-4">
            <div className="flex space-x-2">
              <input
                type="text"
                placeholder="Type your message..."
                className="flex-1 px-4 py-3 border border-gray-300 dark:border-gray-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white dark:placeholder-gray-400"
                value={inputText}
                onChange={handleInputChange}
                onKeyDown={handleKeyDown}
              />
              <button
                className="px-6 py-3 bg-blue-500 text-white rounded-lg hover:bg-blue-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                onClick={() => handleSend()}
                disabled={!inputText.trim()}
              >
                Send
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
