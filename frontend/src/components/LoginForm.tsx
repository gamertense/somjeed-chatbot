import React, { useState } from "react";

interface LoginFormProps {
  onLogin: (userId: string) => void;
  errorMessage?: string | null;
}

const LoginForm: React.FC<LoginFormProps> = ({ onLogin, errorMessage }) => {
  const [userId, setUserId] = useState("");
  const [error, setError] = useState("");

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (userId.trim() === "") {
      setError("User ID is required");
      return;
    }
    setError("");
    onLogin(userId.trim());
  };

  return (
    <div className="flex flex-col h-screen bg-gray-50 dark:bg-gray-900">
      <header className="bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700 px-4 py-3 shadow-sm">
        <h1 className="text-lg font-semibold text-gray-800 dark:text-white">
          Login to Somjeed Chat
        </h1>
      </header>
      <div className="flex-1 flex items-center justify-center px-4">
        <div className="w-full max-w-md bg-white dark:bg-gray-800 p-6 rounded-lg shadow-md">
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label
                htmlFor="userId"
                className="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-1"
              >
                User ID
              </label>
              <input
                type="text"
                id="userId"
                value={userId}
                onChange={(e) => setUserId(e.target.value)}
                placeholder="Enter your user ID (e.g., user1)"
                className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white dark:placeholder-gray-400"
              />
              {error && <p className="text-red-500 text-sm mt-1">{error}</p>}
              {errorMessage && (
                <p className="text-red-500 text-sm mt-1">{errorMessage}</p>
              )}
            </div>
            <button
              type="submit"
              className="w-full px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600"
            >
              Start Chat
            </button>
          </form>
          <div className="mt-4 text-sm text-gray-600 dark:text-gray-400">
            <p>Example user IDs: user1, user2, user3</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default LoginForm;
