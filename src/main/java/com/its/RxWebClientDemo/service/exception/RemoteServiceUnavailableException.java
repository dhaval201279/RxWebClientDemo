package com.its.RxWebClientDemo.service.exception;

public class RemoteServiceUnavailableException extends RuntimeException {
    public RemoteServiceUnavailableException(String msg) {
        super(msg);
    }

    public RemoteServiceUnavailableException(String msg, Exception ex) {
        super(msg, ex);
    }
}
