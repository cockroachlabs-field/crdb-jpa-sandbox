package io.roach.jpa.sandbox.service;

import java.math.BigDecimal;

import io.roach.jpa.sandbox.domain.Product;

public interface OrderSystem {
    void clearAll();

    void createProductInventory();

    void createCustomers();

    void createOrders();

    void listOrders();

    Product findProductBySku(String sku);

    BigDecimal getTotalOrderPrice_ReadOnly();

    BigDecimal getTotalOrderPrice_ReadWrite();

    void removeOrders();
}
