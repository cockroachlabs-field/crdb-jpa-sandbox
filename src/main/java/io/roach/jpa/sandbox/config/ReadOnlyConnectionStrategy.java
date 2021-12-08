package io.roach.jpa.sandbox.config;

import java.sql.Connection;

import net.ttddyy.dsproxy.ConnectionInfo;
import net.ttddyy.dsproxy.proxy.ProxyConfig;

public interface ReadOnlyConnectionStrategy {
    boolean handleSetReadOnly(Connection connection, ConnectionInfo connectionInfo, ProxyConfig proxyConfig)
            throws Exception;
}
