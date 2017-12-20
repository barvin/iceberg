package org.iceberg.reportportal.exceptions;

public class RpException extends RuntimeException {

    public RpException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
