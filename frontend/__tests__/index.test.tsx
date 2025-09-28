import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import "@testing-library/jest-dom";
import Home from "../src/pages/index";

describe("Home Component", () => {
  it("renders the chat header", () => {
    render(<Home />);
    expect(screen.getByText("Somjeed Chat")).toBeInTheDocument();
  });

  it("shows empty state message when no messages", () => {
    render(<Home />);
    expect(
      screen.getByText("Start a conversation with Somjeed")
    ).toBeInTheDocument();
  });

  it("renders input and send button", () => {
    render(<Home />);
    expect(
      screen.getByPlaceholderText("Type your message...")
    ).toBeInTheDocument();
    expect(screen.getByText("Send")).toBeInTheDocument();
  });

  it("send button is disabled when input is empty", () => {
    render(<Home />);
    const sendButton = screen.getByText("Send");
    expect(sendButton).toBeDisabled();
  });

  it("enables send button when input has text", async () => {
    const user = userEvent.setup();
    render(<Home />);
    const input = screen.getByPlaceholderText("Type your message...");
    const sendButton = screen.getByText("Send");

    await user.type(input, "Hello");
    expect(sendButton).not.toBeDisabled();
  });

  it("sends message on button click", async () => {
    const user = userEvent.setup();
    render(<Home />);
    const input = screen.getByPlaceholderText("Type your message...");
    const sendButton = screen.getByText("Send");

    await user.type(input, "Hello Somjeed");
    await user.click(sendButton);

    expect(screen.getByText("Hello Somjeed")).toBeInTheDocument();
    expect(input).toHaveValue("");
    expect(sendButton).toBeDisabled();
  });

  it("sends message on Enter key", async () => {
    const user = userEvent.setup();
    render(<Home />);
    const input = screen.getByPlaceholderText("Type your message...");

    await user.type(input, "Test message{enter}");

    expect(screen.getByText("Test message")).toBeInTheDocument();
  });
});

it("shows inactivity prompt after 10s of user inactivity", async () => {
  // Clear any previous fetch mocks
  jest.useFakeTimers();
  const user = userEvent.setup({ advanceTimers: jest.advanceTimersByTime });
  render(<Home />);
  const input = screen.getByPlaceholderText("Type your message...");
  await user.type(input, "Hi there");
  await user.keyboard("{enter}");
  // Fast-forward 10 seconds
  jest.advanceTimersByTime(10000);
  expect(
    await screen.findByText("Do you need any further assistance?")
  ).toBeInTheDocument();
  jest.useRealTimers();
});

it("shows goodbye and feedback after no response to inactivity prompt", async () => {
  jest.useFakeTimers();
  const user = userEvent.setup({ advanceTimers: jest.advanceTimersByTime });
  render(<Home />);
  const input = screen.getByPlaceholderText("Type your message...");
  await user.type(input, "Test inactivity");
  await user.keyboard("{enter}");
  // Wait for inactivity prompt
  jest.advanceTimersByTime(10000);
  expect(
    await screen.findByText("Do you need any further assistance?")
  ).toBeInTheDocument();
  // Wait for goodbye/feedback
  jest.advanceTimersByTime(10000);
  expect(
    await screen.findByText("Thank you for chatting! How satisfied are you?")
  ).toBeInTheDocument();
  expect(screen.getByText("😊")).toBeInTheDocument();
  expect(screen.getByText("😐")).toBeInTheDocument();
  expect(screen.getByText("😞")).toBeInTheDocument();
  jest.useRealTimers();
});

it("sends feedback emoji and shows thank you message", async () => {
  jest.useFakeTimers();
  const user = userEvent.setup({ advanceTimers: jest.advanceTimersByTime });
  render(<Home />);
  const input = screen.getByPlaceholderText("Type your message...");
  await user.type(input, "Feedback test");
  await user.keyboard("{enter}");
  jest.advanceTimersByTime(10000); // inactivity
  jest.advanceTimersByTime(10000); // goodbye
  const happyBtn = await screen.findByText("😊");
  await user.click(happyBtn);
  expect(
    await screen.findByText("Thank you for your feedback!")
  ).toBeInTheDocument();
  jest.useRealTimers();
});
