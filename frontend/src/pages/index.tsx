import { useState, useRef, useEffect } from "react";

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

  useEffect(() => {
    if (
      !sessionId &&
      messages.length === 0 &&
      process.env.NODE_ENV !== "test"
    ) {
      fetchGreeting();
    }
  }, [sessionId, messages.length]);

  const fetchGreeting = async () => {
    try {
      const res = await fetch("http://localhost:8080/api/v1/chat", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ message: "hello", userId: "demo-user" }),
      });
      if (!res.ok) throw new Error();
      const data = await res.json();
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
    } catch {
      const errorMessage: Message = {
        id: Date.now().toString(),
        text: "Unable to connect to chatbot service.",
        sender: "bot",
        timestamp: new Date(),
      };
      setMessages([errorMessage]);
    }
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

      try {
        const res = await fetch("http://localhost:8080/api/v1/chat", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            message: messageText,
            sessionId,
            userId: "demo-user",
          }),
        });
        if (!res.ok) throw new Error();
        const data = await res.json();
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

  const handleFeedback = (option: string) => {
    handleSend(`Feedback: ${option}`);
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  return (
    <div className="flex flex-col h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white border-b border-gray-200 px-4 py-3 shadow-sm">
        <h1 className="text-lg font-semibold text-gray-800">Somjeed Chat</h1>
      </header>

      {/* Messages Area */}
      <div className="flex-1 overflow-y-auto px-4 py-4 space-y-4">
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
                      : "bg-white text-gray-800 border border-gray-200"
                  }`}
                >
                  <p className="text-sm">{message.text}</p>
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
                        className="px-3 py-1 bg-gray-100 text-gray-800 rounded-full text-sm hover:bg-gray-200"
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
        <div ref={messagesEndRef} />
      </div>

      {/* Input Area */}
      <div className="bg-white border-t border-gray-200 px-4 py-3">
        <div className="flex space-x-2">
          <input
            type="text"
            placeholder="Type your message..."
            className="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
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
  );
}
