package com.hexaware.oms.service;

import java.util.List;

import com.hexaware.oms.dao.IOrderManagementRepository;
import com.hexaware.oms.dao.OrderProcessor;
import com.hexaware.oms.entity.Order;
import com.hexaware.oms.entity.OrderProduct;
import com.hexaware.oms.entity.Product;
import com.hexaware.oms.entity.User;
import com.hexaware.oms.exception.OMSException;

public class OrderService {

    private IOrderManagementRepository dao;

    public OrderService() throws OMSException {
        dao = new OrderProcessor();
    }

    //Product creation
    public boolean createProduct(User user, Product product) throws OMSException {
        if (user == null || !"Admin".equalsIgnoreCase(user.getRole())) {
            throw new OMSException("Access denied. Only admin users can create products.");
        }

        if (product == null || product.getProductName() == null || product.getProductName().isEmpty()) {
            throw new OMSException("Invalid product. Name cannot be empty.");
        }

        return dao.createProduct(user, product);
    }
    
    //User Creation
    public boolean createUser(User user) throws OMSException {
        if (user == null) {
            throw new OMSException("User cannot be null.");
        }

        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            throw new OMSException("Username cannot be empty.");
        }

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new OMSException("Password cannot be empty.");
        }

        if (user.getRole() == null || 
            !(user.getRole().equalsIgnoreCase("Admin") || user.getRole().equalsIgnoreCase("User"))) {
            throw new OMSException("Invalid role. Must be Admin or User.");
        }

        return dao.createUser(user);
    }
    
    //Order Creation
    public Order createOrder(User user, List<OrderProduct> products) throws OMSException {
        if (user == null || user.getUserId() <= 0) {
            throw new OMSException("Invalid user for placing order.");
        }

        if (products == null || products.isEmpty()) {
            throw new OMSException("Order must contain at least one product.");
        }

        for (OrderProduct op : products) {
            if (op.getProductId() <= 0 || op.getQuantity() <= 0) {
                throw new OMSException("Each product must have a valid product ID and quantity.");
            }
        }

        return dao.createOrder(user, products);
    }
    
    //To cancel the order
    public boolean cancelOrder(int userId, int orderId) throws OMSException {
        if (userId <= 0) {
            throw new OMSException("Invalid user ID.");
        }

        if (orderId <= 0) {
            throw new OMSException("Invalid order ID.");
        }

        return dao.cancelOrder(userId, orderId);
    }
    
    // To get all products
    public List<Product> getAllProducts() throws OMSException {
        List<Product> products = dao.getAllProducts();
        if (products == null || products.isEmpty()) {
            throw new OMSException("No products found in the system.");
        }
        return products;
    }
    
    // To get Order By User
    public List<Product> getOrderByUser(User user) throws OMSException {
        if (user == null || user.getUserId() <= 0) {
            throw new OMSException("Invalid user details.");
        }

        List<Product> products = dao.getOrderByUser(user);
        if (products == null || products.isEmpty()) {
            throw new OMSException("No orders found for the user.");
        }

        return products;
    }
    
    //To validate User and admin
    public User validateLogin(String username, String password) throws OMSException {
        User user = dao.getUserByUsernameAndPassword(username, password);
        if (user == null) {
            throw new OMSException("Invalid username or password.");
        }
        return user;
    }

    
}
