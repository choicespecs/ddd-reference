-- Demo data so the Vue UI has something to show on first run.
-- In a real project, keep seed/demo data in a separate location enabled only for dev
-- (e.g. spring.flyway.locations=classpath:db/migration,classpath:db/dev-data).
INSERT INTO catalog_product (id, sku, name, description, price_amount, price_currency, stock_on_hand, active, version) VALUES
('11111111-1111-1111-1111-000000000001', 'BOOK-0001', 'Domain-Driven Design',             'The "blue book" by Eric Evans.',                54.99, 'USD', 12, TRUE, 0),
('11111111-1111-1111-1111-000000000002', 'BOOK-0002', 'Implementing Domain-Driven Design', 'The "red book" by Vaughn Vernon.',              49.99, 'USD',  8, TRUE, 0),
('11111111-1111-1111-1111-000000000003', 'BOOK-0003', 'Learning Domain-Driven Design',     'Vlad Khononov''s practical introduction.',      44.50, 'USD', 20, TRUE, 0),
('11111111-1111-1111-1111-000000000004', 'MUG-0001',  'Bounded Context Mug',               'Holds exactly one ubiquitous language.',         14.00, 'USD', 30, TRUE, 0),
('11111111-1111-1111-1111-000000000005', 'STKR-0001', 'Aggregate Root Sticker Pack',       'Five stickers. Consistency boundary included.',   4.25, 'USD',  2, TRUE, 0);
