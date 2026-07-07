package com.example.demo.Exception;

public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) { super(message); }
}