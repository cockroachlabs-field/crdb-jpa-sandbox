package io.roach.jpa.sandbox.config;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import net.ttddyy.dsproxy.ConnectionInfo;
import net.ttddyy.dsproxy.listener.logging.SLF4JLogLevel;
import net.ttddyy.dsproxy.proxy.ProxyConfig;
import net.ttddyy.dsproxy.proxy.ProxyJdbcObject;
import net.ttddyy.dsproxy.proxy.jdk.ConnectionInvocationHandler;
import net.ttddyy.dsproxy.proxy.jdk.JdkJdbcProxyFactory;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;

@Configuration
@EnableConfigurationProperties
public class JpaConfiguration extends AbstractJpaConfiguration {
    private final ReadOnlyConnectionStrategy readOnlyConnectionStrategy = new ReadOnlyLenientStrategy();

    @Autowired
    @Qualifier("readWriteDataSourceProperties")
    private DataSourceProperties readWriteProperties;

    @Autowired
    @Qualifier("readOnlyDataSourceProperties")
    private DataSourceProperties readOnlyProperties;

    @Override
    @Primary
    public ReadWriteRoutingDataSource primaryDataSource() {
        Map<Object, Object> dataSources = new HashMap<>();
        // Expect read-write DS to not be used with readOnly attribute on txn markers
        dataSources.put(DataSourceType.READ_WRITE, proxyDataSource(readWriteDataSource()));
        dataSources.put(DataSourceType.READ_ONLY, proxyDataSource(readOnlyDataSource()));

        ReadWriteRoutingDataSource routingDataSource = new ReadWriteRoutingDataSource();
        routingDataSource.setTargetDataSources(dataSources);
        routingDataSource.setLenientFallback(false);

        return routingDataSource;
    }

    protected DataSource proxyDataSource(DataSource dataSource) {
        return ProxyDataSourceBuilder
                .create(dataSource)
                .asJson()
                .logQueryBySlf4j(SLF4JLogLevel.TRACE, "io.roach.SQL_TRACE")
                // This has no effect if routing DS are used
                .jdbcProxyFactory(new JdkJdbcProxyFactory() {
                    @Override
                    public Connection createConnection(Connection connection, ConnectionInfo connectionInfo,
                                                       ProxyConfig proxyConfig) {
                        return (Connection) Proxy.newProxyInstance(ProxyJdbcObject.class.getClassLoader(),
                                new Class[] {ProxyJdbcObject.class, Connection.class},
                                new ConnectionInvocationHandler(connection, connectionInfo, proxyConfig) {
                                    @Override
                                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                                        if ("setReadOnly".equals(method.getName()) && Boolean.TRUE.equals(args[0])) {
                                            if (!readOnlyConnectionStrategy.handleSetReadOnly(connection,
                                                    connectionInfo, proxyConfig)) {
                                                return super.invoke(proxy, method, new Object[] {false});
                                            }
                                        }
                                        return super.invoke(proxy, method, args);
                                    }
                                });
                    }
                })
                .build();
    }

    @Bean
    public HikariDataSource readWriteDataSource() {
        HikariDataSource ds = readWriteProperties
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
        return connectionPoolDataSource(ds, "ReadWriteDS");
    }

    @Bean
    @ConfigurationProperties("roach.datasource.readwrite")
    public DataSourceProperties readWriteDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public HikariDataSource readOnlyDataSource() {
        PGSimpleDataSource ds = readOnlyProperties
                .initializeDataSourceBuilder()
                .type(PGSimpleDataSource.class)
                .build();
        return connectionPoolDataSource(ds, "ReadOnlyDS");
    }

    @Bean
    @ConfigurationProperties("roach.datasource.readonly")
    public DataSourceProperties readOnlyDataSourceProperties() {
        return new DataSourceProperties();
    }

    protected HikariDataSource connectionPoolDataSource(DataSource dataSource, String poolName) {
        return new HikariDataSource(hikariConfig(dataSource, poolName));
    }

    protected HikariConfig hikariConfig(DataSource dataSource, String poolName) {
        final int poolSize = Runtime.getRuntime().availableProcessors() * 4;

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setPoolName(poolName);
        hikariConfig.setMaximumPoolSize(poolSize); // Should be: cluster_total_vcpu / total_pool_number
        hikariConfig.setMinimumIdle(poolSize / 2); // Should be maxPoolSize for fixed-sized pool
        hikariConfig.setDataSource(dataSource);
        hikariConfig.setAutoCommit(false);

        hikariConfig.addDataSourceProperty("reWriteBatchedInserts", "true");

        return hikariConfig;
    }
}
