package com.ecommerce.project.security.response;

public class MessageResponse {
    public String getMessage() {
        return message;
    }

    public MessageResponse(String message) {
        this.message = message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private String message;

}
