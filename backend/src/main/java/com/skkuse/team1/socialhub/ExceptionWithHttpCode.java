package com.skkuse.team1.socialhub;

public class ExceptionWithHttpCode extends Exception {
    private final int httpCode;
    public ExceptionWithHttpCode(int httpCode, String message) {
        super(message);
        this.httpCode = httpCode;
    }

    public int getHttpCode() {
        return httpCode;
    }
}
