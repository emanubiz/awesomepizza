-- Migration iniziale: creazione tabelle orders e order_items

CREATE TABLE orders (
    id UUID PRIMARY KEY,
    order_code VARCHAR(20) UNIQUE NOT NULL,
    status VARCHAR(20) NOT NULL,
    customer_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    delivery_address VARCHAR(200) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE order_items (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    pizza_name VARCHAR(100) NOT NULL,
    quantity INTEGER NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- Indici per migliorare le performance
CREATE INDEX idx_orders_order_code ON orders(order_code);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at DESC);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);

-- Commenti per documentazione
COMMENT ON TABLE orders IS 'Tabella principale degli ordini della pizzeria';
COMMENT ON TABLE order_items IS 'Articoli (pizze) di ogni ordine';
COMMENT ON COLUMN orders.version IS 'Campo per optimistic locking - previene conflitti di concorrenza';