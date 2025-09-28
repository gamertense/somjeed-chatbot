import { useState, useRef, useEffect } from "react";
import LoginForm from "../components/LoginForm";
import { useSessionStore } from "../stores/SessionStore";
import { chatService } from "../services/ChatService";

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

  // Inactivity timer state
  const inactivityTimer = useRef<NodeJS.Timeout | null>(null);
  const [showInactivityPrompt, setShowInactivityPrompt] = useState(false);

  // Goodbye and feedback state
  const [showGoodbye, setShowGoodbye] = useState(false);
  // Feedback emoji options
  const feedbackEmojis: string[] = ["😊", "😐", "😞"];
  const [loginError, setLoginError] = useState<string | null>(null);

  const { isLoggedIn, userId, login } = useSessionStore();

  const handleLogin = async (id: string) => {
    setLoginError(null);
    try {
      await fetchGreeting(id);
      login(id);
    } catch {
      setLoginError("Invalid user ID. Please check and try again.");
    }
  };

  // Goodbye timer effect (after inactivity prompt)
  useEffect(() => {
    if (!showInactivityPrompt) return;
    if (showGoodbye) return;
    const timer = setTimeout(() => {
      setShowGoodbye(true);
    }, 10000); // 10 seconds after inactivity prompt
    return () => clearTimeout(timer);
  }, [showInactivityPrompt, showGoodbye]);

  // Fetch greeting is now called after login

  // Inactivity monitoring effect
  useEffect(() => {
    // Only monitor inactivity if there is at least one user message and not already showing prompt
    if (messages.length === 0) return;
    const lastMsg = messages[messages.length - 1];
    if (lastMsg.sender !== "user") return;
    if (showInactivityPrompt) return;
    if (inactivityTimer.current) clearTimeout(inactivityTimer.current);
    inactivityTimer.current = setTimeout(() => {
      setShowInactivityPrompt(true);
    }, 10000); // 10 seconds
    return () => {
      if (inactivityTimer.current) clearTimeout(inactivityTimer.current);
    };
  }, [messages, showInactivityPrompt]);

  const fetchGreeting = async (userId: string) => {
    const data = await chatService.sendMessage("hello", null, userId);
    setSessionId(data.sessionId);
    const greetingMessage: Message = {
      id: Date.now().toString(),
      text: data.botMessage,
      sender: "bot",
      timestamp: new Date(data.timestamp),
      quickReplies: data.quickReplies,
      feedbackOptions: data.feedbackOptions?.map((option: string) => {
        if (option === "HAPPY") return "😊";
        if (option === "NEUTRAL") return "😐";
        if (option === "SAD") return "😢";
        return option;
      }),
    };
    setMessages([greetingMessage]);
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
      const userMessage: Message = {
        id: Date.now().toString(),
        text: messageText,
        sender: "user",
        timestamp: new Date(),
      };
      setMessages((prev) => [...prev, userMessage]);
      setInputText("");
      setShowInactivityPrompt(false); // Reset inactivity prompt on user send
      setShowGoodbye(false); // Reset goodbye on user send

      try {
        const data = await chatService.sendMessage(
          messageText,
          sessionId,
          userId!
        );
        setSessionId(data.sessionId);
        const botMessage: Message = {
          id: (Date.now() + 1).toString(),
          text: data.botMessage,
          sender: "bot",
          timestamp: new Date(data.timestamp),
          quickReplies: data.quickReplies,
          feedbackOptions: data.feedbackOptions?.map((option: string) => {
            if (option === "HAPPY") return "😊";
            if (option === "NEUTRAL") return "😐";
            if (option === "SAD") return "😢";
            return option;
          }),
        };
        setMessages((prev) => [...prev, botMessage]);
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

  const handleFeedback = async (option: string) => {
    // If feedback is an emoji, send to backend as feedback
    if (["😊", "😐", "😞"].includes(option)) {
      try {
        await chatService.sendFeedback(sessionId!, userId!, option);
      } catch {
        // Optionally show error or ignore
      }
      setShowGoodbye(false); // Hide feedback after sending
      setMessages((prev) => [
        ...prev,
        {
          id: Date.now().toString(),
          text: "Thank you for your feedback!",
          sender: "bot",
          timestamp: new Date(),
        },
      ]);
    } else {
      handleSend(`Feedback: ${option}`);
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  return !isLoggedIn ? (
    <LoginForm onLogin={handleLogin} errorMessage={loginError} />
  ) : (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900 flex flex-col">
      <header className="bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700 px-4 py-3 shadow-sm">
        <h1 className="text-lg font-semibold text-gray-800 dark:text-white">Somjeed Chat</h1>
      </header>
      <div className="flex-1 overflow-hidden">
        <div className="h-full overflow-y-auto flex flex-col items-center px-4 py-4">
          <div className="max-w-4xl w-full space-y-4">
        {messages.length === 0 ? (
          <div className="text-center text-gray-500 mt-8">
            Start a conversation with Somjeed
          </div>
        ) : (
          messages.map((message) => (
            <div
              key={message.id}
              className={`flex ${
                message.sender === "user" ? "justify-end" : "justify-start"
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
                  {/* Goodbye and Feedback */}
                  {showGoodbye && (
                    <div className="flex flex-col items-center mt-4 space-y-2">
                      <div className="bg-gray-100 dark:bg-gray-700 text-gray-800 dark:text-white px-4 py-2 rounded-lg shadow">
                        Thank you for chatting! How satisfied are you?
                      </div>
                      <div className="flex space-x-4 mt-2">
                        {feedbackEmojis.map((emoji: string) => (
                          <button
                            key={emoji}
                            onClick={() => handleFeedback(emoji)}
                            className="text-3xl hover:scale-110"
                          >
                            {emoji}
                          </button>
                        ))}
                      </div>
                    </div>
                  )}
                  <p className="text-xs opacity-70 mt-1">
                    {message.timestamp.toLocaleTimeString()}
                  </p>
                </div>
                {message.sender === "bot" && message.feedbackOptions && (
                  <div className="flex space-x-2 mt-2">
                    {message.feedbackOptions.map((option, index) => (
                      <button
                        key={index}
                        onClick={() => handleFeedback(option)}
                        className="text-2xl hover:scale-110"
                      >
                        {option}
                      </button>
                    ))}
                  </div>
                )}
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
        {/* Inactivity Prompt */}
        {showInactivityPrompt && (
          <div className="flex justify-center mt-4">
            <div className="bg-yellow-100 dark:bg-yellow-900 text-yellow-800 dark:text-yellow-200 px-4 py-2 rounded-lg shadow">
              Do you need any further assistance?
            </div>
          </div>
        )}
        <div ref={messagesEndRef} />
      </div>

      {/* Input Area */}
      <div className="bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg px-4 py-3 shadow-sm">
        <div className="flex space-x-2">
          <input
            type="text"
            placeholder="Type your message..."
            className="flex-1 px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white dark:placeholder-gray-400"
            value={inputText}
            onChange={(e) => setInputText(e.target.value)}
            onKeyDown={handleKeyDown}
          />
          <button
            className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 disabled:opacity-50"
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
