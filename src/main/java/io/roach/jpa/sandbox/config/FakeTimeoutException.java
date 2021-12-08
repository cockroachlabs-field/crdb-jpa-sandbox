package io.roach.jpa.sandbox.config;

import java.sql.SQLException;

/**
 * Custom checked exception with 'Timeout' in its name to enable error propagation
 * on setReadOnly capture.
 */
public class FakeTimeoutException extends SQLException {
    public FakeTimeoutException(String reason) {
        super(reason);
    }
}
