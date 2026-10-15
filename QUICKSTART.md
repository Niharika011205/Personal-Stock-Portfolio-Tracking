# Quick Start Guide

Get the Stock Portfolio Tracker running in 5 minutes.

## Option 1: Docker Compose (Recommended)

### Prerequisites
- Docker and Docker Compose installed

### Run
```bash
# Start both PostgreSQL and the application
docker-compose up --build

# Wait for the application to start (look for "Started PortfolioTrackerApplication")
# Application will be available at http://localhost:8080/api
```

### Stop
```bash
docker-compose down
```

---

## Option 2: Local Development

### Prerequisites
- Java 17 JDK
- Maven 3.6+
- PostgreSQL 15

### Step 1: Start PostgreSQL

```bash
# Using Docker
docker run --name portfolio-db \
  -e POSTGRES_USER=portfolio_user \
  -e POSTGRES_PASSWORD=portfolio_password \
  -e POSTGRES_DB=portfolio_db \
  -p 5432:5432 \
  -d postgres:15
```

### Step 2: Build and Run
```bash
# Build the project
mvn clean package

# Run the application
mvn spring-boot:run

# Or run the JAR directly
java -jar target/stock-portfolio-tracker-1.0.0.jar
```

### Step 3: Verify
Application logs should show:
```
Started PortfolioTrackerApplication in X seconds
```

---

## First API Call

Once running, test the API:

### Create a User
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "investor@example.com",
    "firstName": "John",
    "lastName": "Investor",
    "password": "securePassword123"
  }'
```

Response:
```json
{
  "id": 1,
  "email": "investor@example.com",
  "firstName": "John",
  "lastName": "Investor",
  "createdAt": "2024-10-15T10:30:22",
  "updatedAt": "2024-10-15T10:30:22"
}
```

### Create a Portfolio
```bash
curl -X POST "http://localhost:8080/api/portfolios?userId=1&name=My%20Portfolio&description=My%20investments"
```

Response:
```json
{
  "id": 1,
  "name": "My Portfolio",
  "description": "My investments",
  "totalValue": 0,
  "totalCost": 0,
  "unrealizedPnL": 0,
  "returnPercentage": 0,
  "assetCount": 0
}
```

### Add a Transaction
```bash
curl -X POST "http://localhost:8080/api/transactions?portfolioId=1" \
  -H "Content-Type: application/json" \
  -d '{
    "transactionType": "BUY",
    "ticker": "AAPL",
    "quantity": 10,
    "price": 150.25,
    "transactionDate": "2024-10-15T10:30:00",
    "notes": "Apple purchase"
  }'
```

### View Portfolio
```bash
curl "http://localhost:8080/api/portfolios/1?userId=1"
```

### Generate Analytics Report
```bash
curl "http://localhost:8080/api/analytics/report/1"
```

### Get Market Briefing (AI Agent)
```bash
curl "http://localhost:8080/api/briefings/1"
```

---

## Using HTTP Client (IntelliJ/VS Code)

1. Open `examples.http` file in your IDE
2. Click "Run" next to any endpoint
3. View response in the HTTP Client window

---

## Project Structure Overview

```
src/main/java/com/portfolio/
├── controller/        REST endpoints
├── service/          Business logic
├── model/            Database entities
├── repository/       Data access
├── dto/              Request/response objects
├── config/           Spring configuration
└── exception/        Error handling
```

---

## Common Tasks

### View Application Logs
```bash
# If running with docker-compose
docker-compose logs -f app

# If running locally
# Logs appear in console
```

### Connect to Database
```bash
psql -U portfolio_user -d portfolio_db -h localhost
```

### Check Database Schema
```sql
\dt                    -- List all tables
\d users               -- Describe users table
SELECT COUNT(*) FROM users;
```

### Stop Services
```bash
# Docker Compose
docker-compose down

# Or kill individual containers
docker stop portfolio-db portfolio-app
docker rm portfolio-db portfolio-app
```

---

## Next Steps

1. **Read the Documentation**
   - [README.md](./README.md) - Feature overview
   - [DEPLOYMENT.md](./DEPLOYMENT.md) - GCP deployment
   - [examples.http](./examples.http) - API examples

2. **Explore API Endpoints**
   - Create multiple portfolios
   - Add transactions
   - Generate reports
   - Test the market briefing agent

3. **Deploy to GCP**
   - Follow [DEPLOYMENT.md](./DEPLOYMENT.md)
   - Use Cloud Run + Cloud SQL

4. **Extend Features**
   - Add real market data API
   - Implement authentication
   - Add frontend UI
   - Enhanced analytics

---

## Troubleshooting

### Port Already in Use
```bash
# Find process using port 8080
lsof -i :8080

# Kill the process
kill -9 <PID>
```

### Database Connection Error
```bash
# Verify PostgreSQL is running
docker ps | grep postgres

# Check connection
psql -U portfolio_user -d portfolio_db -h localhost -c "SELECT 1"
```

### Build Failed
```bash
# Clean and rebuild
mvn clean compile
mvn clean package

# Check Java version
java -version  # Should be 17+
```

### Application Won't Start
```bash
# Check logs for errors
# Ensure database is running and accessible
# Verify environment variables are set correctly
```

---

## Performance Notes

- First request may be slow (JIT compilation)
- Market data is cached for 15 minutes
- Portfolio calculations are efficient with indexed queries
- Analytics reports are generated on-demand

---

## Security Reminder

- This is a demo application
- Passwords are hashed with SHA-256 (use bcrypt in production)
- API is unauthenticated (add OAuth2/JWT in production)
- No HTTPS in local dev (use HTTPS in production)

---

## Support

For detailed API documentation, see [README.md](./README.md) and [examples.http](./examples.http).
