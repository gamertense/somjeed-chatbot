import React from "react";
import { render, screen, fireEvent } from "@testing-library/react";
import LoginForm from "../src/components/LoginForm";

describe("LoginForm", () => {
  it("renders login form with userId input and start chat button", () => {
    render(<LoginForm onLogin={() => {}} />);

    expect(screen.getByLabelText("User ID")).toBeInTheDocument();
    expect(screen.getByText("Start Chat")).toBeInTheDocument();
    expect(
      screen.getByText("Example user IDs: user1, user2, user3")
    ).toBeInTheDocument();
  });

  it("shows error message when submitting empty userId", () => {
    render(<LoginForm onLogin={() => {}} />);

    fireEvent.click(screen.getByText("Start Chat"));

    expect(screen.getByText("User ID is required")).toBeInTheDocument();
  });

  it("calls onLogin with trimmed userId when valid", () => {
    const mockOnLogin = jest.fn();
    render(<LoginForm onLogin={mockOnLogin} />);

    const input = screen.getByLabelText("User ID");
    fireEvent.change(input, { target: { value: "  user1  " } });
    fireEvent.click(screen.getByText("Start Chat"));

    expect(mockOnLogin).toHaveBeenCalledWith("user1");
    expect(screen.queryByText("User ID is required")).not.toBeInTheDocument();
  });

  it("clears error when entering valid userId", () => {
    render(<LoginForm onLogin={() => {}} />);

    fireEvent.click(screen.getByText("Start Chat"));
    expect(screen.getByText("User ID is required")).toBeInTheDocument();

    const input = screen.getByLabelText("User ID");
    fireEvent.change(input, { target: { value: "user1" } });
    fireEvent.click(screen.getByText("Start Chat"));

    expect(screen.queryByText("User ID is required")).not.toBeInTheDocument();
  });
});
