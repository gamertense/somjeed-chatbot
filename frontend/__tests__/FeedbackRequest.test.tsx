import React from "react";
import { render, screen, fireEvent } from "@testing-library/react";
import "@testing-library/jest-dom";
import FeedbackRequest, {
  FeedbackRating,
} from "../src/components/FeedbackRequest";

describe("FeedbackRequest", () => {
  let mockOnFeedbackSubmit: jest.Mock<void, [FeedbackRating]>;
  let mockOnDismiss: jest.Mock;

  beforeEach(() => {
    mockOnFeedbackSubmit = jest.fn();
    mockOnDismiss = jest.fn();
  });

  it("should render feedback request when visible", () => {
    render(
      <FeedbackRequest
        onFeedbackSubmit={mockOnFeedbackSubmit}
        isVisible={true}
        onDismiss={mockOnDismiss}
      />
    );

    expect(
      screen.getByText("Thanks for chatting with me today. 🙏")
    ).toBeInTheDocument();
    expect(
      screen.getByText("Before you go, could you rate your experience?")
    ).toBeInTheDocument();

    // Check emoji buttons are present
    expect(
      screen.getByLabelText("Rate experience as happy")
    ).toBeInTheDocument();
    expect(
      screen.getByLabelText("Rate experience as neutral")
    ).toBeInTheDocument();
    expect(screen.getByLabelText("Rate experience as sad")).toBeInTheDocument();
  });

  it("should not render when not visible", () => {
    const { container } = render(
      <FeedbackRequest
        onFeedbackSubmit={mockOnFeedbackSubmit}
        isVisible={false}
        onDismiss={mockOnDismiss}
      />
    );

    expect(container.firstChild).toBeNull();
  });

  it("should call onFeedbackSubmit with HAPPY when happy emoji clicked", () => {
    render(
      <FeedbackRequest
        onFeedbackSubmit={mockOnFeedbackSubmit}
        isVisible={true}
        onDismiss={mockOnDismiss}
      />
    );

    const happyButton = screen.getByLabelText("Rate experience as happy");
    fireEvent.click(happyButton);

    expect(mockOnFeedbackSubmit).toHaveBeenCalledWith("HAPPY");
    expect(mockOnFeedbackSubmit).toHaveBeenCalledTimes(1);
  });

  it("should call onFeedbackSubmit with NEUTRAL when neutral emoji clicked", () => {
    render(
      <FeedbackRequest
        onFeedbackSubmit={mockOnFeedbackSubmit}
        isVisible={true}
        onDismiss={mockOnDismiss}
      />
    );

    const neutralButton = screen.getByLabelText("Rate experience as neutral");
    fireEvent.click(neutralButton);

    expect(mockOnFeedbackSubmit).toHaveBeenCalledWith("NEUTRAL");
    expect(mockOnFeedbackSubmit).toHaveBeenCalledTimes(1);
  });

  it("should call onFeedbackSubmit with SAD when sad emoji clicked", () => {
    render(
      <FeedbackRequest
        onFeedbackSubmit={mockOnFeedbackSubmit}
        isVisible={true}
        onDismiss={mockOnDismiss}
      />
    );

    const sadButton = screen.getByLabelText("Rate experience as sad");
    fireEvent.click(sadButton);

    expect(mockOnFeedbackSubmit).toHaveBeenCalledWith("SAD");
    expect(mockOnFeedbackSubmit).toHaveBeenCalledTimes(1);
  });

  it("should call onDismiss when skip feedback is clicked", () => {
    render(
      <FeedbackRequest
        onFeedbackSubmit={mockOnFeedbackSubmit}
        isVisible={true}
        onDismiss={mockOnDismiss}
      />
    );

    const skipButton = screen.getByText("Skip feedback");
    fireEvent.click(skipButton);

    expect(mockOnDismiss).toHaveBeenCalledTimes(1);
  });

  it("should not render skip button when onDismiss is not provided", () => {
    render(
      <FeedbackRequest
        onFeedbackSubmit={mockOnFeedbackSubmit}
        isVisible={true}
      />
    );

    expect(screen.queryByText("Skip feedback")).not.toBeInTheDocument();
  });

  it("should display exact assignment text formatting", () => {
    render(
      <FeedbackRequest
        onFeedbackSubmit={mockOnFeedbackSubmit}
        isVisible={true}
        onDismiss={mockOnDismiss}
      />
    );

    // Check exact message text from assignment
    expect(
      screen.getByText("Thanks for chatting with me today. 🙏")
    ).toBeInTheDocument();
    expect(
      screen.getByText("Before you go, could you rate your experience?")
    ).toBeInTheDocument();

    // Check emoji order: 😊 😐 😞
    const buttons = screen
      .getAllByRole("button")
      .filter((button) =>
        button.getAttribute("aria-label")?.includes("Rate experience")
      );
    expect(buttons).toHaveLength(3);
    expect(buttons[0].textContent).toBe("😊");
    expect(buttons[1].textContent).toBe("😐");
    expect(buttons[2].textContent).toBe("😞");
  });

  it("should have proper accessibility attributes", () => {
    render(
      <FeedbackRequest
        onFeedbackSubmit={mockOnFeedbackSubmit}
        isVisible={true}
        onDismiss={mockOnDismiss}
      />
    );

    const happyButton = screen.getByLabelText("Rate experience as happy");
    const neutralButton = screen.getByLabelText("Rate experience as neutral");
    const sadButton = screen.getByLabelText("Rate experience as sad");

    expect(happyButton).toHaveAttribute("title", "happy experience");
    expect(neutralButton).toHaveAttribute("title", "neutral experience");
    expect(sadButton).toHaveAttribute("title", "sad experience");
  });
});
