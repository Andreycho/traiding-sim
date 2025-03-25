# Trading Simulator Project
This project simulates a cryptocurrency trading platform. Users can buy and sell cryptocurrencies, view real-time prices, and track their transaction history. The platform uses real-time data from Kraken via WebSocket and stores account and transaction information in an H2 database.

## Tech Stack

### Frontend:
- React: JavaScript library for building the user interface
- Vite: Next-generation build tool for fast frontend development
- TypeScript: JavaScript superset that adds static typing
- TailwindCSS: Utility-first CSS framework for styling

### Backend:
- Spring Boot: Java framework for building the backend API
- H2 Database: In-memory database for storing data (used for development and testing)
- Kraken WebSocket API: For real-time cryptocurrency price updates


## Features
- Real-time Price Updates: Get live prices for cryptocurrencies like BTC and ETH from Kraken Websocket API.
- Buy/Sell Cryptocurrencies: Users can buy and sell cryptocurrencies.
- Transaction History: View detailed transaction history for buys and sells.
- Profit/Loss Tracking: Calculate profit or loss from all transactions.
- Account Reset: Option to reset the account balance and holdings.
- H2 Database: All account and transaction data is stored in an H2 database with persistent storage.

## Project Setup

### Prerequisites
Ensure the following are installed on your system:
- **Java 21** or **later** (for Spring Boot backend)
- **Maven** (for building the backend)
- **Node.js (v22.14.0)** and **npm** (for the frontend)
- **H2 Database** (embedded in the backend)

### Clone the repository
```bash
git clone https://github.com/Andreycho/traiding-sim.git

cd traiding-sim
```

### Backend Setup
#### 1. Go to the backend directory:
```bash
cd backend
```
#### 2. Set up dependencies:
```bash
./mvnw clean install  # For Maven
```
#### 3. Configure the **application.properties** file:
- Ensure your H2 database URL and credentials are correctly set.
- The WebSocket API URI for Kraken should be set as wss://ws.kraken.com/v2.
#### 4. Run the application through the **TraidingsimApplication.class** or start the backend server :
```bash
./mvnw spring-boot:run  # For Maven
```
#### 5. Access the H2 console at **http://localhost:8080/h2-console** to inspect the database (example credentials: **sa/password**).

### Frontend Setup
#### 1. Go to the frontend directory:
```bash
cd frontend
```
#### 2. Install dependencies:
```bash
npm install
```
#### 3. Start the frontend server:
```bash
npm run dev
```
#### 4. The frontend will be available at **http://localhost:5173**.

### Database Configuration (H2)
Persistent H2 database is used for storage with the following configuration in **application.properties**:
```
spring.datasource.url=jdbc:h2:~/test;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.defer-datasource-initialization=true
spring.jpa.hibernate.ddl-auto=update
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```
This configuration ensures that:
- The database is persistent across sessions **(DB_CLOSE_DELAY=-1)**.
- The H2 Web Console is available at **/h2-console** for browsing the database.

#### H2 Database Console
The H2 Web Console can be accessed at:
```bash
http://localhost:8080/h2-console
```
Use the following credentials to log in:
- JDBC URL: `jdbc:h2:~/test`
- Username: `sa`
- Password: `password`

### Logging
Logging is configured in the `application.properties` file:
```
logging.file.name=logs/crypto-trading.log
logging.file.path=logs
```
- **Log File Location**: The logs are stored in the `logs/crypto-trading.log` file.
- **Logging Framework**: The application uses **SLF4J** for logging.

### WebSocket Communication
The backend communicates with the Kraken API via WebSocket for real-time price updates. The WebSocket URI is set as:
```
wss://ws.kraken.com/v2
```

### Additional Configuration
- The backend supports managing transactions, balances, and cryptocurrency holdings through RESTful APIs.
- The frontend makes use of React and TailwindCSS to create an interactive and responsive UI.

## API Endpoints

### 1. Get Crypto Prices
- **Endpoint**: `/api/prices`
- **Method**: `GET`
- **Response**: A map of cryptocurrency symbols to their current prices.
- **Example**:
 ```
{
  "BTC": 38000.0,
  "ETH": 2500.0
}
```
- **Description**: Retrieves the current live prices for supported cryptocurrencies.

