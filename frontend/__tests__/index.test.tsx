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
