# Somjeed Chatbot

A credit card customer support chatbot built with Java Spring Boot backend and Next.js frontend. Somjeed provides contextual greetings, proactive intent prediction, and handles various customer service scenarios.

## Features

- **Contextual Greetings**: Time-based and weather-aware greeting system
- **Proactive Intent Prediction**: Predicts customer needs based on account status and transaction history
- **Intent Detection**: Handles 5+ different customer service intents
- **Mock User Scenarios**: Pre-configured test users for different customer situations
- **Satisfaction Evaluation**: Post-conversation feedback system

## Quick Start

### Prerequisites

- Docker and Docker Compose
- Git

### Running the Application

1. Clone the repository:

```bash
git clone <repository-url>
cd somjeed-chatbot
```

2. Start the application using Docker Compose:

```bash
docker-compose up
```

3. Access the application:
   - **Frontend**: http://localhost:3000
   - **Backend API**: http://localhost:8080

The application will automatically build and start both the backend (Spring Boot) and frontend (Next.js) services.

## Mock Users for Testing

The application includes several pre-configured mock users to test different customer scenarios. Use these user IDs when testing the chatbot functionality:

### Regular Users

| User ID   | Card Number                 | Current Balance | Payment Status | Description                              |
| --------- | --------------------------- | --------------- | -------------- | ---------------------------------------- |
| `user123` | \***\*-\*\***-\*\*\*\*-1234 | ฿1,250.00       | CURRENT        | Regular user with current payment status |
| `user456` | \***\*-\*\***-\*\*\*\*-5678 | ฿2,850.75       | OVERDUE        | User with overdue payment                |
| `user789` | \***\*-\*\***-\*\*\*\*-9012 | ฿456.30         | UPCOMING       | User with upcoming payment due           |

### Scenario-Specific Users (Intent Prediction Testing)

These users are designed to test the proactive intent prediction system:

#### 1. Overdue Payment Scenario

- **User ID**: `user_overdue`
- **Card Number**: \***\*-\*\***-\*\*\*\*-4321
- **Current Balance**: ฿120,000.00
- **Payment Status**: OVERDUE (due September 1, 2025)
- **Scenario**: Triggers payment inquiry intent prediction
- **Expected Bot Behavior**: Proactively asks about checking outstanding balance

#### 2. Recent Payment Scenario

- **User ID**: `user_recent_payment`
- **Card Number**: \***\*-\*\***-\*\*\*\*-8765
- **Current Balance**: ฿2,500.00
- **Payment Status**: CURRENT (payment received today)
- **Scenario**: Triggers credit balance inquiry intent prediction
- **Expected Bot Behavior**: Offers to show updated available credit

#### 3. Duplicate Transaction Scenario

- **User ID**: `user_duplicate_txn`
- **Card Number**: \***\*-\*\***-\*\*\*\*-3456
- **Current Balance**: ฿5,678.90
- **Payment Status**: CURRENT
- **Scenario**: Has suspicious duplicate transactions (฿2,890 x2 within 48 hours)
- **Expected Bot Behavior**: Proactively asks about potential duplicate transactions

## Testing Different Scenarios

### Greeting System

The chatbot provides contextual greetings based on:

- **Time of day**: Good morning (5:00-11:59), Good afternoon (12:00-16:59), Good evening (17:00+)
- **Weather conditions**: Sunny, Cloudy, Rainy, Stormy (mocked data)

### Intent Prediction Testing

1. Use scenario-specific user IDs to test proactive intent prediction
2. The bot will automatically detect user context after greeting
3. Test different conversation flows based on predicted intents

### Intent Detection

The system supports these intents:

- Payment Inquiry
- E-Statement Request
- Transaction Dispute
- Credit Balance Check
- General Support

### Satisfaction Evaluation

- After 10 seconds of inactivity, the bot asks if further assistance is needed
- After another 10 seconds, it initiates the goodbye sequence with satisfaction rating

## API Endpoints

Key backend endpoints for testing:

- `GET /api/chat/greeting/{userId}` - Get contextual greeting
- `POST /api/chat/message` - Send message to chatbot
- `GET /api/users/{userId}` - Get user information
- `GET /api/users/{userId}/transactions` - Get user transactions
- `POST /api/feedback` - Submit satisfaction feedback

## Development

### Backend (Spring Boot + Maven)

```bash
cd backend
./mvnw spring-boot:run
```

### Frontend (Next.js)

```bash
cd frontend
npm install
npm run dev
```

### Running Tests

```bash
# Backend tests
cd backend
./mvnw test

# Frontend tests
cd frontend
npm test
```

## Architecture

The application follows a microservices architecture:

- **Frontend**: Next.js React application with TypeScript
- **Backend**: Spring Boot REST API with Java
- **Data Layer**: Mock data service for user and transaction data
- **Communication**: RESTful API calls between frontend and backend
