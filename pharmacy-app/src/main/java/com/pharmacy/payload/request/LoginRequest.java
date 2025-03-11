package com.pharmacy.payload.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class LoginRequest {
    
    @NotBlank
    private String username;
    
    @NotBlank
    private String password;
}

