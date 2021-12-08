package io.roach.jpa.sandbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.zaxxer.hikari.HikariDataSource;

import io.roach.jpa.sandbox.service.OrderSystem;

@Configuration
@EnableAutoConfiguration(exclude = {
        TransactionAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        DataSourceAutoConfiguration.class
})
@ComponentScan(basePackages = "io.roach")
@EnableJpaRepositories(basePackages = {"io.roach"})
public class OrderApplication implements CommandLineRunner {
    public static void main(String[] args) {
        new SpringApplicationBuilder(OrderApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private OrderSystem orderSystem;

    @Autowired
    @Qualifier("readWriteDataSource")
    private HikariDataSource readWriteDS;

    @Autowired
    @Qualifier("readOnlyDataSource")
    private HikariDataSource readOnlyDS;


    @Override
    public void run(String... args) {
//        DataSourceInfo.logDataSourceInfo(readWriteDS);
//        DataSourceInfo.logDataSourceInfo(readOnlyDS);
//        if (true)return;

        logger.info("Clear all...");
        orderSystem.clearAll();

        logger.info(">> Creating products...");
        orderSystem.createProductInventory();

        logger.info(">> Creating customers...");
        orderSystem.createCustomers();

        logger.info(">> Creating orders...");
        orderSystem.createOrders();

        logger.info(">> Listing orders...");
        orderSystem.listOrders();

        logger.info(">> Find by sku: {}", orderSystem.findProductBySku("CRDB-UL-ED1"));

        logger.info(">> Get total order price (RO): {}", orderSystem.getTotalOrderPrice_ReadOnly());

        logger.info(">> Get total order price (RW): {}", orderSystem.getTotalOrderPrice_ReadWrite());

        logger.info(">> Removing orders...");
        orderSystem.removeOrders();
    }
}

