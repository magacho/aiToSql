-- ============================================================================
-- PostgreSQL Initialization Script for MCP Server Testing
-- ============================================================================

-- Create readonly user for MCP Server
CREATE USER readonly_user WITH PASSWORD 'readonly_password';
GRANT CONNECT ON DATABASE testdb TO readonly_user;
GRANT USAGE ON SCHEMA public TO readonly_user;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO readonly_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO readonly_user;

-- Customers table
CREATE TABLE customers (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20),
    city VARCHAR(50),
    state VARCHAR(2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_customers_email ON customers(email);

-- Products table
CREATE TABLE products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    stock INTEGER DEFAULT 0,
    category VARCHAR(50)
);

CREATE INDEX idx_products_category ON products(category);

-- Orders table
CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    customer_id INTEGER NOT NULL,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) DEFAULT 'pending',
    FOREIGN KEY (customer_id) REFERENCES customers(id)
);

CREATE INDEX idx_orders_customer_id ON orders(customer_id);

-- Sample data
INSERT INTO customers (name, email, phone, city, state) VALUES
('João Silva', 'joao@email.com', '(11) 98765-4321', 'São Paulo', 'SP'),
('Maria Santos', 'maria@email.com', '(21) 98765-1234', 'Rio de Janeiro', 'RJ');

INSERT INTO products (name, description, price, stock, category) VALUES
('Notebook Dell', 'Notebook Dell Inspiron 15', 3499.90, 15, 'Eletrônicos'),
('Mouse Logitech', 'Mouse sem fio', 349.90, 50, 'Periféricos');

INSERT INTO orders (customer_id, total_amount, status) VALUES
(1, 3849.80, 'delivered'),
(2, 1199.70, 'shipped');

GRANT SELECT ON ALL TABLES IN SCHEMA public TO readonly_user;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO readonly_user;