### 2. Buy Cryptocurrency
- **Endpoint**: `/api/crypto/buy`
- **Method**: `POST`
- **Request Params**:
    - `crypto` (required): The cryptocurrency symbol to buy (e.g., `BTC/USD`, `ETH/USD`). 
    - `amount` (required): The amount of cryptocurrency to purchase. 
- **Response**:
- **Success**: Returns a success message with the details of the purchase.
- **Error**: Returns an error message if the transaction fails (e.g., insufficient funds).
- **Example Request**:
 ```
  POST /api/buy?crypto=BTC&amount=0.5
```
- **Example Response**:
 ```json
{
  "success": true,
  "message": "Successfully bought 0.5 BTC for $19000.0"
}
```
- **Description**: Buys the specified amount of cryptocurrency.

### 3. Sell Cryptocurrency
- **Endpoint**: `/api/crypto/buy`
- **Method**: `POST`
- **Request Params**:
    - `crypto` (required): The cryptocurrency symbol to sell (e.g., `BTC/USD`, `ETH/USD`).
    - `amount` (required): The amount of cryptocurrency to sell.
- **Response**:
- **Success**: Returns a success message with the details of the sale.
- **Error**: Returns an error message if the transaction fails (e.g., insufficient funds).
- **Example Request**:
 ```
  POST /api/sell?crypto=BTC&amount=0.5
```
- **Example Response**:
 ```json
{
  "success": true,
  "message": "Successfully sold 0.5 BTC for $19000.0"
}
```
- **Description**: Sells the specified amount of cryptocurrency.


### 4. Get Transaction History
- **Endpoint**: `/api/transactions`
- **Method**: `GET`
- **Response**: A list of all transactions (both buy and sell) made by the user.
- **Example Response**:
 ```json
[
  {
    "crypto": "BTC",
    "amount": 0.5,
    "price": 38000.0,
    "total": 19000.0,
    "type": "BUY"
  },
  {
    "crypto": "BTC",
    "amount": 0.5,
    "price": 40000.0,
    "total": 20000.0,
    "type": "SELL"
  }
]
```
- **Description**: Fetches the transaction history of the account.

### 5. Get Account Balance
- **Endpoint**: `/api/balance`
- **Method**: `GET`
- **Response**: The current account balance.
- **Example Response**:
 ```json
{
  10000.0
}
```
- **Description**: Fetches the current balance of the user's account.

### 6. Get Cryptocurrency Holdings
- **Endpoint**: `/api/holdings`
- **Method**: `GET`
- **Response**: A map of cryptocurrencies and the amounts held by the user.
- **Example Response**:
 ```json
{
  "BTC": 0.5,
  "ETH": 2.0
}
```
- **Description**: Fetches the current holdings of the user's account.

### 7. Calculate Profit and Loss
- **Endpoint**: `/api/profit-loss`
- **Method**: `GET`
- **Response**: A map of the profit or loss made from all transactions for each cryptocurrency.
- **Example Response**:
 ```json
{
  "BTC": 5000.0,
  "ETH": -1000.0
}
```
- **Description**: Fetches the profit/loss based on the cryptocurrencies the user has bought.

### 8. Reset account
- **Endpoint**: `/api/reset`
- **Method**: `POST`
- **Response**: Resets the account balance and holdings to the initial state.
- **Example Response**:
 ```json
{
  "success": true,
  "message": "Account has been reset to the initial balance of $10000.0"
}
```
- **Description**: Resets the account balance, transaction history and holdings.

## Scalability Considerations
To ensure the application can handle increased traffic and data volume, the following scalability strategies can be applied:

### Backend Scalability
- **Caching**: Use Redis or Memcached to cache frequently accessed data (e.g., cryptocurrency prices) and reduce database queries.
- **Asynchronous Processing**: Use message queues like RabbitMQ or Kafka to handle high-load transactions asynchronously.
- **Microservices Architecture**: Break down the monolithic backend into microservices (e.g., separate services for trading, user management, and price fetching).

### Frontend Scalability
- **State Management**: Use Redux or React Query for better state management to optimize API calls.
- **Lazy Loading**: Implement lazy loading and code splitting with React to reduce initial load time.

### Real-time Data Scalability
- **WebSocket Optimization**: Instead of sending price updates for all currencies, allow users to subscribe to specific ones.
- **Rate Limiting**: Implement request throttling to prevent abuse and excessive WebSocket connections.
- **Horizontal Scaling for WebSockets**: Use a message broker like Redis Pub/Sub or Kafka to distribute WebSocket events across multiple servers.