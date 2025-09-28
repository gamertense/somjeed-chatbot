import { useRef, useEffect, useCallback } from "react";

export interface InactivityManagerProps {
  /**
   * Callback fired after 10 seconds of inactivity to show assistance prompt
   */
  onShowAssistancePrompt: () => void;

  /**
   * Callback fired after another 10 seconds if no user response to show feedback request
   */
  onShowFeedbackRequest: () => void;

  /**
   * Whether the assistance prompt is currently showing
   */
  isAssistancePromptVisible: boolean;

  /**
   * Whether the feedback request is currently visible
   */
  isFeedbackVisible: boolean;

  /**
   * Trigger to start/restart the inactivity timer after bot response
   */
  lastBotResponseTime?: number;

  /**
   * Reset trigger for any user activity
   */
  userActivityTrigger?: number;
}

export const useInactivityManager = ({
  onShowAssistancePrompt,
  onShowFeedbackRequest,
  isAssistancePromptVisible,
  isFeedbackVisible,
  lastBotResponseTime,
  userActivityTrigger,
}: InactivityManagerProps) => {
  const firstTimer = useRef<NodeJS.Timeout | null>(null);
  const secondTimer = useRef<NodeJS.Timeout | null>(null);

  const clearAllTimers = useCallback(() => {
    if (firstTimer.current) {
      clearTimeout(firstTimer.current);
      firstTimer.current = null;
    }
    if (secondTimer.current) {
      clearTimeout(secondTimer.current);
      secondTimer.current = null;
    }
  }, []);

  const startInactivitySequence = useCallback(() => {
    // Clear any existing timers
    clearAllTimers();

    // First timer: 10 seconds after bot response → assistance prompt
    firstTimer.current = setTimeout(() => {
      if (!isAssistancePromptVisible && !isFeedbackVisible) {
        onShowAssistancePrompt();

        // Second timer: 10 more seconds after assistance prompt → feedback request
        secondTimer.current = setTimeout(() => {
          if (!isFeedbackVisible) {
            onShowFeedbackRequest();
          }
        }, 10000);
      }
    }, 10000);
  }, [
    onShowAssistancePrompt,
    onShowFeedbackRequest,
    isAssistancePromptVisible,
    isFeedbackVisible,
    clearAllTimers,
  ]);

  // Effect: Start timer when bot responds (service completion)
  useEffect(() => {
    if (
      lastBotResponseTime &&
      !isAssistancePromptVisible &&
      !isFeedbackVisible
    ) {
      startInactivitySequence();
    }
  }, [
    lastBotResponseTime,
    startInactivitySequence,
    isAssistancePromptVisible,
    isFeedbackVisible,
  ]);

  // Effect: Reset timers on any user activity
  useEffect(() => {
    if (userActivityTrigger) {
      clearAllTimers();
    }
  }, [userActivityTrigger, clearAllTimers]);

  // Cleanup on unmount
  useEffect(() => {
    return clearAllTimers;
  }, [clearAllTimers]);

  return {
    clearAllTimers,
    startInactivitySequence,
  };
};

/**
 * Component version of InactivityManager for direct usage
 */
export const InactivityManager: React.FC<InactivityManagerProps> = (props) => {
  useInactivityManager(props);
  return null;
};
