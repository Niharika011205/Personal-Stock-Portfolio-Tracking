# Stock Portfolio Tracker - Deployment Guide

## Prerequisites

- Google Cloud Project with billing enabled
- `gcloud` CLI installed and configured
- Docker installed locally
- Maven 3.6+ and Java 17 JDK

## Local Development Setup

### 1. Database Setup (PostgreSQL)

```bash
# Using Docker
docker run --name portfolio-db \
  -e POSTGRES_USER=portfolio_user \
  -e POSTGRES_PASSWORD=portfolio_password \
  -e POSTGRES_DB=portfolio_db \
  -p 5432:5432 \
  -d postgres:15

# Verify connection
psql -U portfolio_user -d portfolio_db -h localhost
```

### 2. Run Locally

```bash
# Build the project
mvn clean package

# Run Spring Boot application
mvn spring-boot:run

# Application will be available at http://localhost:8080/api
```

## GCP Deployment (Cloud Run + Cloud SQL)

### 1. Create Cloud SQL Instance

```bash
# Set your GCP project
gcloud config set project YOUR_PROJECT_ID

# Create Cloud SQL PostgreSQL instance
gcloud sql instances create portfolio-db \
  --database-version POSTGRES_15 \
  --tier db-f1-micro \
  --region us-central1 \
  --availability-type REGIONAL

# Create database
gcloud sql databases create portfolio_db \
  --instance=portfolio-db

# Create user
gcloud sql users create portfolio_user \
  --instance=portfolio-db \
  --password=SECURE_PASSWORD

# Get the instance connection name (format: project:region:instance)
gcloud sql instances describe portfolio-db --format='value(connectionName)'
```

### 2. Build and Push Docker Image

```bash
# Enable Container Registry API
gcloud services enable containerregistry.googleapis.com

# Build the Docker image
gcloud builds submit --tag gcr.io/YOUR_PROJECT_ID/stock-portfolio-tracker:latest

# Verify the image
gcloud container images list
```

### 3. Deploy to Cloud Run

```bash
# Enable Cloud Run API
gcloud services enable run.googleapis.com

# Set environment variables
export SQL_CONNECTION_NAME="your-project:us-central1:portfolio-db"
export DB_PASSWORD="your-secure-password"

# Deploy to Cloud Run
gcloud run deploy stock-portfolio-tracker \
  --image gcr.io/YOUR_PROJECT_ID/stock-portfolio-tracker:latest \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --set-env-vars=\
SPRING_DATASOURCE_URL=jdbc:postgresql://cloudsql/portfolio_db,\
SPRING_DATASOURCE_USERNAME=portfolio_user,\
SPRING_DATASOURCE_PASSWORD=$DB_PASSWORD,\
SPRING_CLOUD_SQL_INSTANCES=$SQL_CONNECTION_NAME \
  --memory=512Mi \
  --cpu=1 \
  --timeout=3600 \
  --add-cloudsql-instances=$SQL_CONNECTION_NAME

# Get the service URL
gcloud run services describe stock-portfolio-tracker \
  --platform managed \
  --region us-central1 \
  --format='value(status.url)'
```

### 4. Configure Cloud SQL Proxy (if needed locally)

```bash
# Install Cloud SQL Proxy
curl -o cloud_sql_proxy https://dl.google.com/cloudsql/cloud_sql_proxy.linux.amd64
chmod +x cloud_sql_proxy

# Connect to Cloud SQL
./cloud_sql_proxy -instances=YOUR_PROJECT_ID:us-central1:portfolio-db=tcp:5432 &

# Update application.yml to use localhost:5432
```

## Database Migration

### Flyway Migrations

Migrations are automatically applied when the application starts. Place migration scripts in:
```
src/main/resources/db/migration/
```

Naming convention: `V1__description.sql`, `V2__description.sql`

## API Endpoints

### Users
- `POST /api/users` - Create new user
- `GET /api/users/{userId}` - Get user by ID
- `GET /api/users/email/{email}` - Get user by email

### Portfolios
- `POST /api/portfolios?userId=X&name=Portfolio&description=...` - Create portfolio
- `GET /api/portfolios?userId=X` - Get all user portfolios
- `GET /api/portfolios/{portfolioId}?userId=X` - Get specific portfolio
- `PUT /api/portfolios/{portfolioId}/refresh-prices?userId=X` - Refresh asset prices

### Transactions
- `POST /api/transactions?portfolioId=X` - Create transaction
- `GET /api/transactions?portfolioId=X` - Get portfolio transactions
- `GET /api/transactions/by-date-range?portfolioId=X&startDate=...&endDate=...` - Get transactions by date range

### Analytics
- `GET /api/analytics/report/{portfolioId}` - Generate analytics report
- `GET /api/analytics/history/{portfolioId}?days=30` - Get portfolio history

### Market Briefing (AI Agent)
- `GET /api/briefings/{portfolioId}` - Generate AI market briefing

## Monitoring & Logging

### Cloud Logging
```bash
# View application logs
gcloud logging read "resource.type=cloud_run_revision AND resource.labels.service_name=stock-portfolio-tracker" \
  --limit 50 \
  --format json
```

### Cloud Monitoring
- Set up alerts for Cloud Run service errors
- Monitor Cloud SQL instance metrics (CPU, memory, connections)

## Scaling Considerations

### Cloud Run
- Automatically scales based on incoming requests
- Adjust memory and CPU as needed
- Set minimum and maximum instances

```bash
gcloud run deploy stock-portfolio-tracker \
  --update-env-vars=... \
  --min-instances=1 \
  --max-instances=100
```

### Cloud SQL
- Monitor connection pool usage
- Consider read replicas for high-traffic scenarios
- Implement query optimization for large datasets

## Security Best Practices

1. **Cloud SQL Proxy**: Use Cloud SQL Proxy for secure connections
2. **Secrets Manager**: Store sensitive data in Google Secret Manager
   ```bash
   gcloud secrets create db-password --data-file=- <<< "YOUR_PASSWORD"
   ```
3. **IAM Roles**: Configure least-privilege IAM roles
4. **Network**: Use VPC connectors for private Cloud SQL access
5. **API Authentication**: Implement API key or OAuth2 authentication

## Cost Optimization

- Use `db-f1-micro` instance tier for development
- Set up budget alerts in GCP console
- Monitor Cloud Run execution time and memory usage
- Consider shared-cpu instances during off-peak hours

## Troubleshooting

### Connection Issues
```bash
# Test Cloud SQL connectivity
gcloud sql connect portfolio-db --user=portfolio_user

# Check Cloud Run environment variables
gcloud run services describe stock-portfolio-tracker --platform managed
```

### Database Migration Failures
- Check Flyway migration files syntax
- Verify database permissions
- Review application logs for detailed error messages

### High Memory Usage
- Review query performance
- Optimize database indexes
- Consider pagination for large datasets

## Backup & Recovery

### Cloud SQL Backups
```bash
# Enable automated backups (default: once per day)
gcloud sql backups create --instance=portfolio-db

# List backups
gcloud sql backups list --instance=portfolio-db

# Restore from backup
gcloud sql backups restore BACKUP_ID --backup-instance=portfolio-db
```

## Performance Optimization

1. Add database indexes on frequently queried columns (already included in schema)
2. Implement caching for market data (MarketDataService has 15-min cache)
3. Use connection pooling (HikariCP configured)
4. Paginate large result sets in list endpoints
5. Consider async processing for heavy analytics queries
