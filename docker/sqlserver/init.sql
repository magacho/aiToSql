-- ============================================================================
-- SQL Server Initialization Script for MCP Server Testing
-- ============================================================================

-- Use master database to create login
USE master;
GO

-- Create login for readonly user
IF NOT EXISTS (SELECT * FROM sys.server_principals WHERE name = 'readonly_user')
BEGIN
    CREATE LOGIN readonly_user WITH PASSWORD = 'ReadOnly@Pass123!';
END;
GO

-- Switch to testdb
USE testdb;
GO

-- Create database user
IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = 'readonly_user')
BEGIN
    CREATE USER readonly_user FOR LOGIN readonly_user;
END;
GO

-- Grant SELECT permission
GRANT SELECT TO readonly_user;
GO

-- Customers table
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'customers')
BEGIN
    CREATE TABLE customers (
        id INT IDENTITY(1,1) PRIMARY KEY,
        name NVARCHAR(100) NOT NULL,
        email NVARCHAR(100) UNIQUE NOT NULL,
        phone NVARCHAR(20),
        city NVARCHAR(50),
        state NVARCHAR(2),
        created_at DATETIME DEFAULT GETDATE()
    );
    
    CREATE INDEX idx_customers_email ON customers(email);
END;
GO

-- Products table
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'products')
BEGIN
    CREATE TABLE products (
        id INT IDENTITY(1,1) PRIMARY KEY,
        name NVARCHAR(200) NOT NULL,
        description NVARCHAR(MAX),
        price DECIMAL(10, 2) NOT NULL,
        stock INT DEFAULT 0,
        category NVARCHAR(50)
    );
    
    CREATE INDEX idx_products_category ON products(category);
END;
GO

-- Orders table
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'orders')
BEGIN
    CREATE TABLE orders (
        id INT IDENTITY(1,1) PRIMARY KEY,
        customer_id INT NOT NULL,
        order_date DATETIME DEFAULT GETDATE(),
        total_amount DECIMAL(10, 2) NOT NULL,
        status NVARCHAR(20) DEFAULT 'pending',
        FOREIGN KEY (customer_id) REFERENCES customers(id)
    );
    
    CREATE INDEX idx_orders_customer_id ON orders(customer_id);
END;
GO

-- Sample data
INSERT INTO customers (name, email, phone, city, state) VALUES
('João Silva', 'joao@email.com', '(11) 98765-4321', 'São Paulo', 'SP'),
('Maria Santos', 'maria@email.com', '(21) 98765-1234', 'Rio de Janeiro', 'RJ');
GO

INSERT INTO products (name, description, price, stock, category) VALUES
('Notebook Dell', 'Notebook Dell Inspiron 15', 3499.90, 15, 'Eletrônicos'),
('Mouse Logitech', 'Mouse sem fio', 349.90, 50, 'Periféricos');
GO

INSERT INTO orders (customer_id, total_amount, status) VALUES
(1, 3849.80, 'delivered'),
(2, 1199.70, 'shipped');
GO

-- Grant SELECT permission again
GRANT SELECT TO readonly_user;
GO
