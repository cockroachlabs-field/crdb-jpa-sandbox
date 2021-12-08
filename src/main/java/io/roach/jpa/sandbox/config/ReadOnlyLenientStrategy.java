package io.roach.jpa.sandbox.config;

import java.sql.Connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.ttddyy.dsproxy.ConnectionInfo;
import net.ttddyy.dsproxy.proxy.ProxyConfig;

public class ReadOnlyLenientStrategy implements ReadOnlyConnectionStrategy {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public boolean handleSetReadOnly(Connection connection, ConnectionInfo connectionInfo, ProxyConfig proxyConfig) {
        logger.warn("Connection setReadOnly(true) called for datasource: {}",
                connectionInfo.getDataSourceName());
        return true;
    }
}
