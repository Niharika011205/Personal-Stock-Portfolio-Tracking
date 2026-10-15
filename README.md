# Stock Portfolio Tracker & Analytics

A full-stack personal stock portfolio tracking and analytics application built with Spring Boot, PostgreSQL, and deployed on Google Cloud Platform.

## Features

### 📊 Core Features
- **User Management**: Create and manage user accounts with secure password hashing
- **Portfolio Management**: Create and manage multiple investment portfolios
- **Transaction Tracking**: Log buy/sell/dividend transactions with detailed records
- **Real-Time Valuation**: Calculate portfolio value, cost basis, and P&L in real-time
- **Asset Allocation**: Track individual stock positions and performance metrics
- **Market Data Integration**: Mock market data service with cache support (ready for real API integration)
- **Historical Snapshots**: Daily portfolio snapshots for historical analysis

### 📈 Analytics & Reporting
- **Performance Reports**: Comprehensive portfolio analytics including:
  - Total portfolio value and cost basis
  - Unrealized P&L and return percentage
  - Asset allocation breakdown
  - Top and worst performers
  - Average cost per asset
- **Portfolio History**: Track performance over customizable time periods
- **Transaction Reports**: Query transactions by date range

### 🤖 AI/Agent Features
- **Market Briefing Agent**: AI-powered portfolio analysis providing:
  - Portfolio overview and performance summary
  - Risk assessment (LOW/MODERATE/HIGH)
  - Identified key risks and concerns
  - Actionable recommendations for portfolio optimization
  - Diversification analysis
  - Concentration risk alerts

## Technology Stack

- **Backend**: Spring Boot 3.2.0
- **Language**: Java 17
- **Database**: PostgreSQL 15
- **ORM**: Spring Data JPA with Hibernate
- **Migration**: Flyway
- **Cloud**: Google Cloud Platform (Cloud Run, Cloud SQL)
- **Build**: Maven
- **Containerization**: Docker

## Project Structure

```
stock-portfolio-tracker/
├── src/main/
│   ├── java/com/portfolio/
│   │   ├── config/          # Spring configurations
│   │   ├── controller/      # REST API endpoints
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── exception/       # Global exception handling
│   │   ├── model/           # JPA entity models
│   │   ├── repository/      # Data access layer
│   │   ├── service/         # Business logic layer
│   │   └── PortfolioTrackerApplication.java
│   └── resources/
│       ├── application.yml
│       ├── application-gcp.yml
│       └── db/migration/    # Flyway migration scripts
├── pom.xml
├── Dockerfile
├── cloudbuild.yaml
├── DEPLOYMENT.md            # Deployment guide
└── README.md
```

## Getting Started

### Prerequisites
- Java 17 JDK
- Maven 3.6+
- PostgreSQL 15 (or Docker)
- Docker (for containerization)

### Local Development

1. **Start PostgreSQL**
```bash
docker run --name portfolio-db \
  -e POSTGRES_USER=portfolio_user \
  -e POSTGRES_PASSWORD=portfolio_password \
  -e POSTGRES_DB=portfolio_db \
  -p 5432:5432 \
  -d postgres:15
```

2. **Build and Run**
```bash
mvn clean package
mvn spring-boot:run
```

3. **Access API**
- Base URL: `http://localhost:8080/api`
- API is ready for requests

### GCP Deployment

See [DEPLOYMENT.md](./DEPLOYMENT.md) for complete Cloud Run + Cloud SQL setup instructions.

## API Documentation

### Authentication
Currently, the API is unauthenticated. In production, implement OAuth2 or JWT authentication.

### User Endpoints

**Create User**
```http
POST /api/users
Content-Type: application/json

{
  "email": "investor@example.com",
  "firstName": "John",
  "lastName": "Investor",
  "password": "securePassword123"
}
```

**Get User**
```http
GET /api/users/{userId}
GET /api/users/email/{email}
```

### Portfolio Endpoints

**Create Portfolio**
```http
POST /api/portfolios?userId=1&name=My%20Portfolio&description=Personal%20investments
```

**Get User Portfolios**
```http
GET /api/portfolios?userId=1
```

**Get Portfolio Details**
```http
GET /api/portfolios/{portfolioId}?userId=1
```

**Refresh Portfolio Prices**
```http
PUT /api/portfolios/{portfolioId}/refresh-prices?userId=1
```

### Transaction Endpoints

**Create Transaction**
```http
POST /api/transactions?portfolioId=1
Content-Type: application/json

{
  "transactionType": "BUY",
  "ticker": "AAPL",
  "quantity": 10,
  "price": 150.25,
  "transactionDate": "2024-10-15T14:30:00",
  "notes": "Initial purchase"
}
```

