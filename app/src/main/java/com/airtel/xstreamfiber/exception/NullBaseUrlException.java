package com.airtel.xstreamfiber.exception;

public class NullBaseUrlException extends RuntimeException {
    @Override
    public String getMessage() {
        return "baseURL == null";
    }
}
