package com.hexaware.oms.main;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.hexaware.oms.entity.*;
import com.hexaware.oms.exception.OMSException;
import com.hexaware.oms.service.OrderService;

public class MainModule {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        OrderService service;

        try {
            service = new OrderService();
        } catch (OMSException e) {
            System.out.println("System failed to initialize: " + e.getMessage());
            sc.close();
            return;
        }

        boolean exit = false;

        while (!exit) {
            System.out.println("\n--- Order Management System ---");
            System.out.println("1. Create User");
            System.out.println("2. Create Product");
            System.out.println("3. Create Order");
            System.out.println("4. Cancel Order");
            System.out.println("5. View All Products");
            System.out.println("6. View Orders by User");
            System.out.println("7. Exit");
            System.out.print("Enter your choice: ");

            int choice = sc.nextInt();
            sc.nextLine(); 

            try {
                switch (choice) {
                    case 1:
                        System.out.print("Enter username: ");
                        String username = sc.nextLine();
                        System.out.print("Enter password: ");
                        String password = sc.nextLine();
                        System.out.print("Enter role (Admin/User): ");
                        String role = sc.nextLine();

                        User user = new User(0, username, password, role);
                        if (service.createUser(user)) {
                            System.out.println("User created successfully.");
                        } else {
                            System.out.println("Failed to create user.");
                        }
                        break;

                    case 2:
                        System.out.print("Enter admin username: ");
                        String adminUsername = sc.nextLine();
                        System.out.print("Enter admin password: ");
                        String adminPassword = sc.nextLine();
                        User admin = service.validateLogin(adminUsername, adminPassword);

                        System.out.print("Enter product type (Electronics/Clothing): ");
                        String type = sc.nextLine();

                        System.out.print("Enter product name: ");
                        String pname = sc.nextLine();
                        System.out.print("Enter description: ");
                        String desc = sc.nextLine();
                        System.out.print("Enter price: ");
                        double price = sc.nextDouble();
                        System.out.print("Enter quantity: ");
                        int qty = sc.nextInt();
                        sc.nextLine(); 

                        Product product = null;

                        if ("Electronics".equalsIgnoreCase(type)) {
                            System.out.print("Enter brand: ");
                            String brand = sc.nextLine();
                            System.out.print("Enter warranty period: ");
                            int warranty = sc.nextInt();
                            sc.nextLine(); 
                            product = new Electronics(0, pname, desc, price, qty, type, brand, warranty);
                        } else if ("Clothing".equalsIgnoreCase(type)) {
                            System.out.print("Enter size: ");
                            String size = sc.nextLine();
                            System.out.print("Enter color: ");
                            String color = sc.nextLine();
                            product = new Clothing(0, pname, desc, price, qty, type, size, color);
                        }

                        if (service.createProduct(admin, product)) {
                            System.out.println("Product added successfully.");
                        }
                        break;

                    case 3:
                        System.out.print("Enter user ID for order: ");
                        int uid = sc.nextInt();
                        sc.nextLine(); 

                        User orderUser = new User();
                        orderUser.setUserId(uid);

                        List<OrderProduct> orderProducts = new ArrayList<>();

                        System.out.print("How many products to order? ");
                        int count = sc.nextInt();
                        for (int i = 0; i < count; i++) {
                            System.out.print("Enter product ID: ");
                            int pid = sc.nextInt();
                            System.out.print("Enter quantity: ");
                            int quantity = sc.nextInt();
                            orderProducts.add(new OrderProduct(0, pid, quantity));
                        }

                        Order newOrder = service.createOrder(orderUser, orderProducts);
                        System.out.println("Order created with ID: " + newOrder.getOrderId());
                        break;

                    case 4:
                        System.out.print("Enter user ID: ");
                        int cancelUserId = sc.nextInt();
                        System.out.print("Enter order ID to cancel: ");
                        int cancelOrderId = sc.nextInt();

                        if (service.cancelOrder(cancelUserId, cancelOrderId)) {
                            System.out.println("Order cancelled successfully.");
                        }
                        break;

                    case 5:
                        List<Product> products = service.getAllProducts();
                        for (Product p : products) {
                            System.out.println(p.getProductId() + " - " + p.getProductName() + " - ₹" + p.getPrice());
                        }
                        break;

                    case 6:
                        System.out.print("Enter user ID to view orders: ");
                        int viewUserId = sc.nextInt();
                        sc.nextLine();

                        User viewUser = new User();
                        viewUser.setUserId(viewUserId);

                        List<Product> orderedProducts = service.getOrderByUser(viewUser);
                        for (Product p : orderedProducts) {
                            System.out.println(p.getProductId() + " - " + p.getProductName() + " - ₹" + p.getPrice());
                        }
                        break;

                    case 7:
                        exit = true;
                        System.out.println("Exiting system.");
                        break;

                    default:
                        System.out.println("Invalid choice.");
                }
            } catch (OMSException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        sc.close();
    }
}
