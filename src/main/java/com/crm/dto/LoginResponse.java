// LoginResponse.java
package com.crm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String type = "Bearer";
    private String username;
    private String message;

    public LoginResponse(String token, String username, String message) {
        this.token = token;
        this.username = username;
        this.message = message;
    }
}