package com.its.RxWebClientDemo.exception;

public class BackendServiceException extends RuntimeException{
    private int statusCode;

    public BackendServiceException (String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
