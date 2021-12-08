package io.roach.jpa.sandbox.config;

import java.sql.Connection;

import net.ttddyy.dsproxy.ConnectionInfo;
import net.ttddyy.dsproxy.proxy.ProxyConfig;

public class ReadOnlyStrictStrategy implements ReadOnlyConnectionStrategy {
    @Override
    public boolean handleSetReadOnly(Connection connection, ConnectionInfo connectionInfo, ProxyConfig proxyConfig)
            throws Exception {
        throw new FakeTimeoutException(
                "Connection setReadOnly(true) called for datasource ["
                        + connectionInfo.getDataSourceName()
                        + "] See stacktrace for outer-most txn marker.");
    }
}
