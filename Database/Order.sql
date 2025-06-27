create database ordermanagement;
use ordermanagement;

-- users Table
create table users (
    user_id int primary key auto_increment,
    username varchar(50) not null unique,
    password varchar(100) null,
    role varchar(10) check (role in ('Admin', 'User')) not null
);

-- products table
create table products (
    product_id int primary key auto_increment,
    product_name varchar(100) not null,
    description text,
    price double not null,
    quantity_in_stock int not null,
    type varchar(20) check (type in ('Electronics', 'Clothing')),
    
    -- For Electronics 
    brand varchar(50),
    warranty_period INT,
    
    -- For Clothing 
    size varchar(10),
    color varchar(30)
);

-- rders table
create table orders (
    order_id int primary key auto_increment,
    user_id int,
    order_date timestamp default current_timestamp,
    foreign key (user_id) references users(user_id)
);

-- order_products table
create table orderproducts (
    order_id int,
    product_id int,
    quantity int not null,
    primary key (order_id, product_id),
    foreign key (order_id) references orders(order_id),
    foreign key (product_id) references products(product_id)
);

-- Inserting into users
insert into users (username, password, role) values
('admin1', 'adminp123', 'Admin'),
('Selvaraj', 'password1', 'User'),
('Rajkumar', 'password2', 'User'),
('admin2', 'admin321', 'Admin'),
('Agalya', 'agalya123', 'User');

select * from users;

-- Products values ( electronics )
insert into products (product_name, description, price, quantity_in_stock, type, brand, warranty_period)
values('iPhone 14', 'Latest iPhone', 79839.00, 10, 'Electronics', 'Apple', 12),
('Samsung TV', 'Smart 8K TV', 55800.00, 5, 'Electronics', 'Samsung', 24),
('Dell Laptop', 'Core i3 Laptop', 72000.00, 7, 'Electronics', 'Dell', 18);

-- Clothing
insert into products (product_name, description, price, quantity_in_stock, type, size, color)
values('T-Shirt', 'Cotton T-shirt', 599.00, 15, 'Clothing', 'M', 'Black'),
('Jeans', 'Blue denim jeans', 1239.00, 40, 'Clothing', 'L', 'Blue');

select * from products;

rename table orderproducts to order_products;

select * from orders;
