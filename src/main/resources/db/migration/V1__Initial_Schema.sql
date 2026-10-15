-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Portfolios table
CREATE TABLE IF NOT EXISTS portfolios (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Portfolio Assets table
CREATE TABLE IF NOT EXISTS portfolio_assets (
    id BIGSERIAL PRIMARY KEY,
    portfolio_id BIGINT NOT NULL,
    ticker VARCHAR(10) NOT NULL,
    quantity NUMERIC(15, 2) NOT NULL,
    average_purchase_price NUMERIC(15, 4) NOT NULL,
    current_price NUMERIC(15, 4) NOT NULL,
    current_value NUMERIC(20, 2) NOT NULL,
    total_cost NUMERIC(20, 2) NOT NULL,
    unrealized_pnl NUMERIC(20, 2) NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(portfolio_id, ticker),
    FOREIGN KEY (portfolio_id) REFERENCES portfolios(id) ON DELETE CASCADE
);

-- Transactions table
CREATE TABLE IF NOT EXISTS transactions (
    id BIGSERIAL PRIMARY KEY,
    portfolio_id BIGINT NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    ticker VARCHAR(10) NOT NULL,
    quantity NUMERIC(15, 2) NOT NULL,
    price NUMERIC(15, 4) NOT NULL,
    total_amount NUMERIC(20, 2) NOT NULL,
    transaction_date TIMESTAMP NOT NULL,
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (portfolio_id) REFERENCES portfolios(id) ON DELETE CASCADE
);

-- Portfolio Snapshots table (for historical tracking)
CREATE TABLE IF NOT EXISTS portfolio_snapshots (
    id BIGSERIAL PRIMARY KEY,
    portfolio_id BIGINT NOT NULL,
    snapshot_date DATE NOT NULL,
    total_value NUMERIC(20, 2) NOT NULL,
    total_cost NUMERIC(20, 2) NOT NULL,
    unrealized_pnl NUMERIC(20, 2) NOT NULL,
    asset_count INTEGER,
    return_percentage NUMERIC(10, 4),
    FOREIGN KEY (portfolio_id) REFERENCES portfolios(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_portfolios_user_id ON portfolios(user_id);
CREATE INDEX idx_portfolio_assets_portfolio_id ON portfolio_assets(portfolio_id);
CREATE INDEX idx_portfolio_assets_ticker ON portfolio_assets(ticker);
CREATE INDEX idx_transactions_portfolio_id ON transactions(portfolio_id);
CREATE INDEX idx_transactions_date ON transactions(transaction_date);
CREATE INDEX idx_portfolio_snapshots_portfolio_date ON portfolio_snapshots(portfolio_id, snapshot_date);
