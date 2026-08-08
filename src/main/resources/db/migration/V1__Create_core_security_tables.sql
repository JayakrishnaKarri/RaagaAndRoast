-- ================================================================
-- RaagaAndRoast Database Schema - Core Tables
-- Migration: V1__Create_core_security_tables.sql
-- Description: Creates the foundational security and user management tables
-- ================================================================

-- ================================================================
-- Users Table
-- ================================================================
CREATE TABLE users (
    id CHAR(36) NOT NULL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
    credentials_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    
    INDEX idx_users_username (username),
    INDEX idx_users_email (email),
    INDEX idx_users_enabled (enabled),
    INDEX idx_users_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- Roles Table
-- ================================================================
CREATE TABLE roles (
    id CHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_roles_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- Permissions Table
-- ================================================================
CREATE TABLE permissions (
    id CHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    resource VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_permissions_name (name),
    INDEX idx_permissions_resource (resource),
    INDEX idx_permissions_action (action),
    INDEX idx_permissions_resource_action (resource, action)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- User Roles Junction Table (Many-to-Many)
-- ================================================================
CREATE TABLE user_roles (
    user_id CHAR(36) NOT NULL,
    role_id CHAR(36) NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_by CHAR(36),
    
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_by) REFERENCES users(id) ON DELETE SET NULL,
    
    INDEX idx_user_roles_user_id (user_id),
    INDEX idx_user_roles_role_id (role_id),
    INDEX idx_user_roles_assigned_at (assigned_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- Role Permissions Junction Table (Many-to-Many)
-- ================================================================
CREATE TABLE role_permissions (
    role_id CHAR(36) NOT NULL,
    permission_id CHAR(36) NOT NULL,
    granted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    granted_by CHAR(36),
    
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE,
    FOREIGN KEY (granted_by) REFERENCES users(id) ON DELETE SET NULL,
    
    INDEX idx_role_permissions_role_id (role_id),
    INDEX idx_role_permissions_permission_id (permission_id),
    INDEX idx_role_permissions_granted_at (granted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- Customers Table
-- ================================================================
CREATE TABLE customers (
    id CHAR(36) NOT NULL PRIMARY KEY,
    user_id CHAR(36) NOT NULL UNIQUE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    phone_number VARCHAR(20),
    date_of_birth DATE,
    preferences VARCHAR(1000),
    marketing_consent BOOLEAN NOT NULL DEFAULT FALSE,
    cart_id CHAR(36),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    INDEX idx_customers_user_id (user_id),
    INDEX idx_customers_phone (phone_number),
    INDEX idx_customers_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- Addresses Table
-- ================================================================
CREATE TABLE addresses (
    id CHAR(36) NOT NULL PRIMARY KEY,
    customer_id CHAR(36) NOT NULL,
    address_type ENUM('HOME', 'WORK', 'OTHER') NOT NULL DEFAULT 'HOME',
    street_address VARCHAR(100) NOT NULL,
    address_line_2 VARCHAR(100),
    city VARCHAR(50) NOT NULL,
    state VARCHAR(50) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    country VARCHAR(50) NOT NULL DEFAULT 'India',
    delivery_instructions VARCHAR(500),
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    
    INDEX idx_addresses_customer_id (customer_id),
    INDEX idx_addresses_type (address_type),
    INDEX idx_addresses_is_default (is_default),
    INDEX idx_addresses_city (city),
    INDEX idx_addresses_postal_code (postal_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- Categories Table
-- ================================================================
CREATE TABLE categories (
    id CHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    display_order INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    image_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    
    INDEX idx_category_name (name),
    INDEX idx_category_active (active),
    INDEX idx_category_display_order (display_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- Menu Items Table
-- ================================================================
CREATE TABLE menu_items (
    id CHAR(36) NOT NULL PRIMARY KEY,
    category_id CHAR(36) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    available BOOLEAN NOT NULL DEFAULT TRUE,
    image_url VARCHAR(500),
    preparation_time_minutes INT,
    calories INT,
    vegetarian BOOLEAN NOT NULL DEFAULT FALSE,
    vegan BOOLEAN NOT NULL DEFAULT FALSE,
    gluten_free BOOLEAN NOT NULL DEFAULT FALSE,
    spicy BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
    
    INDEX idx_menu_item_name (name),
    INDEX idx_menu_item_category (category_id),
    INDEX idx_menu_item_available (available),
    INDEX idx_menu_item_price (price),
    INDEX idx_menu_item_category_available (category_id, available),
    
    CONSTRAINT chk_menu_items_price_positive CHECK (price > 0),
    CONSTRAINT chk_menu_items_preparation_time CHECK (preparation_time_minutes IS NULL OR preparation_time_minutes >= 0),
    CONSTRAINT chk_menu_items_calories CHECK (calories IS NULL OR calories >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- Carts Table
-- ================================================================
CREATE TABLE carts (
    id CHAR(36) NOT NULL PRIMARY KEY,
    customer_id CHAR(36),
    total_amount DECIMAL(10, 2) DEFAULT 0.00,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_cart_customer_id (customer_id),
    INDEX idx_cart_created_at (created_at),
    INDEX idx_cart_updated_at (updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- Cart Items Table
-- ================================================================
CREATE TABLE cart_items (
    id CHAR(36) NOT NULL PRIMARY KEY,
    cart_id CHAR(36) NOT NULL,
    menu_item_id CHAR(36) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
    FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE,
    
    INDEX idx_cart_item_cart_id (cart_id),
    INDEX idx_cart_item_menu_item_id (menu_item_id),
    INDEX idx_cart_item_created_at (created_at),
    
    CONSTRAINT chk_cart_items_quantity_positive CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- Orders Table
-- ================================================================
CREATE TABLE orders (
    id CHAR(36) NOT NULL PRIMARY KEY,
    customer_id CHAR(36) NOT NULL,
    delivery_address_id CHAR(36),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    total_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    subtotal DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    tax_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    delivery_fee DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    special_instructions VARCHAR(1000),
    estimated_prep_time INT,
    actual_prep_time INT,
    confirmed_at TIMESTAMP NULL,
    preparation_started_at TIMESTAMP NULL,
    ready_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    cancelled_at TIMESTAMP NULL,
    cancellation_reason VARCHAR(500),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE RESTRICT,
    FOREIGN KEY (delivery_address_id) REFERENCES addresses(id) ON DELETE SET NULL,
    
    INDEX idx_orders_customer_id (customer_id),
    INDEX idx_orders_status (status),
    INDEX idx_orders_created_at (created_at),
    INDEX idx_orders_updated_at (updated_at),
    INDEX idx_orders_customer_status (customer_id, status),
    INDEX idx_orders_customer_created (customer_id, created_at),
    
    CONSTRAINT chk_orders_total_amount_positive CHECK (total_amount >= 0),
    CONSTRAINT chk_orders_subtotal_positive CHECK (subtotal >= 0),
    CONSTRAINT chk_orders_tax_amount_non_negative CHECK (tax_amount >= 0),
    CONSTRAINT chk_orders_delivery_fee_non_negative CHECK (delivery_fee >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- Order Items Table
-- ================================================================
CREATE TABLE order_items (
    id CHAR(36) NOT NULL PRIMARY KEY,
    order_id CHAR(36) NOT NULL,
    menu_item_id CHAR(36) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    menu_item_name VARCHAR(100) NOT NULL,
    menu_item_description VARCHAR(500),
    category_name VARCHAR(50),
    special_instructions VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE RESTRICT,
    
    INDEX idx_order_item_order_id (order_id),
    INDEX idx_order_item_menu_item_id (menu_item_id),
    INDEX idx_order_item_created_at (created_at),
    
    CONSTRAINT chk_order_items_unit_price_positive CHECK (unit_price > 0),
    CONSTRAINT chk_order_items_quantity_positive CHECK (quantity > 0),
    CONSTRAINT chk_order_items_subtotal_positive CHECK (subtotal >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- Insert Default Roles
-- ================================================================
INSERT INTO roles (id, name, description) VALUES
('550e8400-e29b-41d4-a716-446655440001', 'ADMIN', 'System administrator with full access'),
('550e8400-e29b-41d4-a716-446655440002', 'MANAGER', 'Manager with menu and order management access'),
('550e8400-e29b-41d4-a716-446655440003', 'STAFF', 'Staff member with order processing access'),
('550e8400-e29b-41d4-a716-446655440004', 'CUSTOMER', 'Customer with ordering and account management access');

-- ================================================================
-- Insert Default Permissions
-- ================================================================
INSERT INTO permissions (id, name, description, resource, action) VALUES
-- User Management
('650e8400-e29b-41d4-a716-446655440001', 'USER_READ', 'Read user information', 'USER', 'READ'),
('650e8400-e29b-41d4-a716-446655440002', 'USER_WRITE', 'Create and update users', 'USER', 'WRITE'),
('650e8400-e29b-41d4-a716-446655440003', 'USER_DELETE', 'Delete users', 'USER', 'DELETE'),

-- Menu Management
('650e8400-e29b-41d4-a716-446655440004', 'MENU_READ', 'Read menu items and categories', 'MENU', 'READ'),
('650e8400-e29b-41d4-a716-446655440005', 'MENU_WRITE', 'Create and update menu items', 'MENU', 'WRITE'),
('650e8400-e29b-41d4-a716-446655440006', 'MENU_DELETE', 'Delete menu items', 'MENU', 'DELETE'),

-- Category Management
('650e8400-e29b-41d4-a716-446655440007', 'CATEGORY_READ', 'Read categories', 'CATEGORY', 'READ'),
('650e8400-e29b-41d4-a716-446655440008', 'CATEGORY_WRITE', 'Create and update categories', 'CATEGORY', 'WRITE'),
('650e8400-e29b-41d4-a716-446655440009', 'CATEGORY_DELETE', 'Delete categories', 'CATEGORY', 'DELETE'),

-- Order Management
('650e8400-e29b-41d4-a716-446655440010', 'ORDER_READ', 'Read orders', 'ORDER', 'READ'),
('650e8400-e29b-41d4-a716-446655440011', 'ORDER_WRITE', 'Create orders', 'ORDER', 'WRITE'),
('650e8400-e29b-41d4-a716-446655440012', 'ORDER_STATUS_UPDATE', 'Update order status', 'ORDER', 'STATUS_UPDATE'),

-- Cart Management
('650e8400-e29b-41d4-a716-446655440013', 'CART_READ', 'Read cart contents', 'CART', 'READ'),
('650e8400-e29b-41d4-a716-446655440014', 'CART_WRITE', 'Modify cart contents', 'CART', 'WRITE'),

-- Report Access
('650e8400-e29b-41d4-a716-446655440015', 'REPORT_READ', 'Access reports and analytics', 'REPORT', 'READ');

-- ================================================================
-- Assign Permissions to Roles
-- ================================================================

-- ADMIN - Full access
INSERT INTO role_permissions (role_id, permission_id) VALUES
('550e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440001'), -- USER_READ
('550e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440002'), -- USER_WRITE
('550e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440003'), -- USER_DELETE
('550e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440004'), -- MENU_READ
('550e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440005'), -- MENU_WRITE
('550e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440006'), -- MENU_DELETE
('550e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440007'), -- CATEGORY_READ
('550e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440008'), -- CATEGORY_WRITE
('550e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440009'), -- CATEGORY_DELETE
('550e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440010'), -- ORDER_READ
('550e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440011'), -- ORDER_WRITE
('550e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440012'), -- ORDER_STATUS_UPDATE
('550e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440013'), -- CART_READ
('550e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440014'), -- CART_WRITE
('550e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440015'); -- REPORT_READ

-- MANAGER - Menu and order management
INSERT INTO role_permissions (role_id, permission_id) VALUES
('550e8400-e29b-41d4-a716-446655440002', '650e8400-e29b-41d4-a716-446655440004'), -- MENU_READ
('550e8400-e29b-41d4-a716-446655440002', '650e8400-e29b-41d4-a716-446655440005'), -- MENU_WRITE
('550e8400-e29b-41d4-a716-446655440002', '650e8400-e29b-41d4-a716-446655440006'), -- MENU_DELETE
('550e8400-e29b-41d4-a716-446655440002', '650e8400-e29b-41d4-a716-446655440007'), -- CATEGORY_READ
('550e8400-e29b-41d4-a716-446655440002', '650e8400-e29b-41d4-a716-446655440008'), -- CATEGORY_WRITE
('550e8400-e29b-41d4-a716-446655440002', '650e8400-e29b-41d4-a716-446655440009'), -- CATEGORY_DELETE
('550e8400-e29b-41d4-a716-446655440002', '650e8400-e29b-41d4-a716-446655440010'), -- ORDER_READ
('550e8400-e29b-41d4-a716-446655440002', '650e8400-e29b-41d4-a716-446655440012'), -- ORDER_STATUS_UPDATE
('550e8400-e29b-41d4-a716-446655440002', '650e8400-e29b-41d4-a716-446655440015'); -- REPORT_READ

-- STAFF - Order processing
INSERT INTO role_permissions (role_id, permission_id) VALUES
('550e8400-e29b-41d4-a716-446655440003', '650e8400-e29b-41d4-a716-446655440004'), -- MENU_READ
('550e8400-e29b-41d4-a716-446655440003', '650e8400-e29b-41d4-a716-446655440007'), -- CATEGORY_READ
('550e8400-e29b-41d4-a716-446655440003', '650e8400-e29b-41d4-a716-446655440010'), -- ORDER_READ
('550e8400-e29b-41d4-a716-446655440003', '650e8400-e29b-41d4-a716-446655440012'); -- ORDER_STATUS_UPDATE

-- CUSTOMER - Basic access
INSERT INTO role_permissions (role_id, permission_id) VALUES
('550e8400-e29b-41d4-a716-446655440004', '650e8400-e29b-41d4-a716-446655440004'), -- MENU_READ
('550e8400-e29b-41d4-a716-446655440004', '650e8400-e29b-41d4-a716-446655440007'), -- CATEGORY_READ
('550e8400-e29b-41d4-a716-446655440004', '650e8400-e29b-41d4-a716-446655440010'), -- ORDER_READ (own orders)
('550e8400-e29b-41d4-a716-446655440004', '650e8400-e29b-41d4-a716-446655440011'), -- ORDER_WRITE
('550e8400-e29b-41d4-a716-446655440004', '650e8400-e29b-41d4-a716-446655440013'), -- CART_READ
('550e8400-e29b-41d4-a716-446655440004', '650e8400-e29b-41d4-a716-446655440014'); -- CART_WRITE