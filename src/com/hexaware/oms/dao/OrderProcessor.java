package com.hexaware.oms.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.hexaware.oms.entity.Clothing;
import com.hexaware.oms.entity.Electronics;
import com.hexaware.oms.entity.Order;
import com.hexaware.oms.entity.OrderProduct;
import com.hexaware.oms.entity.Product;
import com.hexaware.oms.entity.User;
import com.hexaware.oms.exception.DatabaseConnectionException;
import com.hexaware.oms.exception.OMSException;
import com.hexaware.oms.util.DBConnUtil;

public class OrderProcessor implements IOrderManagementRepository {

    private Connection connection;

    public OrderProcessor() throws OMSException {
        try {
            connection = DBConnUtil.getConnection();
        } catch (DatabaseConnectionException e) {
            throw new OMSException("Failed to get DB connection: " + e.getMessage());
        }
    }

    
    // For the creation of User
    @Override
    public boolean createUser(User user) throws OMSException {
        boolean status = false;
        String query = "insert into users (username, password, role) values (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRole());
            int rows = pstmt.executeUpdate();
            status = rows > 0;
        } catch (SQLException e) {
            throw new OMSException("Failed to create user: " + e.getMessage());
        }
        return status;
    }
    
    // For the Products to Order

    @Override
    public boolean createProduct(User user, Product product) throws OMSException {
        boolean status = false;
        if (!"Admin".equalsIgnoreCase(user.getRole())) {
            throw new OMSException("Only admin users can add products.");
        }
        String query = "insert into products (product_name, description, price, quantity_in_stock, type, brand, warranty_period, size, color) "
                + "values (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, product.getProductName());
            pstmt.setString(2, product.getDescription());
            pstmt.setDouble(3, product.getPrice());
            pstmt.setInt(4, product.getQuantityInStock());
            pstmt.setString(5, product.getType());

            if ("Electronics".equalsIgnoreCase(product.getType()) && product instanceof Electronics) {
                Electronics elec = (Electronics) product;
                pstmt.setString(6, elec.getBrand());
                pstmt.setInt(7, elec.getWarrantyPeriod());
                pstmt.setNull(8, java.sql.Types.VARCHAR);
                pstmt.setNull(9, java.sql.Types.VARCHAR);
            } else if ("Clothing".equalsIgnoreCase(product.getType()) && product instanceof Clothing) {
                pstmt.setNull(6, java.sql.Types.VARCHAR);
                pstmt.setNull(7, java.sql.Types.INTEGER);
                Clothing cloth = (Clothing) product;
                pstmt.setString(8, cloth.getSize());
                pstmt.setString(9, cloth.getColor());
            } else {
                throw new OMSException("Invalid product type or data.");
            }

            int rows = pstmt.executeUpdate();
            status = rows > 0;
        } catch (SQLException e) {
            throw new OMSException("Failed to create product: " + e.getMessage());
        }
        return status;
    }
    
    // Then to create Order
    @Override
    public Order createOrder(User user, List<OrderProduct> products) throws OMSException {
        Order order = null;
        String orderQuery = "insert into orders (user_id, order_date) values (?, ?)";
        String orderProductQuery = "insert into order_products (order_id, product_id, quantity) values (?, ?, ?)";

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement orderStmt = connection.prepareStatement(orderQuery, Statement.RETURN_GENERATED_KEYS)) {
                orderStmt.setInt(1, user.getUserId());
                orderStmt.setTimestamp(2, java.sql.Timestamp.valueOf(LocalDateTime.now()));
                int rows = orderStmt.executeUpdate();

                if (rows == 0) {
                    throw new OMSException("Failed to create order.");
                }

                ResultSet rs = orderStmt.getGeneratedKeys();
                if (rs.next()) {
                    int orderId = rs.getInt(1);
                    order = new Order(orderId, user.getUserId(), LocalDateTime.now());

                    try (PreparedStatement opStmt = connection.prepareStatement(orderProductQuery)) {
                        for (OrderProduct op : products) {
                            opStmt.setInt(1, orderId);
                            opStmt.setInt(2, op.getProductId());
                            opStmt.setInt(3, op.getQuantity());
                            opStmt.addBatch();
                        }
                        opStmt.executeBatch();
                    }
                }
            }
            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException se) {
                throw new OMSException("Rollback failed: " + se.getMessage());
            }
            throw new OMSException("Order creation failed: " + e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                throw new OMSException("Failed to reset auto-commit: " + e.getMessage());
            }
        }

        return order;
    }
    
    //To cancel the order
    @Override
    public boolean cancelOrder(int userId, int orderId) throws OMSException {
        boolean status = false;

        String checkQuery = "select * from orders where order_id = ? and user_id = ?";
        String deleteOrderProducts = "delete from order_products where order_id = ?";
        String deleteOrder = "delete from orders where order_id = ?";

        try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
            checkStmt.setInt(1, orderId);
            checkStmt.setInt(2, userId);
            ResultSet rs = checkStmt.executeQuery();

            if (!rs.next()) {
                throw new OMSException("Order or User not found.");
            }

            try (PreparedStatement delProdStmt = connection.prepareStatement(deleteOrderProducts);
                 PreparedStatement delOrderStmt = connection.prepareStatement(deleteOrder)) {

                delProdStmt.setInt(1, orderId);
                delProdStmt.executeUpdate();

                delOrderStmt.setInt(1, orderId);
                int rows = delOrderStmt.executeUpdate();
                status = rows > 0;
            }

        } catch (SQLException e) {
            throw new OMSException("Failed to cancel order: " + e.getMessage());
        }

        return status;
    }

    //To get all products
    
    @Override
    public List<Product> getAllProducts() throws OMSException {
        List<Product> products = new ArrayList<>();
        String query = "select * from products";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String type = rs.getString("type");
                if ("Electronics".equalsIgnoreCase(type)) {
                    Electronics elec = new Electronics(
                            rs.getInt("product_id"),
                            rs.getString("product_name"),
                            rs.getString("description"),
                            rs.getDouble("price"),
                            rs.getInt("quantity_in_stock"),
                            type,
                            rs.getString("brand"),
                            rs.getInt("warranty_period")
                    );
                    products.add(elec);
                } else if ("Clothing".equalsIgnoreCase(type)) {
                    Clothing cloth = new Clothing(
                            rs.getInt("product_id"),
                            rs.getString("product_name"),
                            rs.getString("description"),
                            rs.getDouble("price"),
                            rs.getInt("quantity_in_stock"),
                            type,
                            rs.getString("size"),
                            rs.getString("color")
                    );
                    products.add(cloth);
                }
            }

        } catch (SQLException e) {
            throw new OMSException("Failed to retrieve products: " + e.getMessage());
        }

        return products;
    }
    
    //To get order by User
    @Override
    public List<Product> getOrderByUser(User user) throws OMSException {
        List<Product> products = new ArrayList<>();
        String query = "select p.* from products p "
                     + "join order_products op on p.product_id = op.product_id "
                     + "join orders o on o.order_id = op.order_id "
                     + "where o.user_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, user.getUserId());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String type = rs.getString("type");
                if ("Electronics".equalsIgnoreCase(type)) {
                    Electronics elec = new Electronics(
                            rs.getInt("product_id"),
                            rs.getString("product_name"),
                            rs.getString("description"),
                            rs.getDouble("price"),
                            rs.getInt("quantity_in_stock"),
                            type,
                            rs.getString("brand"),
                            rs.getInt("warranty_period")
                    );
                    products.add(elec);
                } else if ("Clothing".equalsIgnoreCase(type)) {
                    Clothing cloth = new Clothing(
                            rs.getInt("product_id"),
                            rs.getString("product_name"),
                            rs.getString("description"),
                            rs.getDouble("price"),
                            rs.getInt("quantity_in_stock"),
                            type,
                            rs.getString("size"),
                            rs.getString("color")
                    );
                    products.add(cloth);
                }
            }

        } catch (SQLException e) {
            throw new OMSException("Failed to retrieve user's orders: " + e.getMessage());
        }

        return products;
    }
    
    // To check the user and admin
    public User getUserByUsernameAndPassword(String username, String password) throws OMSException {
        User user = null;
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                user = new User(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("role")
                );
            }
        } catch (SQLException e) {
            throw new OMSException("Login check failed: " + e.getMessage());
        }

        return user;
    }

    
    
}
