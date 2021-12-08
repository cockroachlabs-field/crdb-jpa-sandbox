package io.roach.jpa.sandbox.service;

import java.math.BigDecimal;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import io.roach.jpa.sandbox.domain.Order;
import io.roach.jpa.sandbox.repository.OrderRepository;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private DataSource dataSource;

    @Transactional(propagation = Propagation.MANDATORY)
    public BigDecimal getTotalOrderPrice(boolean doWrite) {
        Assert.isTrue(TransactionSynchronizationManager.isActualTransactionActive(), "No tx");

        BigDecimal price = BigDecimal.ZERO;
        List<Order> orders = orderRepository.findAll();
        for (Order order : orders) {
            price = price.add(order.getTotalPrice());

            // Won't work for read-only txn
            if (doWrite) {
                new JdbcTemplate(dataSource)
                        .update("UPDATE orders SET tags=? WHERE id=?", "tag1", order.getId());
            }
        }

        return price;
    }
}
