package io.roach.jpa.sandbox.config;

import java.util.Objects;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.hibernate.cache.internal.NoCachingRegionFactory;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.CockroachDB201Dialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement(proxyTargetClass = true)
@EnableAspectJAutoProxy(proxyTargetClass = true)
public abstract class AbstractJpaConfiguration {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Bean
    @Primary
    public abstract DataSource primaryDataSource();

    @Bean
    public PersistenceExceptionTranslationPostProcessor persistenceExceptionTranslationPostProcessor() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(@Autowired DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setPersistenceUnitName(getClass().getSimpleName());
        emf.setDataSource(dataSource);
        emf.setPackagesToScan("io.roach");

        emf.setJpaVendorAdapter(jpaVendorAdapter());
        emf.setJpaProperties(jpaVendorProperties());

        return emf;
    }

    private JpaVendorAdapter jpaVendorAdapter() {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(false);
        vendorAdapter.setShowSql(false);
        vendorAdapter.setDatabasePlatform(CockroachDB201Dialect.class.getName());
        vendorAdapter.setDatabase(Database.POSTGRESQL);

        HibernateJpaDialect jpaDialect = vendorAdapter.getJpaDialect();
        // Needed for routing DS
        Objects.requireNonNull(jpaDialect).setPrepareConnection(false);

        return vendorAdapter;
    }

    private Properties jpaVendorProperties() {
        return new Properties() {
            {
                setProperty(Environment.GENERATE_STATISTICS, Boolean.TRUE.toString());
                setProperty(Environment.LOG_SESSION_METRICS, Boolean.FALSE.toString());
                setProperty(Environment.USE_MINIMAL_PUTS, Boolean.TRUE.toString());
                setProperty(Environment.USE_SECOND_LEVEL_CACHE, Boolean.FALSE.toString());
                setProperty(Environment.CACHE_REGION_FACTORY, NoCachingRegionFactory.class.getName());
                setProperty(Environment.STATEMENT_BATCH_SIZE, "64");
                setProperty(Environment.ORDER_INSERTS, Boolean.TRUE.toString());
                setProperty(Environment.ORDER_UPDATES, Boolean.TRUE.toString());
                setProperty(Environment.BATCH_VERSIONED_DATA, Boolean.TRUE.toString());
                setProperty(Environment.FORMAT_SQL, Boolean.FALSE.toString());
                // Mutes Postgres JPA Error (Method org.postgresql.jdbc.PgConnection.createClob() is not yet implemented).
                setProperty(Environment.NON_CONTEXTUAL_LOB_CREATION, Boolean.TRUE.toString());
                setProperty(Environment.CONNECTION_PROVIDER_DISABLES_AUTOCOMMIT, Boolean.TRUE.toString());
            }
        };
    }

    @Bean
    public PlatformTransactionManager transactionManager(@Autowired EntityManagerFactory emf) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);
        transactionManager.setJpaDialect(new HibernateJpaDialect());
        return transactionManager;
    }
}
