package com.ecommerce.project.exceptions;

public class ResourceNotFoundException extends RuntimeException {

    String resourceName;
    String fieldValue;
    String fieldName;
    Long fieldId;

    public ResourceNotFoundException() {
    }

    public ResourceNotFoundException(String resourceName,String fieldValue,Long fieldId) {
        super(String.format("%s not found with %s:%d",resourceName,fieldValue,fieldId));
        this.resourceName = resourceName;
        this.fieldValue = fieldValue;
        this.fieldId = fieldId;


    }

    public ResourceNotFoundException(String resourceName, String fieldName, String fieldValue) {
        super(String.format("%s not found with %s : %s",resourceName,fieldName,fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
}
