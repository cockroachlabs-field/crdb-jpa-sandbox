package io.roach.jpa.sandbox;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.hibernate.engine.jdbc.connections.internal.ConnectionProviderInitiator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;

import com.zaxxer.hikari.HikariConfigMXBean;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;

public class DataSourceInfo {
    private static final Logger logger = LoggerFactory.getLogger(DataSourceInfo.class);

    public static void logConnectionInfo(DataSource dataSource) {
        final Map<String, Object> tuples = new LinkedHashMap<>();

        Connection connection = null;
        try {
            connection = DataSourceUtils.doGetConnection(dataSource);
            DatabaseMetaData metaData = connection.getMetaData();

            tuples.put("databaseVersion", new JdbcTemplate(dataSource)
                    .queryForObject("select version()", String.class));
            tuples.put("URL", metaData.getURL());
            tuples.put("databaseProductName", metaData.getDatabaseProductName());
            tuples.put("databaseMajorVersion", metaData.getDatabaseMajorVersion());
            tuples.put("databaseMinorVersion", metaData.getDatabaseMinorVersion());
            tuples.put("databaseProductVersion", metaData.getDatabaseProductVersion());
            tuples.put("driverMajorVersion", metaData.getDriverMajorVersion());
            tuples.put("driverMinorVersion", metaData.getDriverMinorVersion());
            tuples.put("driverName", metaData.getDriverName());
            tuples.put("driverVersion", metaData.getDriverVersion());
            tuples.put("maxConnections", metaData.getMaxConnections());
            tuples.put("defaultTransactionIsolation", metaData.getDefaultTransactionIsolation());
            tuples.put("transactionIsolation", connection.getTransactionIsolation());
            tuples.put("transactionIsolationName",
                    ConnectionProviderInitiator.toIsolationNiceName(connection.getTransactionIsolation()));
        } catch (SQLException ex) {
            // Ignore
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }

        logger.info("Database connection info:");
        tuples.forEach((k, v) -> {
            logger.info("\t{}: {}", k, v);
        });
    }

    public static void logDataSourceInfo(HikariDataSource dataSource) {
        HikariPoolMXBean poolInfo = dataSource.getHikariPoolMXBean();
        logger.info("Connection pool status:");
        logger.info("\tactiveConnections: {}", poolInfo.getActiveConnections());
        logger.info("\tidleConnections: {}", poolInfo.getIdleConnections());
        logger.info("\ttotalConnections: {}", poolInfo.getTotalConnections());
        logger.info("\tthreadsAwaitingConnection: {}", poolInfo.getThreadsAwaitingConnection());

        HikariConfigMXBean configInfo = dataSource.getHikariConfigMXBean();
        logger.info("Connection pool configuration:");
        logger.info("\tpoolName: {}", configInfo.getPoolName());
        logger.info("\tmaximumPoolSize: {}", configInfo.getMaximumPoolSize());
        logger.info("\tminimumIdle: {}", configInfo.getMinimumIdle());
        logger.info("\tconnectionTimeout: {}", configInfo.getConnectionTimeout());
        logger.info("\tvalidationTimeout: {}", configInfo.getValidationTimeout());
        logger.info("\tidleTimeout: {}", configInfo.getIdleTimeout());
        logger.info("\tmaxLifetime: {}", configInfo.getMaxLifetime());
        logger.info("\tleakDetectionThreshold: {}", configInfo.getLeakDetectionThreshold());
        logger.info("\tcatalog: {}", configInfo.getCatalog());

        logConnectionInfo(dataSource);
    }
}
