package com.ecommerce.project.security.response;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

public class UserInfoResponse {
    private Long id;
    private String jwtToken;
    private String username;
    private List<String> roles;

    public String getJwtToken() {
        return jwtToken;
    }

    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    public String getUsername() {
        return username;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserInfoResponse(Long id, String jwtToken, List<String> roles, String username) {
        this.id=id;
        this.jwtToken = jwtToken;
        this.roles = roles;
        this.username = username;
    }
    public UserInfoResponse(Long id,  List<String> roles, String username) {
        this.id=id;
        this.roles = roles;
        this.username = username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}

