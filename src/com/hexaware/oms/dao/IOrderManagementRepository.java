package com.hexaware.oms.dao;

import java.util.List;
import com.hexaware.oms.entity.*;
import com.hexaware.oms.exception.OMSException;

public interface IOrderManagementRepository {

    boolean createUser(User user) throws OMSException;

    boolean createProduct(User user, Product product) throws OMSException;

    Order createOrder(User user, List<OrderProduct> products) throws OMSException;

    boolean cancelOrder(int userId, int orderId) throws OMSException;

    List<Product> getAllProducts() throws OMSException;

    List<Product> getOrderByUser(User user) throws OMSException;

    User getUserByUsernameAndPassword(String username, String password) throws OMSException;
}