**Get Portfolio Transactions**
```http
GET /api/transactions?portfolioId=1
```

**Get Transactions by Date Range**
```http
GET /api/transactions/by-date-range?portfolioId=1&startDate=2024-01-01T00:00:00&endDate=2024-12-31T23:59:59
```

### Analytics Endpoints

**Generate Portfolio Report**
```http
GET /api/analytics/report/{portfolioId}
```

Response includes:
- Portfolio totals and returns
- Asset allocation breakdown
- Top and worst performers
- Transaction count and average costs

**Get Portfolio History**
```http
GET /api/analytics/history/{portfolioId}?days=30
```

### Market Briefing Endpoint (AI Agent)

**Generate Market Briefing**
```http
GET /api/briefings/{portfolioId}
```

Response includes:
- Portfolio overview summary
- Risk assessment
- Key identified risks
- Actionable recommendations

Example Response:
```json
{
  "portfolioId": 1,
  "briefingSummary": "Portfolio Overview: Your portfolio holds 5 assets with a total value of $25,000. Overall return: +15.50% ($3,500). Top performers: AAPL (+45.20%), MSFT (+25.10%). Underperformers: TSM (-5.20%).",
  "portfolioRiskAssessment": "MODERATE - Balanced risk profile.",
  "keyRisks": [
    "High concentration: AAPL represents >30% of portfolio",
    "Limited diversification: Only 5 unique positions"
  ],
  "opportunityRecommendations": [
    "Consider rebalancing: Top position exceeds 40% allocation",
    "Increase portfolio diversification by adding 2-3 more positions"
  ],
  "generatedAt": "2024-10-15T14:35:22.123456"
}
```

## Data Models

### Users
- Email (unique)
- First/Last Name
- Password (SHA-256 hashed)
- Timestamps

### Portfolios
- User reference
- Name and description
- Multiple assets
- Multiple transactions
- Auto-calculated totals

### Portfolio Assets
- Ticker symbol
- Quantity held
- Average purchase price
- Current market price
- Total cost and current value
- Unrealized P&L
- Return percentage

### Transactions
- Type (BUY, SELL, DIVIDEND)
- Ticker and quantity
- Purchase price
- Transaction date
- Optional notes

### Portfolio Snapshots
- Daily portfolio value snapshot
- Total cost and unrealized P&L
- Asset count
- Return percentage

## Configuration

### Application Properties

**Development (application.yml)**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/portfolio_db
    username: portfolio_user
    password: portfolio_password
```

**Production (application-gcp.yml)**
- Uses Cloud SQL connection string
- Reads password from environment variables
- Reduced logging level

## Performance Features

- **Database Indexing**: Optimized indexes on foreign keys and frequently queried columns
- **Caching**: Market data cached for 15 minutes to reduce API calls
- **Connection Pooling**: HikariCP with configurable pool size
- **Lazy Loading**: JPA relationships configured for efficient queries

## Security Considerations

- Password hashing using SHA-256
- SQL injection prevention via parameterized queries (JPA)
- Input validation on all endpoints
- Global exception handling with safe error messages
- CORS configurable for production

### Production Recommendations

1. Implement OAuth2/JWT authentication
2. Use HTTPS/TLS for all communication
3. Store secrets in Google Secret Manager
4. Enable Cloud SQL SSL
5. Implement rate limiting on API endpoints
6. Add request logging and audit trails
7. Use Cloud Run private endpoints when appropriate

## Monitoring & Maintenance

### Logs
- Structured logging with SLF4J
- Different log levels per component
- Cloud Logging integration for GCP deployment

### Metrics
- Application health can be monitored via Cloud Monitoring
- Database performance tracked in Cloud SQL console
- Cloud Run metrics available in GCP dashboard

### Maintenance
- Regular database backups (Cloud SQL automated backups)
- Flyway migration versioning for schema changes
- Dependency updates via Maven

## Future Enhancements

- Real-time stock price integration (Alpha Vantage, IEX Cloud)
- WebSocket support for real-time portfolio updates
- Advanced charting and visualization
- Tax lot tracking for capital gains reporting
- Portfolio comparison benchmarks
- Machine learning predictions
- Mobile app integration
- Dividend tracking and reinvestment
- Options trading support
- Multi-currency portfolio support

## License

MIT License

## Support

For issues or questions:
1. Check the documentation
2. Review the API endpoint examples
3. Check application logs in GCP Cloud Logging
4. Verify database connectivity and migrations

## Database Schema Diagram

```
users (1) ──────── (n) portfolios
                        │
                        ├─── (n) portfolio_assets
                        │
                        ├─── (n) transactions
                        │
                        └─── (n) portfolio_snapshots
```
