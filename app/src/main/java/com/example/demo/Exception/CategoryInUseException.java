package com.example.demo.Exception;

public class CategoryInUseException extends RuntimeException {
    public CategoryInUseException(String message) { super(message); }
}