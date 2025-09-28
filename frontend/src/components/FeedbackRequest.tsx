import React from "react";

export type FeedbackRating = "HAPPY" | "NEUTRAL" | "SAD";

export interface FeedbackRequestProps {
  /**
   * Callback when user selects a feedback rating
   */
  onFeedbackSubmit: (rating: FeedbackRating) => void;

  /**
   * Whether the feedback request is currently visible
   */
  isVisible: boolean;

  /**
   * Optional callback when feedback is dismissed
   */
  onDismiss?: () => void;
}

const EMOJI_MAPPING = {
  HAPPY: "😊",
  NEUTRAL: "😐",
  SAD: "😞",
} as const;

const RATING_ORDER: FeedbackRating[] = ["HAPPY", "NEUTRAL", "SAD"];

export const FeedbackRequest: React.FC<FeedbackRequestProps> = ({
  onFeedbackSubmit,
  isVisible,
  onDismiss,
}) => {
  if (!isVisible) return null;

  const handleEmojiClick = (rating: FeedbackRating) => {
    onFeedbackSubmit(rating);
  };

  return (
    <div className="flex justify-center mt-4 animate-fade-in">
      <div className="bg-blue-50 dark:bg-blue-900/30 border border-blue-200 dark:border-blue-700 rounded-lg p-4 shadow-sm max-w-md">
        {/* Exact text from assignment */}
        <div className="text-center space-y-2 mb-4">
          <p className="text-gray-800 dark:text-gray-200 font-medium">
            Thanks for chatting with me today. 🙏
          </p>
          <p className="text-gray-700 dark:text-gray-300 text-sm">
            Before you go, could you rate your experience?
          </p>
        </div>

        {/* Emoji rating buttons exactly as specified: [ 😊 😐 😞 ] */}
        <div className="flex justify-center items-center space-x-4">
          <span className="text-gray-500 text-sm">[</span>
          {RATING_ORDER.map((rating, index) => (
            <React.Fragment key={rating}>
              <button
                onClick={() => handleEmojiClick(rating)}
                className="text-3xl hover:scale-110 transform transition-transform duration-200 focus:outline-none focus:ring-2 focus:ring-blue-300 dark:focus:ring-blue-600 rounded-lg p-2"
                aria-label={`Rate experience as ${rating.toLowerCase()}`}
                title={`${rating.toLowerCase()} experience`}
              >
                {EMOJI_MAPPING[rating]}
              </button>
              {index < RATING_ORDER.length - 1 && (
                <span className="text-gray-400 text-sm select-none"></span>
              )}
            </React.Fragment>
          ))}
          <span className="text-gray-500 text-sm">]</span>
        </div>

        {onDismiss && (
          <button
            onClick={onDismiss}
            className="mt-3 text-xs text-gray-500 hover:text-gray-700 dark:hover:text-gray-300 underline w-full text-center"
          >
            Skip feedback
          </button>
        )}
      </div>
    </div>
  );
};

export default FeedbackRequest;
