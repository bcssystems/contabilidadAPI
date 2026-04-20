package com.bcsystems.contabilidad.exception;

public class RepeatedException extends RuntimeException {
    public RepeatedException(String message) {
        super(message);
    }
}
