package io.roach.jpa.sandbox.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.lang.Nullable;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class ReadWriteRoutingDataSource extends AbstractRoutingDataSource {
    @Nullable
    @Override
    protected Object determineCurrentLookupKey() {
        Object key = TransactionSynchronizationManager.isCurrentTransactionReadOnly()
                ? DataSourceType.READ_ONLY : DataSourceType.READ_WRITE;
        return key;
    }
}
