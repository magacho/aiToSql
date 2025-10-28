-- Test Data for H2 Database

INSERT INTO customers (name, email, age, country) VALUES
    ('João Silva', 'joao@email.com', 35, 'Brazil'),
    ('Maria Santos', 'maria@email.com', 28, 'Brazil'),
    ('Pedro Costa', 'pedro@email.com', 42, 'Portugal'),
    ('Ana Oliveira', 'ana@email.com', 31, 'Brazil'),
    ('Carlos Rodrigues', 'carlos@email.com', 55, 'Brazil');

INSERT INTO products (name, price, stock, category) VALUES
    ('Laptop', 3500.00, 10, 'Electronics'),
    ('Mouse', 50.00, 100, 'Electronics'),
    ('Keyboard', 150.00, 50, 'Electronics'),
    ('Monitor', 1200.00, 20, 'Electronics'),
    ('Chair', 800.00, 15, 'Furniture');

INSERT INTO orders (customer_id, total_amount, status) VALUES
    (1, 3550.00, 'COMPLETED'),
    (1, 150.00, 'COMPLETED'),
    (2, 1200.00, 'PENDING'),
    (3, 800.00, 'COMPLETED'),
    (4, 3500.00, 'SHIPPED');
