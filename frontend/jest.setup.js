import "@testing-library/jest-dom";

// Mock scrollIntoView for jsdom
Object.defineProperty(HTMLElement.prototype, "scrollIntoView", {
  writable: true,
  value: jest.fn(),
});
